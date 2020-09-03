package org.binave.common.collection.proxy;

import org.binave.common.collection.ExpireMap;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * @author by bin jin on 2019/08/28 01:14.
 */
public class ExpireMapProxy<K, V> implements ExpireMap<K, V> {

    private Map<K, V> map;
    private long defaultTimeMillis;

    // 当改变整体的超时时间时，改变这个
    private long offset;

    public ExpireMapProxy(Map<K, V> map, long defaultTimeMillis) {
        if (map instanceof ExpireMap) {
            throw new IllegalArgumentException();
        }
        this.map = map;
        this.defaultTimeMillis = defaultTimeMillis;
    }

    @Override
    public int size() {
        trim();
        return this.map.size();
    }

    @Override
    public boolean isEmpty() {
        trim();
        return this.map.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return this.map.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return false;
    }

    @Override
    public V get(Object key) {
        return null;
    }

    @Override
    public V get(K key, long timeMillis) {
        return null;
    }

    @Override
    public long getExpire(K key) {
        return 0;
    }

    @Override
    public void addExpire(K key, long timeMillis) {

    }

    @Override
    public V put(K key, long timeMillis, V value) {
        return null;
    }

    @Override
    public V putIfAbsent(K key, long timeMillis, V value) {
        return null;
    }

    @Override
    public V put(K key, V value) {
        return put(key, defaultTimeMillis, value);
    }

    @Override
    public V remove(Object key) {
        return this.map.remove(key);
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> m) {

    }

    @Override
    public void clear() {
        this.map.clear();
    }

    @Override
    public Set<K> keySet() {
        trim();
        return this.map.keySet();
    }

    @Override
    public Collection<V> values() {
        trim();
        return null;
    }

    @Override
    public Set<Entry<K, V>> entrySet() {
        trim();
        return null;
    }

    @Override
    public V getOrDefault(Object key, V defaultValue) {
        return null;
    }

    @Override
    public void forEach(BiConsumer<? super K, ? super V> action) {

    }

    @Override
    public void replaceAll(BiFunction<? super K, ? super V, ? extends V> function) {

    }

    @Override
    public V putIfAbsent(K key, V value) {
        return null;
    }

    @Override
    public boolean remove(Object key, Object value) {
        return false;
    }

    @Override
    public boolean replace(K key, V oldValue, V newValue) {
        return false;
    }

    @Override
    public V replace(K key, V value) {
        return null;
    }

    @Override
    public V computeIfAbsent(K key, Function<? super K, ? extends V> mappingFunction) {
        return null;
    }

    @Override
    public V computeIfPresent(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        return null;
    }

    @Override
    public V compute(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        return null;
    }

    @Override
    public V merge(K key, V value, BiFunction<? super V, ? super V, ? extends V> remappingFunction) {
        return null;
    }

    @Override
    public void trim() {

    }

    @Override
    public long getDefaultExpire() {
        return 0;
    }

    @Override
    public void setDefaultExpire(long timeMillis) {

    }

    @Override
    public void putAll(Map<? extends K, ? extends V> m, long timeMillis) {

    }

    private class Expire {
        V v;
        long timeMillis;

        Expire(V v, long timeMillis) {
            set(v, timeMillis);
        }

        void set(V v, long timeMillis) {
            this.v = v;
            this.timeMillis = timeMillis;
        }
    }
}
