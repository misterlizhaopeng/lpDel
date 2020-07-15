package cn.lip.mybatis.dao;

import cn.lip.mybatis.bean.OrderInfo;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface OrderInfoMapper {

    /**
     * 方法实现说明:订单保存
     * @param orderInfo:订单实体
     * @return: int 插入的条数
     */
    int saveOrderInfo(OrderInfo orderInfo);
}
