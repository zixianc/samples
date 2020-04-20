package top.newleaf.zookeeper.util;

import top.newleaf.zookeeper.common.ZKClient;

/**
 * @author chengshx
 * @date 2020/4/19
 */
public class ZkUtils {

    public final static String SEPARATOR = "/";

    public static String getPath(String prefix, String key) {
        if (!prefix.endsWith(SEPARATOR)) {
            prefix = prefix + SEPARATOR;
        }
        if (key.startsWith(SEPARATOR)) {
            key = key.substring(1);
        }
        return prefix + key;
    }

    public static void checkPath(String path) {
        if (!ZKClient.getClient().exists(path)) {
            synchronized (ZkUtils.class) {
                if (!ZKClient.getClient().exists(path)) {
                    ZKClient.getClient().createPersistent(path);
                }
            }
        }
    }

}
