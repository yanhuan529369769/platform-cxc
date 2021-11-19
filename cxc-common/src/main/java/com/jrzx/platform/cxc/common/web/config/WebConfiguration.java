package com.jrzx.platform.cxc.common.web.config;

import com.jr.platform.redis.core.RedisService;
import com.jr.platform.web.listener.RequestMappingScanListener;
import com.jr.platform.web.support.LoginUserArgumentResolver;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

/**
 * WEB配置类
 */
@Slf4j
@Configuration
@EnableWebMvc
@AllArgsConstructor
public class WebConfiguration implements WebMvcConfigurer {

	private final RedisService redisService;

	/**
	 * Token参数解析
	 *
	 * @param argumentResolvers 解析类
	 */
	@Override
	public void addArgumentResolvers(List<HandlerMethodArgumentResolver> argumentResolvers) {
		//注入用户信息
		argumentResolvers.add(new LoginUserArgumentResolver());
	}

	/**
	 * 资源扫描监听器类
	 *
	 * @return RequestMappingScanListener
	 */
	@Bean
	@ConditionalOnMissingBean(RequestMappingScanListener.class)
	public RequestMappingScanListener resourceAnnotationScan() {
		RequestMappingScanListener scan = new RequestMappingScanListener(redisService);
		log.info("资源扫描类.[{}]", scan);
		return scan;
	}
}
