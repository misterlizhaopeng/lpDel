package cn.lip.mybatis.dao;

import cn.lip.mybatis.bean.Student;
import cn.lip.mybatis.bean.TbUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;


@Mapper
public interface UserDao {
	List<TbUser> getUsers();
	public Student getStudentByIdAndName(@Param("id") int id, @Param("name") String name);
}