package top.newleaf.zookeeper.lock;

import org.I0Itec.zkclient.IZkDataListener;
import org.I0Itec.zkclient.exception.ZkNodeExistsException;
import top.newleaf.zookeeper.ZKClient;

import java.util.concurrent.CountDownLatch;

/**
 * watch同一个节点
 * @author chengshx
 * @date 2020/4/14
 */
public class ZkLock1 {

    private final static String LOCK = "/lock1";

    public static Lock lock() {
        long threadId = Thread.currentThread().getId();
        try {
            ZKClient.getClient().createEphemeral(LOCK, threadId);
            System.out.println("上锁成功" + threadId);
            return new Lock(LOCK, threadId);
        } catch (ZkNodeExistsException e) {
            CountDownLatch cdl = new CountDownLatch(1);
            LockListener listener = new LockListener(cdl, threadId);
            ZKClient.getClient().subscribeDataChanges(LOCK, listener);
            try {
                cdl.await();
                ZKClient.getClient().createEphemeral(LOCK, threadId);
                System.out.println("等待锁成功" + threadId);
                return new Lock(LOCK, threadId);
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            } catch (ZkNodeExistsException e1) {}
        }
        return null;
    }

    public static void unlock(Lock lock) {
        if (lock != null) {
            Long val = ZKClient.getClient().readData(lock.getKey());
            if (val != null && val.equals(lock.getIdentifier())) {
                ZKClient.getClient().delete(lock.getKey());
                System.out.println("释放锁" + lock.getIdentifier());
            }
        }
    }

    private static void checkPath() {
        if (!ZKClient.getClient().exists(LOCK)) {
            ZKClient.getClient().createPersistent(LOCK);
        }
    }

    static class LockListener implements IZkDataListener {

        private CountDownLatch cdl;
        private long threadId;

        public LockListener(CountDownLatch cdl, long threadId) {
            this.cdl = cdl;
            this.threadId = threadId;
        }

        @Override
        public void handleDataChange(String dataPath, Object data) throws Exception {

        }

        @Override
        public void handleDataDeleted(String dataPath) throws Exception {
            cdl.countDown();
            ZKClient.getClient().unsubscribeDataChanges(LOCK, this);
        }
    }
}
