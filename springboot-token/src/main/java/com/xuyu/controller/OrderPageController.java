package com.xuyu.controller;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.xuyu.entity.OrderEntity;
import com.xuyu.extAnnotation.ExtApiIdempotent;
import com.xuyu.extAnnotation.ExtApiToken;
import com.xuyu.mapper.OrderMapper;
import com.xuyu.utils.ConstantUtils;
import com.xuyu.utils.RedisToken;

@Controller
public class OrderPageController {
	@Autowired
	private OrderMapper orderMapper;
	@Autowired
	private RedisToken redisToken;

	@RequestMapping("/indexPage")
	@ExtApiToken
	public String indexPage(HttpServletRequest req) {
		return "indexPage";
	}

	@RequestMapping("/addOrderPage")
	@ExtApiIdempotent(type = ConstantUtils.EXTAPIFORM)
	public String addOrder(OrderEntity orderEntity) {
		int addOrder = orderMapper.addOrder(orderEntity);
		return addOrder > 0 ? "success" : "fail";
	}

}
