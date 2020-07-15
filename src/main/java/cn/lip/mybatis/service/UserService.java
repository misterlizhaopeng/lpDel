package cn.lip.mybatis.service;

import java.util.List;

import cn.lip.mybatis.bean.Student;
import cn.lip.mybatis.bean.TbUser;

public interface UserService {
	List<TbUser> getAll();
	Student getStudentByIdAndName(Integer id, String name) throws Exception;
	Student getStudentByIdAndName_lockByRedisson(Integer id, String name);
	void addStudent(Student student);
}
