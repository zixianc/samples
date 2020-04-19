package top.newleaf.zookeeper.lock;

/**
 * @author chengshx
 * @date 2020/4/15
 */
public class Lock {

    /** 锁的key */
    private String key;
    /** 占有线程标识 */
    private Object identifier;

    public Lock() {
    }

    public Lock(String key, Object identifier) {
        this.key = key;
        this.identifier = identifier;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public Object getIdentifier() {
        return identifier;
    }

    public void setIdentifier(Object identifier) {
        this.identifier = identifier;
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("{");
        sb.append("\"key\":\"").append(key).append('\"');
        sb.append(", \"identifier\":").append(identifier);
        sb.append('}');
        return sb.toString();
    }
}
