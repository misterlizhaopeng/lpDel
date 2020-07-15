package cn.lip.mybatis.service.impl;

import cn.lip.mybatis.bean.MessageContent;
import cn.lip.mybatis.bean.MsgTxtBo;
import cn.lip.mybatis.bean.OrderInfo;
import cn.lip.mybatis.constant.MqConst;
import cn.lip.mybatis.dao.MsgContentMapper;
import cn.lip.mybatis.dao.OrderInfoMapper;
import cn.lip.mybatis.enumdef.MsgStatusEnum;
import cn.lip.mybatis.rabbitmqsendercomponent.MsgSender;
import cn.lip.mybatis.service.IOrderInfoService;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.UUID;

@Service
public class OrderInfoServiceImpl implements IOrderInfoService {
    @Autowired
    private OrderInfoMapper orderInfoMapper;

    @Autowired
    private MsgContentMapper msgContentMapper;

    @Autowired
    private MsgSender msgSender;

    @Transactional
    @Override
    public void saveOrderInfo(OrderInfo orderInfo,MessageContent messageContent) {

        try {
            orderInfoMapper.saveOrderInfo(orderInfo);

            //插入消息表
            msgContentMapper.saveMsgContent(messageContent);

        }catch (Exception e) {
            //log.error("操作数据库失败:{}",e);
            throw new RuntimeException("操作数据库失败");
        }
    }

    @Override
    public void saveOrderInfoWithMessage(OrderInfo orderInfo) throws JsonProcessingException {
        //构建消息对象
        MessageContent messageContent = builderMessageContent(orderInfo.getOrderNo(),orderInfo.getProductNo());

        //保存数据库(下订单、消息发送表)
        saveOrderInfo(orderInfo,messageContent);

        //构建消息发送对象
        MsgTxtBo msgTxtBo = new MsgTxtBo();
        msgTxtBo.setMsgId(messageContent.getMsgId());
        msgTxtBo.setOrderNo(orderInfo.getOrderNo());
        msgTxtBo.setProductNo(orderInfo.getProductNo());

        //发送消息
        msgSender.senderMsg(msgTxtBo);
    }

    /**
     * 方法实现说明:构建消息对象
     * @return:MessageContent 消息实体
     */
    private MessageContent builderMessageContent(long orderNo,Integer productNo) {
        MessageContent messageContent = new MessageContent();
        String msgId = UUID.randomUUID().toString();
        messageContent.setMsgId(msgId);
        messageContent.setCreateTime(new Date());
        messageContent.setUpdateTime(new Date());
        messageContent.setExchange(MqConst.ORDER_TO_PRODUCT_EXCHANGE_NAME);
        messageContent.setRoutingKey(MqConst.ORDER_TO_PRODUCT_QUEUE_NAME);
        //messageContent.setMsgStatus(MsgStatusEnum.SENDING.getCode());
        messageContent.setMsgStatus(0);
        messageContent.setOrderNo(orderNo);
        messageContent.setProductNo(productNo);
        messageContent.setMaxRetry(MqConst.MSG_RETRY_COUNT);
        return messageContent;
    }
}
