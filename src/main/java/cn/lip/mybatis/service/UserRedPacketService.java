package cn.lip.mybatis.service;

public interface UserRedPacketService {
	
	/**
	 * 保存抢红包信息.
	 * @param redPacketId 红包编号
	 * @param userId 抢红包用户编号
	 * @return 影响记录数.
	 */
	public int grabRedPacket(Long redPacketId, Long userId);


	public int grabRedPacketForVersion(Long redPacketId, Long userId);
	public int grabRedPacketForVersionContinueByTimestamp(Long redPacketId, Long userId);
	public int grabRedPacketForVersionContinueByTimes(Long redPacketId, Long userId);
}
