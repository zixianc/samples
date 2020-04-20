package top.newleaf.zookeeper.configcenter;

import org.I0Itec.zkclient.IZkDataListener;
import top.newleaf.zookeeper.common.ZKClient;
import top.newleaf.zookeeper.util.ZkUtils;

import java.util.concurrent.ConcurrentHashMap;

/**
 * @author chengshx
 * @date 2020/4/20
 */
public class ZkConfig {

    private final static String CONFIG = "/config";
    private static ConcurrentHashMap<String, Object> localConfig = new ConcurrentHashMap<>();

    public static Object getConfig(String key) {
        ZkUtils.checkPath(CONFIG);
        // 本地有从本地取
        String path = ZkUtils.getPath(CONFIG, key);
        Object data = localConfig.get(path);
        if (data == null) {
            synchronized (ZkConfig.class) {
                data = localConfig.get(path);
                if (data == null) {
                    // 本地没有从zk获取并监听配置
                    data = ZKClient.getClient().readData(path);
                    System.out.println("watching config");
                    ZKClient.getClient().subscribeDataChanges(path, new ConfigListener());
                    localConfig.put(path, data);
                }
            }
        }
        return data;
    }

    /**
     * 修改配置文件
     * @param key
     * @param data
     */
    public static void updateConfig(String key, Object data) {
        // 将之前的配置存做历史版本
        Object oldData = getConfig(key);
        String path = ZkUtils.getPath(CONFIG, key);
        String oldPath = ZkUtils.getPath(path, "v-");
        ZKClient.getClient().createPersistentSequential(oldPath, oldData);
        // 更新远程配置
        ZKClient.getClient().writeData(path, data);
    }

    static class ConfigListener implements IZkDataListener {

        @Override
        public void handleDataChange(String dataPath, Object data) throws Exception {
            localConfig.put(dataPath, data);
        }

        @Override
        public void handleDataDeleted(String dataPath) throws Exception {
            localConfig.remove(dataPath);
        }
    }
}
