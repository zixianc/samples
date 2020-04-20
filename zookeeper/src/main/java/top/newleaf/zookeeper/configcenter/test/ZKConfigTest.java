package top.newleaf.zookeeper.configcenter.test;

import top.newleaf.zookeeper.common.ZKClient;
import top.newleaf.zookeeper.configcenter.ZkConfig;

/**
 * @author chengshx
 * @date 2020/4/20
 */
public class ZKConfigTest {

    public static void main(String[] args) {
//        ZKClient.getClient().createPersistent("/config");
//        ZKClient.getClient().createPersistent("/config/test", "aaa");
        for (int i = 0; i < 5; i++) {
            new TestThread(i).start();
        }
    }

    static class TestThread extends Thread {

        private int i;

        public TestThread(int i) {
            this.i = i;
        }

        @Override
        public void run() {
            String data = (String) ZkConfig.getConfig("test");
            System.out.println(data);
            ZkConfig.updateConfig("test", "test" + i);
            System.out.println(i);
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            data = (String) ZkConfig.getConfig("test");
            System.out.println(data);
        }
    }
}
