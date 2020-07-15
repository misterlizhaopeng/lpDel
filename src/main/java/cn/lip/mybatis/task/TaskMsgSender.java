package cn.lip.mybatis.task;

import cn.lip.mybatis.bean.MessageContent;
import cn.lip.mybatis.bean.MsgTxtBo;
import cn.lip.mybatis.constant.MqConst;
import cn.lip.mybatis.dao.MsgContentMapper;
import cn.lip.mybatis.enumdef.MsgStatusEnum;
import cn.lip.mybatis.rabbitmqsendercomponent.MsgSender;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class TaskMsgSender {
    @Autowired
    private MsgSender msgSender;

    @Autowired
    private MsgContentMapper msgContentMapper;


    /**
     * initialDelay: 延时5s启动
     * fixedDelay: 周期10S一次，表示任务执行之间的时间间隔，具体是指本次任务结束到下次任务开始之间的时间间隔；
     */
    @Scheduled(initialDelay = 5000, fixedDelay = 10000)
    public void retrySend() {
        System.out.println("-----------------------------");
        // 遍历 查找大于30s没有成功发送的记录；
        //List<MessageContent> messageContentList = msgContentMapper.qryNeedRetryMsg(MsgStatusEnum.CONSUMER_SUCCESS.getCode(), MqConst.TIME_DIFF);
        List<MessageContent> messageContentList = msgContentMapper.qryNeedRetryMsg(3, MqConst.TIME_DIFF);

        for (MessageContent messageContent : messageContentList) {
            if (messageContent != null && (messageContent.getMaxRetry() > messageContent.getCurrentRetry())) {
                MsgTxtBo msgTxtBo = new MsgTxtBo();
                msgTxtBo.setMsgId(messageContent.getMsgId());
                msgTxtBo.setProductNo(messageContent.getProductNo());
                msgTxtBo.setOrderNo(messageContent.getOrderNo());
                // 更新消息重试次数
                msgContentMapper.updateMsgRetryCount(msgTxtBo.getMsgId());
                msgSender.senderMsg(msgTxtBo);
            } else {
                //log.warn("消息:{}以及达到最大重试次数",messageContent);
                System.out.println("消息:" + messageContent + "以及达到最大重试次数");
            }

        }
    }
}
