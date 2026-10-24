package com.labdatahub.component.collect;

import java.util.HashMap;
import java.util.Map;

import com.labdatahub.component.collect.adapter.BrotherCollectionAdapter;
import com.labdatahub.component.collect.adapter.CncCollectionAdapter;
import com.labdatahub.component.collect.adapter.DatabaseCollectionAdapter;
import com.labdatahub.component.collect.adapter.FanucCollectionAdapter;
import com.labdatahub.component.collect.adapter.FinsCollectionAdapter;
import com.labdatahub.component.collect.adapter.Mc3eCollectionAdapter;
import com.labdatahub.component.collect.adapter.ModbusCollectionAdapter;
import com.labdatahub.component.collect.adapter.S7CollectionAdapter;

/**
 * 协议族 → 适配器 注册表
 */
public final class CollectionRegistry {

    /** 已注册的适配器：key = 协议族名 */
    private static final Map<String, CollectionAdapter> ADAPTERS = new HashMap<>();

    static {
        register(new ModbusCollectionAdapter());
        register(new S7CollectionAdapter());
        register(new FinsCollectionAdapter());
        register(new Mc3eCollectionAdapter());
        register(new BrotherCollectionAdapter());
        register(new FanucCollectionAdapter());
        register(new CncCollectionAdapter());
        register(new DatabaseCollectionAdapter());
    }

    private CollectionRegistry() {
        throw new UnsupportedOperationException("该类为静态工具类，禁止实例化");
    }

    private static void register(CollectionAdapter adapter) {
        ADAPTERS.put(adapter.family(), adapter);
    }

    /**
     * 取协议族对应的适配器
     *
     * @param family 协议族
     * @return 适配器；未注册返回 null
     */
    public static CollectionAdapter get(String family) {
        return family == null ? null : ADAPTERS.get(family);
    }
}
