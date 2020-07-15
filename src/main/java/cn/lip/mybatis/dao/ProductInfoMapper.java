package cn.lip.mybatis.dao;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ProductInfoMapper {

    int updateProductStoreById(Integer productId);

}


