package xyz.pplax.kill.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import xyz.pplax.kill.entity.PPLAXKill;

import java.util.List;

@Mapper
public interface PPLAXKillMapper {

    /**
     * 减库存
     * 如果影响行数>1，表示更新的记录行数
     * @param killId
     * @param oldVersion
     * @param newVersion
     * @return
     */
    int reduceInventory(@Param("killId") long killId, @Param("oldVersion") long oldVersion,
                        @Param("newVersion") long newVersion);

    /**
     * 根据id查询秒杀对象
     *
     * @param killId
     * @return
     */
    PPLAXKill queryById(long killId);

    /**
     * 根据偏移量查询秒杀商品列表，说白了就是分页
     *
     * @param offet
     * @param limit
     * @return
     */
    List<PPLAXKill> queryAll(@Param("offset") int offet, @Param("limit") int limit);


}
