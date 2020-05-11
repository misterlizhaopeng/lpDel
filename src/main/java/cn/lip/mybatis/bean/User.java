package cn.lip.mybatis.bean;

import org.hibernate.validator.constraints.NotBlank;

import java.io.Serializable;

import javax.validation.constraints.NotNull;

public class User implements Serializable {
	
	private Integer id;
	@NotNull(message = "年龄不能为空")
	private Integer age;
	@NotBlank(message = "姓名不能为空")
	private String name;
	
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	public Integer getAge() {
		return age;
	}
	public void setAge(Integer age) {
		this.age = age;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}

}
