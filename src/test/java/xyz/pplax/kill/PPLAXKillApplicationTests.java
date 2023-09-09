package xyz.pplax.kill;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import xyz.pplax.kill.entity.PPLAXKill;
import xyz.pplax.kill.entity.PayOrder;
import xyz.pplax.kill.mapper.PPLAXKillMapper;
import xyz.pplax.kill.mapper.PayOrderMapper;

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

}
