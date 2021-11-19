package com.jrzx.platform.cxc.common.web.dict;

import cn.hutool.cache.Cache;
import cn.hutool.cache.CacheUtil;
import cn.hutool.core.date.DateUnit;
import com.jr.platform.web.dict.DictConvertUtil;

import java.util.List;

/**
 * 字典缓存工具类
 *
 * @Author CaiRui
 * @Date 2021/3/16 13:45
 */
public class DataDictCachKit {
    private static Cache<String, List<DictConvertUtil.Dict>> cache = CacheUtil.newFIFOCache(100, DateUnit.HOUR.getMillis() * 6);

    public static List<DictConvertUtil.Dict> get(String key) {
        return cache.get(key);
    }

    public static void put(String key, List<DictConvertUtil.Dict> list) {
        cache.put(key, list);
    }

    public static void clean(String key) {
        cache.remove(key);
    }

    public static void cleanAll() {
        cache.clear();
    }

    public static boolean containsKey(String key) {
        return cache.containsKey(key);
    }
}
