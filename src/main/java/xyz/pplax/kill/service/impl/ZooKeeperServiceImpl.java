package xyz.pplax.kill.service.impl;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;
import org.apache.curator.retry.RetryNTimes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import xyz.pplax.kill.bean.ZKConfigBean;
import xyz.pplax.kill.config.ZookeeperConfig;
import xyz.pplax.kill.service.ZooKeeperService;

@Service
public class ZooKeeperServiceImpl implements ZooKeeperService {

    @Autowired
    ZKConfigBean zkConfigBean;


    @Override
    public CuratorFramework createCuratorFramework() {
        CuratorFramework curatorFramework = CuratorFrameworkFactory.newClient(
                zkConfigBean.getConnectStr(),
                zkConfigBean.getSessionTimeout(),
                zkConfigBean.getConnectTimeout(),
                new RetryNTimes(3, 1000)
        );
        curatorFramework.start();
        return curatorFramework;
    }

    @Override
    public InterProcessMutex getLock() {
        return new InterProcessMutex(createCuratorFramework(), zkConfigBean.getLockRoot());
    }

}
