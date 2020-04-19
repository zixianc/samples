package top.newleaf.zookeeper.lock.test;

import top.newleaf.zookeeper.lock.Lock;
import top.newleaf.zookeeper.lock.ZkLock3;

import java.util.concurrent.CountDownLatch;

/**
 * @author chengshx
 * @date 2020/4/19
 */
public class ZkLock3Test {

    private final static String key = "test";

    public static void main(String[] args) {
        CountDownLatch cdl = new CountDownLatch(5);
        for (int i = 0; i < 5; i++) {
            LockThread thread = new LockThread(cdl);
            thread.start();
            cdl.countDown();
        }
    }

    static class LockThread extends Thread {
        CountDownLatch cdl;

        public LockThread(CountDownLatch cdl) {
            this.cdl = cdl;
        }

        @Override
        public void run() {
            try {
                cdl.await();
                Lock lock = null;
                try {
                    lock = ZkLock3.lock(key);
                    if (lock != null) {
                        Thread.sleep(100);
                        System.out.println("finished1");
                        try {
                            lock = ZkLock3.lock(key);
                            if (lock != null) {
                                Thread.sleep(100);
                                System.out.println("finished2");
                            }
                        } finally {
                            ZkLock3.unlock(lock);
                        }
                    }
                } finally {
                    ZkLock3.unlock(lock);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}