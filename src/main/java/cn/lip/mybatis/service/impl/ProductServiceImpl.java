package cn.lip.mybatis.service.impl;

import cn.lip.mybatis.bean.MessageContent;
import cn.lip.mybatis.bean.MsgTxtBo;
import cn.lip.mybatis.dao.MsgContentMapper;
import cn.lip.mybatis.dao.ProductInfoMapper;
import cn.lip.mybatis.enumdef.MsgStatusEnum;
import cn.lip.mybatis.excep.BizExp;
import cn.lip.mybatis.service.IProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

@Service
public class ProductServiceImpl implements IProductService {

    @Autowired
    private ProductInfoMapper productInfoMapper;

    @Autowired
    private MsgContentMapper msgContentMapper;

    @Transactional
    @Override
    public boolean updateProductStore(MsgTxtBo msgTxtBo) {
        boolean updateFlag = true;
        try{
            //更新库存
            productInfoMapper.updateProductStoreById(msgTxtBo.getProductNo());

            //更新消息表状态
            MessageContent messageContent = new MessageContent();
            messageContent.setMsgId(msgTxtBo.getMsgId());
            messageContent.setUpdateTime(new Date());
            //messageContent.setMsgStatus(MsgStatusEnum.CONSUMER_SUCCESS.getCode());
            messageContent.setMsgStatus(3);
            msgContentMapper.updateMsgStatus(messageContent);

            //System.out.println(1/0);
        }catch (Exception e) {
            //log.error("更新数据库失败:{}",e);
            System.out.println("更新数据库失败:"+e);
            throw new BizExp(0,"更新数据库异常");
        }
        return updateFlag;
    }
}
