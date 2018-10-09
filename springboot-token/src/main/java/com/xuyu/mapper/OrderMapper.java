package com.xuyu.mapper;

import org.apache.ibatis.annotations.Insert;

import com.xuyu.entity.OrderEntity;


public interface OrderMapper {
	@Insert("insert order_info values (null,#{orderName},#{orderDes})")
	public int addOrder(OrderEntity OrderEntity);
}
