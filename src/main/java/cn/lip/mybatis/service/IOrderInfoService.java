package cn.lip.mybatis.service;

import cn.lip.mybatis.bean.MessageContent;
import cn.lip.mybatis.bean.OrderInfo;
import com.fasterxml.jackson.core.JsonProcessingException;

public interface IOrderInfoService {

    /**
     * 方法实现说明:订单保存
     *
     * @author:smlz
     * @param orderInfo:订单实体
     */
    void saveOrderInfo(OrderInfo orderInfo, MessageContent messageContent);

    void saveOrderInfoWithMessage(OrderInfo orderInfo) throws JsonProcessingException;
}
