/*
 * Copyright (c) 2017 bin jin.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.binave.common.collection.proxy;


import org.binave.common.api.SyncProxy;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * 代理 {@link java.util.Map}，用于同步更新所有实例
 *
 * @author bin jin
 * @since 1.8
 */
public class MapProxy<K, V> implements Map<K, V>, SyncProxy<Map<K, V>> {

    private Map<K, V> map;

    @Override
    public void syncUpdate(Map<K, V> map) {
        this.map = map;
    }

    @Override
    public boolean isNull() {
        return map == null;
    }

    @Override
    public int size() {
        return this.map.size();
    }

    @Override
    public boolean isEmpty() {
        return this.map.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return this.map.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return this.map.containsValue(value);
    }

    @Override
    public V get(Object key) {
        return this.map.get(key);
    }

    @Override
    public V put(K key, V value) {
        return this.map.put(key, value);
    }

    @Override
    public V remove(Object key) {
        return this.map.remove(key);
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> m) {
        this.map.putAll(m);
    }

    @Override
    public void clear() {
        this.map.clear();
    }

    @Override
    public Set<K> keySet() {
        return this.map.keySet();
    }

    @Override
    public Collection<V> values() {
        return this.map.values();
    }

    @Override
    public Set<Entry<K, V>> entrySet() {
        return this.map.entrySet();
    }

    @Override
    public String toString() {
        return map != null ? map.toString() : "{}";
    }

    @Override
    public boolean equals(Object obj) {
        return map != null && map.equals(obj);
    }

    @Override
    public int hashCode() {
        return map != null ? map.hashCode() : -1;
    }
}
