package top.newleaf.zookeeper.lock;

import org.I0Itec.zkclient.IZkDataListener;
import top.newleaf.zookeeper.ZKClient;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * watch前一个节点
 * @author chengshx
 * @date 2020/4/15
 */
public class ZkLock2 {

    private static String LOCK = "/lock2";

    static {
        checkPath();
    }

    public static Lock lock(String key) {
        long threadId = Thread.currentThread().getId();
        // 创建临时节点后，判断节点是不是顺序第一的节点
        String currentPath = LockUtils.getPath(LOCK, key);
        currentPath = ZKClient.getClient().createEphemeralSequential(currentPath, threadId);
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
                return new Lock(currentPath, threadId);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public static void unlock(Lock lock) {
        if (lock != null) {
            Long val = ZKClient.getClient().readData(lock.getKey());
            if (val != null && val.equals(lock.getIdentifier())) {
                ZKClient.getClient().delete(lock.getKey());
            }
        }
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

    private static void checkPath() {
        if (!ZKClient.getClient().exists(LOCK)) {
            ZKClient.getClient().createPersistent(LOCK);
        }
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
            cdl.countDown();
            System.out.println("等待锁成功" + currentPath);
            ZKClient.getClient().unsubscribeDataChanges(beforePath, this);
        }
    }
}
