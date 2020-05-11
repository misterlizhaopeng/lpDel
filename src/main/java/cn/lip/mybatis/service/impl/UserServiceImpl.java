package cn.lip.mybatis.service.impl;

import cn.lip.mybatis.bean.Student;
import cn.lip.mybatis.bean.TbUser;
import cn.lip.mybatis.dao.UserDao;
import cn.lip.mybatis.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserServiceImpl implements UserService {

	@Autowired
	private UserDao a;

	@Override
	public List<TbUser> getAll() {
		System.err.println("getAll-----------------------");
		return a.getUsers();
	}

	@Override
	public Student getStudentByIdAndName(Integer id, String name) {
		return a.getStudentByIdAndName(id, name);
	}

}
