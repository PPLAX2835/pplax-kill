package xyz.pplax.kill.bean.boot;


import io.protostuff.LinkedBuffer;
import io.protostuff.ProtostuffIOUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import xyz.pplax.kill.constant.RedisKey;
import xyz.pplax.kill.constant.RedisKeyPrefix;
import xyz.pplax.kill.entity.PPLAXKill;
import xyz.pplax.kill.mapper.PPLAXKillMapper;
import xyz.pplax.kill.mq.MQConsumer;
import xyz.pplax.kill.singleton.MyRuntimeSchema;

import javax.annotation.Resource;
import java.util.List;

@Component
public class InitTask implements CommandLineRunner {
    private static final Logger logger = LoggerFactory.getLogger(InitTask.class);

    @Resource(name = "initJedisPool")
    private JedisPool jedisPool;
    @Resource
    private PPLAXKillMapper pplaxKillMapper;
    @Resource
    private MQConsumer mqConsumer;

    @Override
    public void run(String... args) throws Exception {
        initRedis();
        logger.info("StartToConsumeMsg--->");
        mqConsumer.receive();
    }

    /**
     * 预热秒杀数据到Redis
     */
    private void initRedis() {
        Jedis jedis = jedisPool.getResource();
        //清空Redis缓存
        jedis.flushDB();

        List<PPLAXKill> pplaxKillList = pplaxKillMapper.queryAll(0, 10);
        if (pplaxKillList == null || pplaxKillList.size()< 1) {
            logger.info("--FatalError!!! kill_list_data is empty");
            return;
        }

        for (PPLAXKill pplaxKill : pplaxKillList) {
            jedis.sadd(RedisKey.PPLAXKILL_ID_SET, pplaxKill.getKillId() + "");

            String inventoryKey = RedisKeyPrefix.PPLAXKILL_INVENTORY + pplaxKill.getKillId();
            jedis.set(inventoryKey, String.valueOf(pplaxKill.getInventory()));

            String killGoodsKey = RedisKeyPrefix.PPLAXKILL_GOODS + pplaxKill.getKillId();
            byte[] goodsBytes = ProtostuffIOUtil.toByteArray(pplaxKill, MyRuntimeSchema.getInstance().getGoodsRuntimeSchema(),
                    LinkedBuffer.allocate(LinkedBuffer.DEFAULT_BUFFER_SIZE));
            jedis.set(killGoodsKey.getBytes(), goodsBytes);
        }
        jedis.close();
        logger.info("Redis缓存数据初始化完毕！");
    }
}
