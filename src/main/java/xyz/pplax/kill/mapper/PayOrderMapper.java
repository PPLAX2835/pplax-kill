package xyz.pplax.kill.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import xyz.pplax.kill.entity.PayOrder;

import java.util.Date;

@Mapper
public interface PayOrderMapper {

    /**
     * 插入购买明细,可过滤重复
     * @param killId
     * @param userPhone
     * @return
     * 插入的行数
     */
    int insertPayOrder(@Param("killId") long killId, @Param("userPhone") long userPhone,
                       @Param("nowTime") Date nowTime);

    /**
     * 根据id查询SuccessKilled并携带秒杀产品对象实体
     * @param killId
     * @return
     */
    PayOrder queryByIdWithkillId(@Param("killId") long killId, @Param("userPhone") long userPhone);

}
