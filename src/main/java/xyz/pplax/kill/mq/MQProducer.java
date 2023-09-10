package xyz.pplax.kill.mq;

import com.alibaba.fastjson.JSON;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.MessageProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import xyz.pplax.kill.bean.MQConfigBean;
import xyz.pplax.kill.constant.RedisKey;
import xyz.pplax.kill.dto.PPLAXKillMsgBody;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.concurrent.TimeoutException;

@Component
public class MQProducer {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private MQChannelManager mqChannelManager;

    @Resource(name = "initJedisPool")
    private JedisPool jedisPool;

    @Autowired
    private MQConfigBean mqConfigBean;


    public void send(PPLAXKillMsgBody pplaxKillMsgBody) {
        // 获得json串
        String msg = JSON.toJSONString(pplaxKillMsgBody);

        // 获取当前线程的RabbitMQ通道
        Channel channel = mqChannelManager.getSendChannel();
        try {
            logger.info(" [mqSend] '" + msg + "'");
            channel.confirmSelect();

            channel.basicPublish(
                    "",
                    mqConfigBean.getQueue(),
                    MessageProperties.PERSISTENT_TEXT_PLAIN,
                    msg.getBytes()
                    );
        } catch (IOException e) {
            e.printStackTrace();
        }


        // 确认消息的可靠性传递
        boolean sendAcked = false;
        try {
            sendAcked = channel.waitForConfirms(100);
        } catch (InterruptedException | TimeoutException e) {
            e.printStackTrace();
        }

        // 如果能够RabbitMQ能够连接,进行redis操作
        logger.info("sendAcked={}", sendAcked);
        if (sendAcked) {
            Jedis jedis = jedisPool.getResource();
            jedis.sadd(RedisKey.QUEUE_PRE_PPLAXKILL, pplaxKillMsgBody.getKillId() + "@" + pplaxKillMsgBody.getUserPhone());
            jedis.close();
        } else {
            logger.info("!!!mqSend_NACKED,NOW_RETRY>>>");
            try {
                channel.basicPublish("",
                        mqConfigBean.getQueue(),
                        MessageProperties.PERSISTENT_TEXT_PLAIN,
                        msg.getBytes());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }





}
