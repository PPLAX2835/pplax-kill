package xyz.pplax.kill;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import xyz.pplax.kill.dto.PPLAXKillMsgBody;
import xyz.pplax.kill.entity.PPLAXKill;
import xyz.pplax.kill.entity.PayOrder;
import xyz.pplax.kill.mapper.PPLAXKillMapper;
import xyz.pplax.kill.mapper.PayOrderMapper;
import xyz.pplax.kill.mapper.cache.RedisMapper;
import xyz.pplax.kill.mq.MQConsumer;
import xyz.pplax.kill.mq.MQProducer;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

@SpringBootTest
class PPLAXKillApplicationTests {

    @Test
    void contextLoads() {
    }

    @Autowired
    PayOrderMapper payOrderMapper;
    @Test
    void PayOrderMapperTest() {
        payOrderMapper.insertPayOrder(1000, 18830292772L, new Date());
        PayOrder payOrder = payOrderMapper.queryByIdWithkillId(1000, 18830292772L);
        System.out.println(payOrder);
    }

    @Autowired
    PPLAXKillMapper pplaxKillMapper;
    @Test
    void PPLAXKillMapperTest() {
        PPLAXKill pplaxKill = pplaxKillMapper.queryById(1001);
        System.out.println(pplaxKill);

        pplaxKillMapper.reduceInventory(pplaxKill.getKillId(), pplaxKill.getVersion(), pplaxKill.getVersion()+1);

        List<PPLAXKill> pplaxKills = pplaxKillMapper.queryAll(0, 2);
        System.out.println(pplaxKills);
    }


    @Autowired
    RedisMapper redisMapper;
    @Test
    void RedisMapperTest() {
        List<PPLAXKill> pplaxKills = pplaxKillMapper.queryAll(0, 5);

        redisMapper.setAllGoods(pplaxKills);

        List<PPLAXKill> allGoods = redisMapper.getAllGoods();
        System.out.println(allGoods);

        PPLAXKill iKill = new PPLAXKill();
        iKill.setKillId(1004);
        iKill.setName("商品4");
        iKill.setInventory(100);
        redisMapper.putPPLAXKill(iKill);
        PPLAXKill pplaxKile = redisMapper.getPPLAXKile(1004);
        System.out.println(pplaxKile);
    }


//    @Autowired
//    MQProducer mqProducer;
//    @Test
//    void MQConsumerTest() {
//        for (int i=0;i<100;i++){
//            Thread thread = new Thread(new Sender(mqProducer, i));
//            thread.start();
//        }
//
//        try {
//            Thread.sleep(20000);
//            System.out.println("mq process");
//            List<PPLAXKill> allGoods = redisMapper.getAllGoods();
//            System.out.println(allGoods);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//    }
//    public static class Sender implements Runnable {
//        MQProducer mqProducer;
//        int messageIndex;
//
//        public Sender(MQProducer mqProducer, int messageIndex) {
//            this.mqProducer = mqProducer;
//            this.messageIndex = messageIndex;
//        }
//
//        @Override
//        public void run() {
//
//            PPLAXKillMsgBody pplaxKillMsgBody = new PPLAXKillMsgBody();
//            pplaxKillMsgBody.setKillId(messageIndex);
//            pplaxKillMsgBody.setUserPhone(18830292770L + messageIndex);
//            mqProducer.send(pplaxKillMsgBody);
//
//            try {
//                Thread.sleep(200);
//                System.out.println("mq process");
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//        }
//    }

    @Resource(name = "initJedisPool")
    private JedisPool jedisPool;
    @Test
    public void InitTaskTest() {
//        Jedis jedis = jedisPool.getResource();
//        //清空Redis缓存
//        jedis.flushDB();
        List<PPLAXKill> allGoods = redisMapper.getAllGoods();
        System.out.println(allGoods);
    }

}
