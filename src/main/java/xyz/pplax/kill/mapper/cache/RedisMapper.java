package xyz.pplax.kill.mapper.cache;

import io.protostuff.LinkedBuffer;
import io.protostuff.ProtostuffIOUtil;
import io.protostuff.runtime.RuntimeSchema;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import xyz.pplax.kill.constant.RedisKey;
import xyz.pplax.kill.constant.RedisKeyPrefix;
import xyz.pplax.kill.entity.PPLAXKill;
import xyz.pplax.kill.singleton.MyRuntimeSchema;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Repository
public class RedisMapper {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Resource(name = "initJedisPool")
    private JedisPool jedisPool;

    // 获得单例类对象
    private RuntimeSchema<PPLAXKill> runtimeSchema = MyRuntimeSchema.getInstance().getGoodsRuntimeSchema();

    /**
     * 从redis中根据killId获得对应值
     * @param killId
     * @return
     */
    public PPLAXKill getPPLAXKile(long killId) {
        // redis操作
        try (Jedis jedis = jedisPool.getResource()) {
            String key = RedisKeyPrefix.PPLAXKILL_GOODS + killId;     // 获得键
            byte[] bytes = jedis.get(key.getBytes());               // 通过键的字节数组从redis中获得对应值的字节数组
            // 判断bytes是否为空
            if (bytes != null) {
                PPLAXKill pplaxKill = runtimeSchema.newMessage();
                ProtostuffIOUtil.mergeFrom(bytes, pplaxKill, runtimeSchema);
                // 反序列化
                return pplaxKill;
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return null;
    }

    /**
     * 将pplaxKill放到redis中
     * @param pplaxKill
     * @return
     */
    public String putPPLAXKill (PPLAXKill pplaxKill) {
        // set Object(PPLAXKill) -> 序列化 -> byte[]
        try (Jedis jedis = jedisPool.getResource()) {
            String key = RedisKeyPrefix.PPLAXKILL_GOODS + pplaxKill.getKillId();    // 拼成键
            byte[] bytes = ProtostuffIOUtil.toByteArray(pplaxKill, runtimeSchema, LinkedBuffer.allocate(LinkedBuffer.DEFAULT_BUFFER_SIZE));     // 获得对象的字节数组
            return jedis.set(key.getBytes(), bytes);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return null;
    }

    /**
     * 从redis中获得所有实时商品数据(包括实时库存量)
     * @return
     */
    public List<PPLAXKill> getAllGoods() {
        List<PPLAXKill> result = new ArrayList<>();
        Jedis jedis = jedisPool.getResource();
        Set<String> idSet = jedis.smembers(RedisKey.PPLAXKILL_ID_SET);

        if (idSet != null || idSet.size() > 0) {
            for (String killId : idSet) {
                String key = RedisKeyPrefix.PPLAXKILL_GOODS + killId;
                byte[] bytes = jedis.get(key.getBytes());
                if (bytes != null) {
                    PPLAXKill pplaxKill = runtimeSchema.newMessage();
                    ProtostuffIOUtil.mergeFrom(bytes, pplaxKill, runtimeSchema);

                    try {
                        // key获取到的库存量是初始值，并不是当前值，所有需要从RedisKeyPrefix.PPLAXKILL_INVENTORY+KillId
                        // 获取到的库存，再设置到结果中去
                        String inventoryStr = jedis.get(RedisKeyPrefix.PPLAXKILL_GOODS + killId);
                        if (!StringUtils.hasText(inventoryStr)) {
                            pplaxKill.setInventory(Integer.parseInt(inventoryStr));
                        }
                    } catch (NumberFormatException e) {
                        logger.error(e.getMessage(), e);
                    }
                    result.add(pplaxKill);
                }
            }
        }
        jedis.close();
        return result;
    }

    public void setAllGoods(List<PPLAXKill> list) {
        Jedis jedis = jedisPool.getResource();
        if (list == null || list.size() < 1) {
            logger.info("--FatalError!!! seckill_list_data is empty");
            return ;
        }

        jedis.del(RedisKey.PPLAXKILL_ID_SET);

        for (PPLAXKill pplaxKill : list) {
            jedis.sadd(RedisKey.PPLAXKILL_ID_SET, pplaxKill.getKillId() + "");

            String pplaxKillGoodsKey = RedisKeyPrefix.PPLAXKILL_GOODS + pplaxKill.getKillId();
            byte[] bytes = ProtostuffIOUtil.toByteArray(pplaxKill, MyRuntimeSchema.getInstance().getGoodsRuntimeSchema(), LinkedBuffer.allocate(LinkedBuffer.DEFAULT_BUFFER_SIZE));
            jedis.set(pplaxKillGoodsKey.getBytes(), bytes);
        }

        jedis.close();
        logger.info("数据库Goods数据已同步到Redis");
    }



}
