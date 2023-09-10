package xyz.pplax.kill.service;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;

public interface ZooKeeperService {
    // 创建 CuratorFramework 实例
    public CuratorFramework createCuratorFramework();

    // 获取分布式锁
    public InterProcessMutex getLock();
}
