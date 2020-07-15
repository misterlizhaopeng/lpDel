package cn.lip.mybatis.dao;

import cn.lip.mybatis.bean.Dept;
import org.apache.ibatis.annotations.Mapper;


@Mapper
public interface DeptDao {
	void addDept(Dept dept);
}