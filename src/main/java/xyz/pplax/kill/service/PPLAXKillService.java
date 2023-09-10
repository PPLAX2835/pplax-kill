package xyz.pplax.kill.service;


import org.springframework.stereotype.Service;
import xyz.pplax.kill.dto.Exposer;
import xyz.pplax.kill.dto.PPLAXKillExecution;
import xyz.pplax.kill.entity.PPLAXKill;
import xyz.pplax.kill.exception.PPLAXKillException;

import java.util.List;

public interface PPLAXKillService {

    /**
     * 查询所有秒杀记录
     *
     * @return
     */
    List<PPLAXKill> getKillList();

    /**
     * 查询单个秒杀记录
     * @param killId
     * @return
     */
    PPLAXKill getById(long killId);

    /**
     * 秒杀开启输出秒杀接口地址,
     * 否则输出系统时间和秒杀时间
     * @param killId
     * @return
     */
    Exposer exportKillUrl(long killId);

    /**
     * 执行秒杀操作
     * @param killId
     * @param userPhone
     * @param md5
     * @return
     * @throws PPLAXKillException
     */
    PPLAXKillExecution executeKill(long killId, long userPhone, String md5) throws PPLAXKillException;

    /**
     * 更新库存
     * @param killId
     * @param userPhone
     * @return
     * @throws PPLAXKillException
     */
    PPLAXKillExecution updateInventory(long killId, long userPhone) throws PPLAXKillException;

    /**
     * 在Redis中真正进行秒杀操作
     * @param killId
     * @param userPhone
     * @throws PPLAXKillException
     */
    void handleInRedis(long killId, long userPhone) throws PPLAXKillException;

    /**
     * 查看是否秒杀成功
     * @param killId
     * @param userPhone
     * @return
     */
    public int isGrab(long killId, long userPhone);
}