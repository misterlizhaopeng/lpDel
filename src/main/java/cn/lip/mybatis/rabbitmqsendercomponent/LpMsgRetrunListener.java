package cn.lip.mybatis.rabbitmqsendercomponent;

import cn.lip.mybatis.bean.MessageContent;
import cn.lip.mybatis.bean.MsgTxtBo;
import cn.lip.mybatis.dao.MsgContentMapper;
import cn.lip.mybatis.enumdef.MsgStatusEnum;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;




@Component
public class LpMsgRetrunListener implements RabbitTemplate.ReturnCallback {

    @Autowired
    private MsgContentMapper msgContentMapper;

    @Override
    public void returnedMessage(Message message, int replyCode, String replyText, String exchange, String routingKey) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            MsgTxtBo msgTxtBo = objectMapper.readValue(message.getBody(), MsgTxtBo.class);
            //log.info("无法路由消息内容:{},cause:{}",msgTxtBo,replyText);

            // 构建消息对象
            MessageContent messageContent = new MessageContent();
            messageContent.setErrCause(replyText);
            messageContent.setUpdateTime(new Date());
            //messageContent.setMsgStatus(MsgStatusEnum.SENDING_FAIL.getCode());
            messageContent.setMsgStatus(2);
            messageContent.setMsgId(msgTxtBo.getMsgId());
            // 更新消息表
            msgContentMapper.updateMsgStatus(messageContent);
        } catch (Exception e) {
            //log.error("更新消息表异常:{}",e);
        }
    }
}
