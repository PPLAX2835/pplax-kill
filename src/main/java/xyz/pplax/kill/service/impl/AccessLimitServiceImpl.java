package xyz.pplax.kill.service.impl;

import com.google.common.util.concurrent.RateLimiter;
import org.springframework.stereotype.Service;
import xyz.pplax.kill.service.AccessLimitService;

/**
 * 通过Google guava的RateLimiter进行限流操作
 */
@Service
public class AccessLimitServiceImpl implements AccessLimitService {

    /**
     * 每秒只发出十个令牌,只有拿到令牌才可以进行秒杀
     */
    private RateLimiter rateLimiter = RateLimiter.create(10);

    /**
     * 尝试获取令牌
     * @return
     */
    @Override
    public boolean tryAcquirePPLAXKill() {
        return rateLimiter.tryAcquire();
    }
}
