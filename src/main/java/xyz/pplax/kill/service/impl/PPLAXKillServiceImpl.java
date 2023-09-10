package xyz.pplax.kill.service.impl;

import org.checkerframework.checker.units.qual.A;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import xyz.pplax.kill.constant.RedisKey;
import xyz.pplax.kill.constant.RedisKeyPrefix;
import xyz.pplax.kill.dto.Exposer;
import xyz.pplax.kill.dto.PPLAXKillExecution;
import xyz.pplax.kill.dto.PPLAXKillMsgBody;
import xyz.pplax.kill.entity.PPLAXKill;
import xyz.pplax.kill.entity.PayOrder;
import xyz.pplax.kill.enums.PPLAXKillStateEnum;
import xyz.pplax.kill.exception.PPLAXKillException;
import xyz.pplax.kill.mapper.PPLAXKillMapper;
import xyz.pplax.kill.mapper.PayOrderMapper;
import xyz.pplax.kill.mapper.cache.RedisMapper;
import xyz.pplax.kill.mq.MQProducer;
import xyz.pplax.kill.service.AccessLimitService;
import xyz.pplax.kill.service.PPLAXKillService;
import xyz.pplax.kill.utils.EncryptionUtil;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

/**
 * 秒杀service
 */
@Service
public class PPLAXKillServiceImpl implements PPLAXKillService {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private PPLAXKillMapper pplaxKillMapper;
    @Autowired
    private PayOrderMapper payOrderMapper;
    @Autowired
    private RedisMapper redisMapper;
    @Autowired
    private AccessLimitService accessLimitService;

    @Autowired
    private MQProducer mqProducer;
    @Resource(name = "initJedisPool")
    private JedisPool jedisPool;

    /**
     * 获得PPLAXKill的列表,优先从缓存中读取
     * @return
     */
    @Override
    public List<PPLAXKill> getKillList() {
        List<PPLAXKill> list = redisMapper.getAllGoods();
        if (list == null || list.size()<1) {
            list = pplaxKillMapper.queryAll(0, 10);
            redisMapper.setAllGoods(list);
        }
        return list;
    }

    /**
     * 通过id查询
     * @param killId
     * @return
     */
    @Override
    public PPLAXKill getById(long killId) {
        return pplaxKillMapper.queryById(killId);
    }

    @Override
    public Exposer exportKillUrl(long killId) {
        // 优化点:缓存优化:超时的基础上维护一致性

        // 访问redis
        PPLAXKill pplaxKill = redisMapper.getPPLAXKile(killId);
        if (pplaxKill == null) {    // 判断redis中是否有存储
            // redis中没有,访问数据库
            pplaxKill = pplaxKillMapper.queryById(killId);
            if (pplaxKill == null) {
                // 数据库中也没有,说明商品没有在售
                return new Exposer(false, killId);
            } else {
                // 存入redis
                redisMapper.putPPLAXKill(pplaxKill);
            }
        }

        Date startTime = pplaxKill.getStartTime();
        Date endTime = pplaxKill.getEndTime();
        // 系统当前时间
        Date nowTime = new Date();
        if (nowTime.getTime() < startTime.getTime() || nowTime.getTime() > endTime.getTime()) {
            // 已经不在秒杀时间内
            return new Exposer(false, killId, nowTime.getTime(), startTime.getTime(), endTime.getTime());
        }

        // 获得md5
        String md5 = EncryptionUtil.getMD5(String.valueOf(killId));

        return new Exposer(true, md5, killId);
    }

    /**
     * 执行秒杀
     * @param killId
     * @param userPhone
     * @param md5
     * @return
     * @throws PPLAXKillException
     */
    @Override
    public PPLAXKillExecution executeKill(long killId, long userPhone, String md5) throws PPLAXKillException {
        if (accessLimitService.tryAcquirePPLAXKill()) {
            // 获得了令牌
            return handlePPLAXKillAsync(killId, userPhone, md5);
        } else {
            // 没拿到,抛出访问限制的异常
            logger.info("--->ACCESS_LIMITED-->killId={},userPhone={}", killId, userPhone);
            throw new PPLAXKillException(PPLAXKillStateEnum.ACCESS_LIMIT);
        }
    }

