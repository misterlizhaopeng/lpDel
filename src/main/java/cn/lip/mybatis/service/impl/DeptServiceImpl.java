package cn.lip.mybatis.service.impl;

import cn.lip.mybatis.bean.Dept;
import cn.lip.mybatis.dao.DeptDao;
import cn.lip.mybatis.service.DeptService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;


@Service
public class DeptServiceImpl implements DeptService {


    @Autowired
    private DeptDao deptDao;


    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW )
    public void addDept(Dept dept) {
        try {
            int i = 1 / 0;
            System.out.println("-------------------------------------> xxxxxxxxxxxxxxxxxxxx");
        } catch (Exception e) {
            // 异常的情况下，当前写数据也不能成功；
            deptDao.addDept(dept);
            throw new RuntimeException(e.toString());
        }

    }
}
