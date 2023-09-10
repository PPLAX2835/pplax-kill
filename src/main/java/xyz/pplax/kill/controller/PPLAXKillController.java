package xyz.pplax.kill.controller;

import com.alibaba.fastjson.JSON;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import xyz.pplax.kill.dto.Exposer;
import xyz.pplax.kill.dto.PPLAXKillExecution;
import xyz.pplax.kill.dto.PPLAXKillResult;
import xyz.pplax.kill.entity.PPLAXKill;
import xyz.pplax.kill.enums.PPLAXKillStateEnum;
import xyz.pplax.kill.exception.PPLAXKillException;
import xyz.pplax.kill.service.PPLAXKillService;

import java.util.Date;
import java.util.List;

@Controller
@RequestMapping("/pplaxKill")
public class PPLAXKillController {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private PPLAXKillService pplaxKillService;

    @RequestMapping("/demo")
    @ResponseBody
    public String demo() {
        long killId = 1000L;
        PPLAXKill pplaxKill = pplaxKillService.getById(killId);
        Thread currentThread = Thread.currentThread();
        logger.info("thread.hashCode={},id={},name={}"
                , new Object[]{currentThread.hashCode(), currentThread.getId(), currentThread.getName()});
        return JSON.toJSONString(pplaxKill);
    }

    @RequestMapping(value = "/list", method = RequestMethod.GET)
    public String list(Model model) {
        //获取列表页
        List<PPLAXKill> list = pplaxKillService.getKillList();
        model.addAttribute("list", list);
        return "list";
    }

    @RequestMapping(value = "/detail/{killId}", method = RequestMethod.GET)
    public String detail(@PathVariable("killId") Long killId, Model model) {
        if (killId == null) {
            return "redirect:/pplaxKill/list";
        }
        PPLAXKill pplaxKill = pplaxKillService.getById(killId);
        if (pplaxKill == null) {
            return "forward:/pplaxKill/list";
        }
        model.addAttribute("pplaxKill", pplaxKill);
        return "detail";
    }

    //ajax json
    @RequestMapping(value = "/exposer/{killId}",
            produces = {"application/json;charset=UTF-8"})
    @ResponseBody
    public PPLAXKillResult<Exposer> exposer(@PathVariable Long killId) {
        PPLAXKillResult<Exposer> result;
        try {
            Exposer exposer = pplaxKillService.exportKillUrl(killId);
            result = new PPLAXKillResult<Exposer>(true, exposer);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            result = new PPLAXKillResult<Exposer>(false, e.getMessage());
        }
        return result;
    }

    @RequestMapping(value = "/execution/{killId}/{phone}/{md5}",
            method = RequestMethod.POST,
            produces = {"application/json;charset=UTF-8"})
    @ResponseBody
    public PPLAXKillResult<PPLAXKillExecution> execute(@PathVariable("killId") Long killId,
                                                     @PathVariable("phone") Long phone,
                                                     @PathVariable("md5") String md5) {
        //springmvc valid
        if (phone == null) {
            return new PPLAXKillResult<PPLAXKillExecution>(false, "未注册");
        }
        PPLAXKillResult<PPLAXKillExecution> result;
        try {
            PPLAXKillExecution execution = pplaxKillService.executeKill(killId, phone, md5);
            return new PPLAXKillResult<PPLAXKillExecution>(true, execution);
        } catch (PPLAXKillException e1) {
            PPLAXKillExecution execution = new PPLAXKillExecution(killId, e1.getPPLAXKillStateEnum());
            return new PPLAXKillResult<PPLAXKillExecution>(true, execution);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            PPLAXKillExecution execution = new PPLAXKillExecution(killId, PPLAXKillStateEnum.INNER_ERROR);
            return new PPLAXKillResult<PPLAXKillExecution>(true, execution);
        }
    }

    @RequestMapping(value = "/time/now", method = RequestMethod.GET)
    @ResponseBody
    public PPLAXKillResult<Long> time() {
        Date now = new Date();
        return new PPLAXKillResult(true, now.getTime());
    }

    /**
     * @param killId
     * @param phone
     * @return 返回代码的含义0： 排队中; 1: 秒杀成功; 2： 秒杀失败
     * @TODO String boughtKey = RedisKeyPrefix.BOUGHT_USERS + killId
     * 还有一个redisKey存放已经入队列了的userPhone，   ENQUEUED_USER
     * 进队列的时候sadd ENQUEUED_USER , 消费成功的时候，sdel ENQUEUED_USER
     * 查询这个isGrab接口的时候，先查sismembles boughtKey, true则表明秒杀成功.
     * 否则，ismembles ENQUEUED_USER, 如果在队列中，说明排队中， 如果不在，说明秒杀失败
     */
    @RequestMapping(value = "/isGrab/{killId}/{phone}")
    @ResponseBody
    public String isGrab(@PathVariable("killId") Long killId,
                         @PathVariable("phone") Long phone) {
        int result = pplaxKillService.isGrab(killId, phone);
        return result + "";
    }
}