    /**
     * 处理秒杀操作
     * @param killId
     * @param userPhone
     * @param md5
     * @return
     * @throws PPLAXKillException
     */
    private PPLAXKillExecution handlePPLAXKillAsync (long killId, long userPhone, String md5) throws PPLAXKillException {
        if (md5 == null || !md5.equals(EncryptionUtil.getMD5(String.valueOf(killId)))) {
            // md5不匹配或不存在,请求非法
            logger.info("PPLAXKILL_DATA_REWRITE!!!. killId={},userPhone={}", killId, userPhone);
            throw new PPLAXKillException(PPLAXKillStateEnum.DATA_REWRITE);
        }

        long threadId = Thread.currentThread().getId();

        Jedis jedis = jedisPool.getResource();
        String inventoryKey = RedisKeyPrefix.PPLAXKILL_INVENTORY + killId;
        String boughtKey = RedisKeyPrefix.BOUGHT_USERS + killId;

        String inventoryStr = jedis.get(inventoryKey);
        int inventory = Integer.parseInt(inventoryStr);
        if (inventory <= 0) {
            // 卖完了
            jedis.close();
            logger.info("PPLAXKILLSOLD_OUT. killId={},userPhone={}", killId, userPhone);
            throw new PPLAXKillException(PPLAXKillStateEnum.SOLD_OUT);
        }
        if (jedis.sismember(boughtKey, String.valueOf(userPhone))) {
            jedis.close();
            // 重复请求
            logger.info("PPLAXKILL_REPEATED. killId={},userPhone={}", killId, userPhone);
            throw new PPLAXKillException(PPLAXKillStateEnum.REPEAT_KILL);
        } else {
            jedis.close();

            // 进入待秒杀队列，进行后续串行操作
            PPLAXKillMsgBody pplaxKillMsgBody = new PPLAXKillMsgBody();
            pplaxKillMsgBody.setKillId(killId);
            pplaxKillMsgBody.setUserPhone(userPhone);
            mqProducer.send(pplaxKillMsgBody);

            // 秒杀成功,返回给客户端
            PayOrder payOrder = new PayOrder();
            payOrder.setUserPhone(userPhone);
            payOrder.setKillId(killId);
            payOrder.setState(PPLAXKillStateEnum.ENQUEUE_PRE_PPLAXKILL.getState());
            logger.info("ENQUEUE_PRE_PPLAXKILL>>>killId={},userPhone={}", killId, userPhone);
            return new PPLAXKillExecution(killId, PPLAXKillStateEnum.ENQUEUE_PRE_PPLAXKILL, payOrder);
        }
    }

