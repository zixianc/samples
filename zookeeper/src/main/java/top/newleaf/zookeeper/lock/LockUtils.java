package top.newleaf.zookeeper.lock;

/**
 * @author chengshx
 * @date 2020/4/19
 */
public class LockUtils {

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

}
