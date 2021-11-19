package com.jrzx.platform.cxc.common.database.handler;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.jr.platform.core.entity.LoginUser;
import com.jr.platform.service.finance.api.FinanceConstants;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;

/**
 * 自动填充时间字段
 */
@Component
@Slf4j
public class JrMetaObjectHandler implements MetaObjectHandler {

    @Override
    public void insertFill(MetaObject metaObject) {
        this.strictInsertFill(metaObject, "createTime", LocalDateTime.class, LocalDateTime.now());
        this.strictInsertFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());
        if (null == getLoginUser()) {
            this.strictInsertFill(metaObject, "createBy", String.class, "system");
            this.strictInsertFill(metaObject, "updateBy", String.class, "system");
        } else {
            this.strictInsertFill(metaObject, "createBy", String.class, getLoginUser().getUserName());
            this.strictInsertFill(metaObject, "updateBy", String.class, getLoginUser().getUserName());
        }
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        this.strictUpdateFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());
        if (null == getLoginUser()) {
            this.strictInsertFill(metaObject, "update_by", String.class, "system");
        } else {
            this.strictInsertFill(metaObject, "update_by", String.class, getLoginUser().getUserName());
        }
    }


    /**
     * 获取当前登录人
     *
     * @return
     */
    private LoginUser getLoginUser() {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        String token = request.getHeader(FinanceConstants.JR_AUTH_TOKEN_HEADER);
        log.info("当前登录人token：-》{}", token);
        if (StrUtil.isBlank(token)) {
            return null;
        }
        LoginUser loginUser = com.jr.platform.security.util.SecurityUtil.getUser(token);
        log.info("当前登录人：-》{}", JSON.toJSONString(loginUser));
        return loginUser;
    }

}
