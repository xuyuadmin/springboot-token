package com.xuyu.utils;

import java.util.UUID;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class RedisToken {

	@Autowired
	private BaseRedisService baseRedisService;
	private static final long TOKENTIMEOUT=60*60;
	//1.在调用接口之前生成对应的令牌token，存放在redis
	public String getToken() {
		//生成token 规则保证 临时唯一 不支持分布式场景 分布式全局id生成规则
		String token="token"+UUID.randomUUID();
		//如何保证token临时（缓存）使用redis 实现缓存
		baseRedisService.setString(token,token,TOKENTIMEOUT);
		return token;
		
	}
	
	
	//在调用接口的时候，将该令牌放入到请求头中
	
	//接口获取对应的令牌，获取不到对应的令牌，直接返回提示请勿重复提交
	public synchronized boolean findToken(String tokenKey) {
		//2.接口获取到对应的令牌。获取到则删除令牌，执行业务逻辑
		String tokenValue = (String)baseRedisService.getString(tokenKey);
		if(StringUtils.isEmpty(tokenValue)) {
			return false;
		}
		//保证每个接口对应的token，只能访问一次，保证接口幂等性
		baseRedisService.delKey(tokenValue);
		return true;
	}
}
