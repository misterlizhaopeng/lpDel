package cn.lip.mybatis.consumer;

import cn.lip.mybatis.bean.MessageContent;
import cn.lip.mybatis.bean.MsgTxtBo;
import cn.lip.mybatis.constant.MqConst;
import cn.lip.mybatis.dao.MsgContentMapper;
import cn.lip.mybatis.enumdef.MsgStatusEnum;
import cn.lip.mybatis.excep.BizExp;
import cn.lip.mybatis.service.IProductService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Date;

@Component
public class ProductDefiConsumer {
    /**
     * 队列名称
     */
    public static final String ORDER_TO_PRODUCT_QUEUE_NAME = "order-to-product.queue";

    public static final String DIS_LOCK_KEY = "LOCK_KEY";

    @Autowired
    private IProductService productService;

    @Autowired
    private MsgContentMapper msgContentMapper;

    @Autowired
    private RedisTemplate redisTemplate;


    /**
     * 没有加分布式锁的版本,可能存在重复消费问题
     *
     * @param message
     * @param channel
     * @throws IOException
     */
  /*  @RabbitListener(queues = {ORDER_TO_PRODUCT_QUEUE_NAME})
    @RabbitHandler */

    public void consumerMsg(Message message, Channel channel) throws IOException {

        ObjectMapper objectMapper = new ObjectMapper();
        MsgTxtBo msgTxtBo = objectMapper.readValue(message.getBody(), MsgTxtBo.class);

        //log.info("消费消息:{}",msgTxtBo);
        System.out.println("消费消息:" + msgTxtBo);
        Long deliveryTag = (Long) message.getMessageProperties().getDeliveryTag();

        try {
            //更新消息表也业务表
            productService.updateProductStore(msgTxtBo);

            //消息签收
            channel.basicAck(deliveryTag, false);
        } catch (Exception e) {
            //更新msg表为消费失败
            //更新消息表状态
            MessageContent messageContent = new MessageContent();
            messageContent.setMsgId(msgTxtBo.getMsgId());
            messageContent.setUpdateTime(new Date());
            //messageContent.setMsgStatus(MsgStatusEnum.CONSUMER_FAIL.getCode());
            messageContent.setMsgStatus(4);
            msgContentMapper.updateMsgStatus(messageContent);

            channel.basicReject(deliveryTag, false);
        }
    }



  //消费者进行消费消息
    //@RabbitListener(queues = {ORDER_TO_PRODUCT_QUEUE_NAME})
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = MqConst.ORDER_TO_PRODUCT_QUEUE_NAME, durable = "true"),
            exchange = @Exchange(value = MqConst.ORDER_TO_PRODUCT_EXCHANGE_NAME, durable = "true", type = ExchangeTypes.DIRECT),
            key = MqConst.ORDER_TO_PRODUCT_ROUTING_KEY
    ))
    @RabbitHandler
    public void consumerMsgWithLock(Message message, Channel channel) throws IOException {

        ObjectMapper objectMapper = new ObjectMapper();
        MsgTxtBo msgTxtBo = objectMapper.readValue(message.getBody(), MsgTxtBo.class);
        Long deliveryTag = (Long) message.getMessageProperties().getDeliveryTag();

        String lock_Key=DIS_LOCK_KEY + msgTxtBo.getMsgId();
        //为什么在此处加分布式锁：
        // 就是业务逻辑异常（BizExp）或者 因为网络抖动，此处的代码表示这两种情况都会向消息表中进行一条更新异常操作，让定时器重新扫描，
        // 直到五次完成（五次为认为设置，也可以设置别的次数），停止扫描，为了保证幂等性，此方法通过了redis 分布式锁实现了幂等性；
        if (redisTemplate.opsForValue().setIfAbsent(lock_Key, msgTxtBo.getMsgId())) {
            //log.info("消费消息:{}", msgTxtBo);
            System.out.println("消费消息:" + msgTxtBo);
            try {
                //更新消息表、业务表
                productService.updateProductStore(msgTxtBo);
                //消息签收
                //System.out.println(1 / 0);// 模拟网络抖动
                channel.basicAck(deliveryTag, false);//确认收到消息
            } catch (Exception e) {
                //更新数据库异常说明业务没有操作成功需要删除分布式锁
                if (e instanceof BizExp) {
                    BizExp bizExp = (BizExp) e;
                    //log.info("数据业务异常:{},即将删除分布式锁", bizExp.getErrMsg());
                    System.out.println("数据业务异常:" + bizExp.getErrMsg() + ",即将删除分布式锁");
                    //删除分布式锁
                    redisTemplate.delete(lock_Key);
                }

                //更新消息表状态
                MessageContent messageContent = new MessageContent();
                messageContent.setMsgStatus(MsgStatusEnum.CONSUMER_FAIL.getCode());
                messageContent.setUpdateTime(new Date());
                messageContent.setErrCause(e.getMessage());
                messageContent.setMsgId(msgTxtBo.getMsgId());
                msgContentMapper.updateMsgStatus(messageContent);
                channel.basicReject(deliveryTag, false);//拒收消息，并且消息不回队列，意思是消息将会被丢弃
            }

        } else {
            //log.warn("请不要重复消费消息{}", msgTxtBo);
            System.out.println("请不要重复消费消息" + msgTxtBo);
            channel.basicReject(deliveryTag, false);//拒收消息，并且消息不回队列，意思是消息将会被丢弃
        }

    }


}
