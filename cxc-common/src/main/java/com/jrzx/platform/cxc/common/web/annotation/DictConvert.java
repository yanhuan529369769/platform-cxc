package com.jrzx.platform.cxc.common.web.annotation;

import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface DictConvert {
    //TODO 抽取到上层common  同时添加系统和字典权限等细分项
    /**
     * 字典类型 dict_type 如果是 List 则无需指定 该值
     */
    String value() default "";

    /**
     * 填写该字段后 该字段会拿到 refField 配置的字段的值并且根据字典转换
     */
    String refField() default "";

    /**
     * 如果转换的值不匹配是否要转换为NULL
     */
    boolean ifNotMatchConvertToNull() default false;

    /**
     * 带逗号的多字典 分隔符 默认逗号
     */
    String delimiter() default ",";
    /**
     * 指定字典的转换数据（若指定则以当前字典数据为准）
     * eg: 0:女,1:男,2:未知
     */
    String dicts() default "";
}
