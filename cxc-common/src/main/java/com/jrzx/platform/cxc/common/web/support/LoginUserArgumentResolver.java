package com.jrzx.platform.cxc.common.web.support;

import com.jr.platform.core.entity.LoginUser;
import com.jr.platform.security.util.SecurityUtil;
import com.jr.platform.web.annotation.EnableUser;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import javax.servlet.http.HttpServletRequest;

/**
 * 通过header里的token获取用户信息
 */
@Slf4j
@AllArgsConstructor
public class LoginUserArgumentResolver implements HandlerMethodArgumentResolver {

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        boolean isHasEnableUserAnn = parameter.hasParameterAnnotation(EnableUser.class);
        boolean isHasLoginUserParameter = parameter.getParameterType().isAssignableFrom(LoginUser.class);
        return isHasEnableUserAnn && isHasLoginUserParameter;
    }

    @Override
    public Object resolveArgument(MethodParameter methodParameter, ModelAndViewContainer modelAndViewContainer, NativeWebRequest nativeWebRequest, WebDataBinderFactory webDataBinderFactory) throws Exception {
        EnableUser user = methodParameter.getParameterAnnotation(EnableUser.class);
        boolean value= user.value();
        HttpServletRequest request = nativeWebRequest.getNativeRequest(HttpServletRequest.class);
        LoginUser loginUser = SecurityUtil.getUser(request);
        /**
         * 根据value状态获取更多用户信息，待实现
         */
        return loginUser;
    }
}
