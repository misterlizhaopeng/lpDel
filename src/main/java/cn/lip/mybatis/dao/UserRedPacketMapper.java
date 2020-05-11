package cn.lip.mybatis.dao;

import cn.lip.mybatis.bean.UserRedPacket;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserRedPacketMapper {
    /**
     * 插入抢红包信息.
     * @param userRedPacket ——抢红包信息
     * @return 影响记录数.
     */
    public int grabRedPacket(UserRedPacket userRedPacket);
}
