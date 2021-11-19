package com.jrzx.platform.cxc.common.web.dict;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.ClassUtil;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import com.jr.platform.web.annotation.DictConvert;
import lombok.Builder;
import lombok.Data;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DictConvertUtil {
    public static <T> void convertToCode(T data) {
        convertToCode(data, null);
    }

    /**
     * 男 to 1
     *
     * @param data     需要转换的对象
     * @param consumer 转换后做一些事情
     */
    public static <T> void convertToCode(T data, Consumer<T> consumer) {
        if (Objects.isNull(data)) {
            return;
        }
        convert(data, true);
        if (consumer != null) {
            consumer.accept(data);
        }
    }

    public static <T> void convertToCodeList(List<T> data) {
        convertToCodeList(data, null);
    }

    /**
     * 男 to 1
     *
     * @param data     需要转换的对象
     * @param consumer 转换后做一些事情
     */
    public static <T> void convertToCodeList(List<T> data, Consumer<T> consumer) {
        if (Objects.isNull(data) || CollUtil.isEmpty(data)) {
            return;
        }
        data.parallelStream().forEach(d -> {
            convert(d, true);
            if (consumer != null) {
                consumer.accept(d);
            }
        });
    }

    public static <T> void convertToDictionary(T data) {
        convertToDictionary(data, null);
    }

    /**
     * 1 to 男
     *
     * @param data     需要转换的对象
     * @param consumer 转换后做一些事情
     */
    public static <T> void convertToDictionary(T data, Consumer<T> consumer) {
        if (Objects.isNull(data)) {
            return;
        }
        convert(data, false);
        if (consumer != null) {
            consumer.accept(data);
        }
    }

    public static <T> void convertToDictionaryList(List<T> data) {
        convertToDictionaryList(data, null);
    }

    /**
     * 1 to 男
     *
     * @param data     需要转换的对象
     * @param consumer 转换后做一些事情
     */
    public static <T> void convertToDictionaryList(List<T> data, Consumer<T> consumer) {
        if (Objects.isNull(data) || CollUtil.isEmpty(data)) {
            return;
        }
        data.parallelStream().forEach(d -> {
            convert(d, false);
            if (consumer != null) {
                consumer.accept(d);
            }
        });
    }

    /**
     * 转换字典中的值
     *
     * @param data     需要转换的对象
     * @param isToCode {
     *                 eg:      男-1、女-0
     *                 true:    则将男转为1
     *                 false:   则将1转为男
     *                 }
     */
    private static <T> void convert(T data, boolean isToCode) {

        // 获取当前类的所有字段
        Field[] fields = data.getClass().getDeclaredFields();

        // 过滤 static、 final、private static final字段
        final List<Field> filteredFields = Stream.of(fields).filter(f -> !(f.getModifiers() == Modifier.FINAL
                || f.getModifiers() == Modifier.STATIC
                || f.getModifiers() == (Modifier.PRIVATE | Modifier.STATIC | Modifier.FINAL)
                || f.getAnnotation(DictConvert.class) == null)).collect(Collectors.toList());

        // 处理
        for (Field f : filteredFields) {

            // 获取当前字段注解
            DictConvert annotation = f.getAnnotation(DictConvert.class);

            // 没有加注解的字段不处理
            if (annotation == null) {
                continue;
            }

            // 反射获取字段值
            Object value;

            // 字典类型
            String dictType = annotation.value();

            // 如果是引用其他字段则值从其他字段取
            if (StrUtil.isNotEmpty(annotation.refField())) {
                value = ReflectUtil.getFieldValue(data, annotation.refField());
            }

            // 否则获取当前字段值
            else {
                value = ReflectUtil.getFieldValue(data, f);
            }

            // 转换字典时字段值为空 不进行后续处理
            if (value == null) {
                continue;
            }

            // 类型
            final Class<?> classType = value.getClass();

            // 如果不是基本类型
            if (!ClassUtil.isBasicType(classType) && classType != String.class) {
                // 是List 循环则递归调用
                if (value instanceof List) {
                    for (Object o : (List) value) {
                        convert(o, isToCode);
                    }
                }
                // 不是 List 则视为对象反射调用
                else {
                    convert(value, isToCode);
                }
            }

            // 自定义的字典
            final String dicts = annotation.dicts();

            // 转换字典时字段字典类型未配置（字典key都不配置转个毛线）
            if (StrUtil.isEmpty(dictType) && StrUtil.isEmpty(dicts)) {
                continue;
            }

            // 获取字典的对应 映射关系 （建议此处做缓存提高转换速度）
            final List<Dict> currDictList;

            if (StrUtil.isNotBlank(dicts)) {
                final List<String> dictList = StrUtil.splitTrim(dicts, ",");
                currDictList = Optional.ofNullable(dictList)
                        .filter(CollUtil::isNotEmpty)
                        .map(s -> s.parallelStream().map(d -> {
                            final List<String> dTrim = StrUtil.splitTrim(d, ":");
                            return dTrim.size() == 2 ? Dict.builder().dictValue(dTrim.get(0)).dictLabel(dTrim.get(1)).build() : null;
                        }).filter(Objects::nonNull).collect(Collectors.toList())).orElse(new ArrayList<>(0));

            } else {
                currDictList = getDictByDictType(dictType);
            }

            // 是否匹配到了字典中的值
            boolean isMatchSuccess = false;

            // 获取当前字典值
            final String beanValue = Convert.toStr(value);

            // 支持类似 , 逗号隔开的字典转换, 如果需要支持其他 DictConvert#delimiter() 可在此设置
            // eg : 兴趣爱好 （足球,篮球,奥利给）
            // 转换后则为 （football,basketball,aoligei）
            final String delimiter = annotation.delimiter();

            final List<String> beanValues = StrUtil.splitTrim(beanValue, delimiter);

            // 1 to 男
            if (!isToCode) {
                // 逗号隔开字典转换支持
                if (CollUtil.isNotEmpty(beanValues) && beanValues.size() > 1) {
                    final Map<String, String> dictMap = currDictList.stream().collect(Collectors.toMap(Dict::getDictValue, Dict::getDictLabel));
                    final List<String> matchesDict = beanValues.stream()
                            .filter(dictMap::containsKey)
                            .map(dm -> Objects.nonNull(dictMap.get(dm)) ? dictMap.get(dm) : "")
                            .collect(Collectors.toList());
                    if (CollUtil.isNotEmpty(matchesDict)) {
                        isMatchSuccess = true;
                        ReflectUtil.setFieldValue(data, f, CollUtil.join(matchesDict, delimiter));
                    }
                } else {
                    for (Dict sysDictData : currDictList) {
                        if (Objects.equals(Convert.toStr(value), sysDictData.getDictValue())) {
                            ReflectUtil.setFieldValue(data, f, Objects.nonNull(sysDictData.getDictLabel()) ? sysDictData.getDictLabel() : value);
                            isMatchSuccess = true;
                            break;
                        }
                    }
                }
            }
            // 男 to 1
            else {
                // 逗号隔开字典转换支持
                if (CollUtil.isNotEmpty(beanValues) && beanValues.size() > 1) {
                    final Map<String, String> dictMap = currDictList.stream().collect(Collectors.toMap(Dict::getDictLabel, Dict::getDictValue));
                    final List<String> matchesDict = beanValues.stream()
                            .filter(dictMap::containsKey)
                            .map(dm -> Objects.nonNull(dictMap.get(dm)) ? dictMap.get(dm) : "")
                            .collect(Collectors.toList());
                    if (CollUtil.isNotEmpty(matchesDict)) {
                        isMatchSuccess = true;
                        ReflectUtil.setFieldValue(data, f, CollUtil.join(matchesDict, delimiter));
                    }
                } else {
                    for (Dict sysDictData : currDictList) {
                        if (Objects.equals(Convert.toStr(value), sysDictData.getDictLabel())) {
                            ReflectUtil.setFieldValue(data, f, Objects.nonNull(sysDictData.getDictValue()) ? sysDictData.getDictValue() : value);
                            isMatchSuccess = true;
                            break;
                        }
                    }
                }
            }

            // 如果匹配不到字典中的值 且 字段中明确表示如果匹配不到就置为NULL
            if (!isMatchSuccess && annotation.ifNotMatchConvertToNull()) {
                ReflectUtil.setFieldValue(data, f, null);
            }

        }
    }


/************************************** 字典数据源获取开始   ******************************************/
    /**
     * 根据字典类型查询所有该类型的字典信息
     * 可以在此处添加缓存（加快转换速度）
     * eg:使用 spring cache
     *
     * @param dictType 字典类型
     * @return 匹配到的字典集合
     * @Cacheable(value = "dictConvert",key = "#dictType")
     * public static List<Dict> getDictByDictType(String dictType) {
     * return dictData;
     * }
     */
    private static List<Dict> getDictByDictType(String dictType) {

//        DictService dictService= ManageSpringBeans.getBean(DictService.class);
//
//        return dictService.listDictByType(dictType);
        return null;

    }

    /************************************** 字典数据源获取开始   ******************************************/

    @Data
    @Builder
    public static class Dict {
        /**
         * dict type
         * eg: sex
         */
        private String dictType;

        /**
         * dict label
         * eg: 男
         */
        private String dictLabel;

        /**
         * dict value
         * eg: 1
         */
        private String dictValue;
    }
}
