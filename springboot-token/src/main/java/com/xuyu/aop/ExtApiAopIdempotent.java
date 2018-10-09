package com.xuyu.aop;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.xuyu.extAnnotation.ExtApiIdempotent;
import com.xuyu.extAnnotation.ExtApiToken;
import com.xuyu.utils.ConstantUtils;
import com.xuyu.utils.RedisToken;




public class ExtApiAopIdempotent {

	@Autowired
	private RedisToken redisToken;
	
	public void rlAop() {
	}
	@Pointcut("rlAop()")
	public void before(JoinPoint point) {
		MethodSignature  signature = (MethodSignature) point.getSignature();
		ExtApiToken extApiToken = signature.getMethod().getDeclaredAnnotation(ExtApiToken.class);
		if(extApiToken!=null) {
			// 可以放入到AOP代码 前置通知
			getRequest().setAttribute("token", redisToken.getToken());
		}
	}
	// 环绕通知
	@Around("rlAop()")
	public Object doBefore(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
		//2.判斷方法上是否加上ExtApiIdempotent註解
		MethodSignature  methodSignature  = (MethodSignature) proceedingJoinPoint.getSignature();
		ExtApiIdempotent declaredAnnotation = methodSignature.getMethod().getDeclaredAnnotation(ExtApiIdempotent.class);
		//3.如果方法上加上注解
		if(declaredAnnotation!=null) {
			String type = declaredAnnotation.type();
			// 如何使用Token 解决幂等性
			// 步骤：
			String token=null;
			HttpServletRequest request = getRequest();
			if(type.equals(ConstantUtils.EXTAPIHEAD)) {
				token = request.getHeader("token");
			}else {
				token = request.getParameter("token");
			}
			if(StringUtils.isEmpty(token)) {
				return "参数错误";
			}
			// 3.接口获取对应的令牌,如果能够获取该(从redis获取令牌)令牌(将当前令牌删除掉) 就直接执行该访问的业务逻辑
			boolean isToken = redisToken.findToken(token);
			if(!isToken) {
				response("请勿重复提交!");
				// 后面方法不在继续执行
				return null;
			}
		}
		//放行
		Object proceed = proceedingJoinPoint.proceed();
		return proceed;
	}

	public HttpServletRequest getRequest() {
		ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
		HttpServletRequest request = attributes.getRequest();
		return request;
	}

	public void response(String msg) throws IOException {
		ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
		HttpServletResponse response = attributes.getResponse();
		response.setHeader("Content-type", "text/html;charset=UTF-8");
		PrintWriter writer = response.getWriter();
		try {
			writer.println(msg);
		} catch (Exception e) {

		} finally {
			writer.close();
		}

	}
}
