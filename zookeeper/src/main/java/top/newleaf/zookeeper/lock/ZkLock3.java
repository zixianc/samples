package top.newleaf.zookeeper.lock;

import org.I0Itec.zkclient.IZkDataListener;
import top.newleaf.zookeeper.ZKClient;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * watch前一个节点，可重入
 * @author chengshx
 * @date 2020/4/15
 */
public class ZkLock3 {

    private final static String LOCK = "/lock2";

    static {
        if (!ZKClient.getClient().exists(LOCK)) {
            ZKClient.getClient().createPersistent(LOCK);
        }
    }

    public static Lock lock(String key) {
        long threadId = Thread.currentThread().getId();
        // 重入锁
        Lock lock = reenterLock(threadId);
        if (lock != null) {
            return lock;
        }
        // 创建临时节点后，判断节点是不是顺序第一的节点
        String currentPath = LockUtils.getPath(LOCK, key);
        String data = incrSequence(threadId, "0");
        currentPath = ZKClient.getClient().createEphemeralSequential(currentPath, data);
        List<String> children = getChildren();
        String path = LockUtils.getPath(LOCK, children.get(0));
        // 顺序第一抢锁成功，生成锁记录
        if (currentPath.equals(path)) {
            System.out.println("上锁成功" + currentPath);
            return new Lock(currentPath, threadId);
        } else {
            // 抢锁失败监听前一个节点（排队，公平锁）
            String beforePath = getBeforePath(children, currentPath);
            System.out.println(currentPath + " listening " + beforePath);
            CountDownLatch cdl = new CountDownLatch(1);
            LockListener listener = new LockListener(currentPath, beforePath, cdl);
            ZKClient.getClient().subscribeDataChanges(beforePath, listener);
            try {
                // 前一个节点删除则等待锁成功
                cdl.await();
                ZKClient.getClient().writeData(currentPath, data);
                return new Lock(currentPath, threadId);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    private static Lock reenterLock(long threadId) {
        List<String> children = getChildren();
        if (children != null && !children.isEmpty()) {
            String key = children.get(0);
            String path = LockUtils.getPath(LOCK, key);
            String data = ZKClient.getClient().readData(path);
            String[] values = data.split("_");
            if (values[0].equals(String.valueOf(threadId))) {
                ZKClient.getClient().writeData(path, incrSequence(threadId, values[1]));
                return new Lock(path, threadId);
            }
        }
        return null;
    }

    public static void unlock(Lock lock) {
        if (lock != null) {
            String data = ZKClient.getClient().readData(lock.getKey());
            if (data != null) {
                String[] values = data.split("_");
                if (values[0].equals(String.valueOf(lock.getIdentifier()))) {
                    if (Integer.valueOf(values[1]) == 1) {
                        ZKClient.getClient().delete(lock.getKey());
                        System.out.println("释放锁" + lock.getKey());
                    } else {
                        ZKClient.getClient().writeData(lock.getKey(), decrSequence(values[0], values[1]));
                        System.out.println("释放重入锁" + lock.getKey());
                    }
                }
            }
        }
    }

    private static String incrSequence(Object threadId, String number) {
        return threadId + "_" + (Integer.valueOf(number) + 1);
    }

    private static String decrSequence(Object threadId, String number) {
        return threadId + "_" + (Integer.valueOf(number) - 1);
    }

    private static List<String> getChildren() {
        List<String> children = ZKClient.getClient().getChildren(LOCK);
        Collections.sort(children);
        return children;
    }

    private static String getBeforePath(List<String> children, String currentPath) {
        int i = Collections.binarySearch(children, currentPath.substring(LOCK.length() + 1));
        return LockUtils.getPath(LOCK, children.get(i - 1));
    }

    static class LockListener implements IZkDataListener {

        private String currentPath;
        private String beforePath;
        private CountDownLatch cdl;

        public LockListener(String currentPath, String beforePath, CountDownLatch cdl) {
            this.currentPath = currentPath;
            this.beforePath = beforePath;
            this.cdl = cdl;
        }

        @Override
        public void handleDataChange(String dataPath, Object data) throws Exception {

        }

        @Override
        public void handleDataDeleted(String dataPath) throws Exception {
            System.out.println("等待锁成功" + currentPath);
            cdl.countDown();
            ZKClient.getClient().unsubscribeDataChanges(beforePath, this);
        }
    }
}
