package cn.lip.mybatis.rabbitmqsendercomponent;

import cn.lip.mybatis.bean.MsgTxtBo;
import cn.lip.mybatis.constant.MqConst;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.support.CorrelationData;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class MsgSender implements InitializingBean {
    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private LpMsgComfirm lpMsgComfirm;

    @Autowired
    private LpMsgRetrunListener lpMsgRetrunListener;

    @Override
    public void afterPropertiesSet() throws Exception {
        rabbitTemplate.setConfirmCallback(lpMsgComfirm);
        rabbitTemplate.setReturnCallback(lpMsgRetrunListener);
        //设置消息转换器
        Jackson2JsonMessageConverter jackson2JsonMessageConverter = new Jackson2JsonMessageConverter();
        rabbitTemplate.setMessageConverter(jackson2JsonMessageConverter);
    }


    /**
     * 方法实现说明:真正的发送消息
     *
     * @param msgTxtBo:发送的消息对象
     */
    public void senderMsg(MsgTxtBo msgTxtBo) {

        //log.info("发送的消息ID:{}",msgTxtBo.getMsgId());

        CorrelationData correlationData = new CorrelationData(msgTxtBo.getMsgId());

        rabbitTemplate.convertAndSend(MqConst.ORDER_TO_PRODUCT_EXCHANGE_NAME, MqConst.ORDER_TO_PRODUCT_ROUTING_KEY, msgTxtBo, correlationData);
    }


}
