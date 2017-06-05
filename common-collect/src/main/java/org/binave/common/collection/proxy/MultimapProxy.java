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
import com.google.common.collect.Multimap;
import com.google.common.collect.Multiset;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * 代理 {@link com.google.common.collect.Multimap}，用于同步更新所有实例
 *
 * @author bin jin
 * @since 1.8
 */
public class MultimapProxy<K, V> implements Multimap<K, V>, SyncProxy<Multimap<K, V>> {

    private Multimap<K, V> multimap;

    @Override
    public void syncUpdate(Multimap<K, V> multimap) {
        this.multimap = multimap;
    }

    @Override
    public boolean isNull() {
        return multimap == null;
    }

    @Override
    public int size() {
        return multimap.size();
    }

    @Override
    public boolean isEmpty() {
        return this.multimap.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return this.multimap.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return this.multimap.containsValue(value);
    }

    @Override
    public boolean containsEntry(Object key, Object value) {
        return this.multimap.containsEntry(key, value);
    }

    @Override
    public boolean put(K key, V value) {
        return this.multimap.put(key, value);
    }

    @Override
    public boolean remove(Object key, Object value) {
        return this.multimap.remove(key, value);
    }

    @Override
    public boolean putAll(K key, Iterable<? extends V> values) {
        return this.multimap.putAll(key, values);
    }

    @Override
    public boolean putAll(Multimap<? extends K, ? extends V> multimap) {
        return this.multimap.putAll(multimap);
    }

    @Override
    public Collection<V> replaceValues(K key, Iterable<? extends V> values) {
        return this.multimap.replaceValues(key, values);
    }

    @Override
    public Collection<V> removeAll(Object key) {
        return this.multimap.removeAll(key);
    }

    @Override
    public void clear() {
        this.multimap.clear();
    }

    @Override
    public Collection<V> get(K key) {
        return this.multimap.get(key);
    }

    @Override
    public Set<K> keySet() {
        return this.multimap.keySet();
    }

    @Override
    public Multiset<K> keys() {
        return this.multimap.keys();
    }

    @Override
    public Collection<V> values() {
        return this.multimap.values();
    }

    @Override
    public Collection<Map.Entry<K, V>> entries() {
        return this.multimap.entries();
    }

    @Override
    public Map<K, Collection<V>> asMap() {
        return this.multimap.asMap();
    }

    @Override
    public String toString() {
        return multimap != null ? multimap.toString() : "{}";
    }

    @Override
    public boolean equals(Object obj) {
        return multimap != null && multimap.equals(obj);
    }

    @Override
    public int hashCode() {
        return multimap != null ? multimap.hashCode() : -1;
    }
}