    /**
     * 先插入秒杀记录再减库存
     * @param killId
     * @param userPhone
     * @return
     * @throws PPLAXKillException
     */
    @Override
    public PPLAXKillExecution updateInventory(long killId, long userPhone) throws PPLAXKillException {
        // 执行逻辑---减库存,记录购买行为
        Date nowTime = new Date();
        try {
            //插入秒杀记录(记录购买行为)
            //这处， kill_record的id等于这个特定id的行被启用了行锁，但是其他的事务可以insert另外一行，不会阻止其他事务里对这个表的insert操作
            int insertCount = payOrderMapper.insertPayOrder(killId, userPhone, nowTime);

            // 唯一:killId,userPhone
            if (insertCount <= 0) {
                // 重复请求
                logger.info("kill REPEATED. killId={},userPhone={}", killId, userPhone);
                throw new PPLAXKillException(PPLAXKillStateEnum.REPEAT_KILL);
            } else {
                //减库存,热点商品竞争
                // reduceNumber是update操作，开启作用在表kill上的行锁
                PPLAXKill currentPPLAXKill = pplaxKillMapper.queryById(killId);
                boolean validTime = false;
                if (currentPPLAXKill != null) {
                    long nowStamp = nowTime.getTime();
                    if (
                            nowStamp > currentPPLAXKill.getStartTime().getTime() && nowStamp < currentPPLAXKill.getEndTime().getTime()
                            && currentPPLAXKill.getInventory() > 0 && currentPPLAXKill.getVersion() > -1
                    ) {
                        validTime = true;
                    }
                }

                if (validTime) {
                    long oldVersion = currentPPLAXKill.getVersion();
                    // update操作开始，表seckill的seckill_id等于seckillId的行被启用了行锁,   其他的事务无法update这一行， 可以update其他行
                    int updateCount = pplaxKillMapper.reduceInventory(killId, oldVersion, oldVersion + 1);
                    if (updateCount <= 0) {
                        //没有更新到记录，秒杀结束,rollback
                        logger.info("kill_DATABASE_CONCURRENCY_ERROR!!!. killId={},userPhone={}", killId, userPhone);
                        throw new PPLAXKillException(PPLAXKillStateEnum.DB_CONCURRENCY_ERROR);
                    } else {
                        //秒杀成功 commit
                        PayOrder payOrder = payOrderMapper.queryByIdWithkillId(killId, userPhone);
                        logger.info("kill SUCCESS->>>. killId={},userPhone={}", killId, userPhone);
                        return new PPLAXKillExecution(killId, PPLAXKillStateEnum.SUCCESS, payOrder);
                        //return后，事务结束，关闭作用在表seckill上的行锁
                        // update结束，行锁被取消  。reduceInventory()被执行前后数据行被锁定, 其他的事务无法写这一行。
                    }
                } else {
                    logger.info("kill_END. seckillId={},userPhone={}", killId, userPhone);
                    throw new PPLAXKillException(PPLAXKillStateEnum.END);
                }
            }
        } catch (PPLAXKillException e1) {
            throw e1;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            //  所有编译期异常 转化为运行期异常
            throw new PPLAXKillException(PPLAXKillStateEnum.INNER_ERROR);
        }
    }

    /**
     * 在Redis中真正进行秒杀操作
     * @param killId
     * @param userPhone
     * @throws PPLAXKillException
     */
    @Override
    public void handleInRedis(long killId, long userPhone) throws PPLAXKillException {
        Jedis jedis = jedisPool.getResource();

        String inventoryKey = RedisKeyPrefix.PPLAXKILL_INVENTORY + killId;
        String boughtKey = RedisKeyPrefix.BOUGHT_USERS + killId;

        String inventoryStr = jedis.get(inventoryKey);
        int inventory = Integer.parseInt(inventoryStr);
        if (inventory <= 0) {
            // 卖完了
            logger.info("handleInRedis PPLAXKILLSOLD_OUT. killId={},userPhone={}", killId, userPhone);
            throw new PPLAXKillException(PPLAXKillStateEnum.SOLD_OUT);
        }
        if (jedis.sismember(boughtKey, String.valueOf(userPhone))) {
            // 重复请求
            logger.info("handleInRedis PPLAXKILL_REPEATED. killId={},userPhone={}", killId, userPhone);
            throw new PPLAXKillException(PPLAXKillStateEnum.REPEAT_KILL);
        }

        jedis.decr(inventoryKey);
        jedis.sadd(boughtKey, String.valueOf(userPhone));
        logger.info("handleInRedis_done");
    }

    /**
     * 判断秒杀状态
     * @param killId
     * @param userPhone
     * @return 0： 排队中; 1: 秒杀成功; 2： 秒杀失败
     */
    @Override
    public int isGrab(long killId, long userPhone) {
        int result = 0;

        Jedis jedis = jedisPool.getResource();
        try {
            String boughtKey = RedisKeyPrefix.BOUGHT_USERS + killId;
            result = jedis.sismember(boughtKey, String.valueOf(userPhone)) ? 1 : 0;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            result = 0;
        }

        if (result == 0) {
            if (!jedis.sismember(RedisKey.QUEUE_PRE_PPLAXKILL, killId + "@" + userPhone)) {
                result = 2;
            }
        }

        return result;
    }
}
