package cn.lip.mybatis.bean;

import java.io.Serializable;

public class Dept implements Serializable {
    private Long deptno;
    private String dname;
    private String dbsource;

    public Long getDeptno() {
        return deptno;
    }

    public void setDeptno(Long deptno) {
        this.deptno = deptno;
    }

    public String getDname() {
        return dname;
    }

    public void setDname(String dname) {
        this.dname = dname;
    }

    public String getDbsource() {
        return dbsource;
    }

    public void setDbsource(String dbsource) {
        this.dbsource = dbsource;
    }
}
