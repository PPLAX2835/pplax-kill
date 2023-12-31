package xyz.pplax.kill.config;

import xyz.pplax.kill.bean.ZKConfigBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ZookeeperConfig {
    @Value("${zookeeper.lock-root}")
    private String lockRoot;

    @Value("${zookeeper.session-timeout}")
    private String sessionTimeout;

    @Value("${zookeeper.connect-str}")
    private String connectStr;

    @Value("${zookeeper.connect-timeout}")
    private String connectTimeout;

    @Value("${zookeeper.lock-acquire-timeout}")
    private String lockAcquireTimeout;

    @Bean
    public ZKConfigBean zkConfigBean() {
        ZKConfigBean zkConfigBean = new ZKConfigBean();
        zkConfigBean.setLockRoot(lockRoot);
        zkConfigBean.setSessionTimeout(Integer.parseInt(sessionTimeout));
        zkConfigBean.setConnectStr(connectStr);
        zkConfigBean.setConnectTimeout(Integer.parseInt(connectTimeout));
        zkConfigBean.setLockAcquireTimeout(Integer.parseInt(lockAcquireTimeout));
        return zkConfigBean;
    }
}
