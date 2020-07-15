package cn.lip.mybatis.rabbitmqsendercomponent;

import cn.lip.mybatis.bean.MessageContent;
import cn.lip.mybatis.dao.MsgContentMapper;
import cn.lip.mybatis.enumdef.MsgStatusEnum;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.support.CorrelationData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;


// 当前类的作用：判断消息是否发送到mq服务器成功，
// 如果成功记录表 message_content，不成功，也记录表 message_content；控制逻辑在方法：confirm 下面；
@Component
public class LpMsgComfirm implements RabbitTemplate.ConfirmCallback {

    @Autowired
    private MsgContentMapper msgContentMapper;

    @Override
    public void confirm(CorrelationData correlationData, boolean ack, String cause) {
        String msgId = correlationData.getId();
        if(ack) {
            //log.info("消息Id:{}对应的消息被broker签收成功",msgId);
            updateMsgStatusWithAck(msgId);
        }else{
            //log.warn("消息Id:{}对应的消息被broker签收失败:{}",msgId,cause);
            updateMsgStatusWithNack(msgId,cause);
        }
    }

    /**
     * 方法实现说明:更新消息表状态为
     * @param msgId:消息ID
     */
    private void updateMsgStatusWithAck(String msgId) {
        MessageContent messageContent = builderUpdateContent(msgId);
        //messageContent.setMsgStatus(MsgStatusEnum.SENDING_SUCCESS.getCode());
        messageContent.setMsgStatus(1);
        msgContentMapper.updateMsgStatus(messageContent);
    }

    private void updateMsgStatusWithNack(String msgId,String cause){

        MessageContent messageContent = builderUpdateContent(msgId);

        //messageContent.setMsgStatus(MsgStatusEnum.SENDING_FAIL.getCode());
        messageContent.setMsgStatus(2);
        messageContent.setErrCause(cause);
        msgContentMapper.updateMsgStatus(messageContent);
    }

    private MessageContent builderUpdateContent(String msgId) {
        MessageContent messageContent = new MessageContent();
        messageContent.setMsgId(msgId);
        messageContent.setUpdateTime(new Date());
        return messageContent;
    }
}
