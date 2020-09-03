package org.binave.common.collection;

import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * 在构造函数中设置默认过期时间
 *
 * @author by bin jin on 2019/08/27 21:44.
 */
public interface ExpireMap<K, V> extends Map<K, V> {

    /**
     * 如果数值大于现在，则认为是时间点。
     * 否则认为是时间差
     */
    V put(K key, long timeMillis, V value);

    V putIfAbsent(K key, V value);

    V putIfAbsent(K key, long timeMillis, V value);

    void putAll(Map<? extends K, ? extends V> m, long timeMillis);

    /**
     * 取出并续时
     */
    V get(K key, long timeMillis);

    long getExpire(K key);

    /**
     * 续时
     */
    void addExpire(K key, long timeMillis);

    /**
     * 清理全部过期缓存
     */
    void trim();

    /**
     * 获得默认过期时间
     */
    long getDefaultExpire();

    /**
     * 修改默认过期时间
     *
     * 通过修正值来调节
     */
    void setDefaultExpire(long timeMillis);

    V getOrDefault(Object key, V defaultValue);

    void forEach(BiConsumer<? super K, ? super V> action);

    void replaceAll(BiFunction<? super K, ? super V, ? extends V> function);

    boolean replace(K key, V oldValue, V newValue);

    V replace(K key, V value);

    V computeIfAbsent(K key, Function<? super K, ? extends V> mappingFunction);

    V computeIfPresent(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction);

    V compute(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction);

    V merge(K key, V value, BiFunction<? super V, ? super V, ? extends V> remappingFunction);
}
