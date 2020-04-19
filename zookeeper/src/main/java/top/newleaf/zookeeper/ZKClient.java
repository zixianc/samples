package top.newleaf.zookeeper;

import org.I0Itec.zkclient.ZkClient;

/**
 * @author chengshx
 * @date 2020/4/19
 */
public class ZKClient {

    private static ZkClient zkClient = null;;

    public static ZkClient getClient() {
        if (zkClient == null) {
            synchronized (ZKClient.class) {
                if (zkClient == null) {
                    zkClient = new ZkClient("localhost:2181");
                }
            }
        }
        return zkClient;
    }
}
