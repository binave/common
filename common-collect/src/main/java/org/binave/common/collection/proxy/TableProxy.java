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
import com.google.common.collect.Table;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * 代理 {@link com.google.common.collect.Table}，用于同步更新所有实例
 *
 * @author bin jin
 * @since 1.8
 */
public class TableProxy<R, C, V> implements Table<R, C, V>, SyncProxy<Table<R, C, V>> {

    private Table<R, C, V> table;

    @Override
    public void syncUpdate(Table<R, C, V> table) {
        this.table = table;
    }

    @Override
    public boolean isNull() {
        return table == null;
    }

    @Override
    public boolean contains(Object rowKey, Object columnKey) {
        return this.table.contains(rowKey, columnKey);
    }

    @Override
    public boolean containsRow(Object rowKey) {
        return this.table.containsRow(rowKey);
    }

    @Override
    public boolean containsColumn(Object columnKey) {
        return this.table.containsColumn(columnKey);
    }

    @Override
    public boolean containsValue(Object value) {
        return this.table.containsValue(value);
    }

    @Override
    public V get(Object rowKey, Object columnKey) {
        return this.table.get(rowKey, columnKey);
    }

    @Override
    public boolean isEmpty() {
        return this.table.isEmpty();
    }

    @Override
    public int size() {
        return this.table.size();
    }

    @Override
    public void clear() {
        this.table.clear();
    }

    @Override
    public V put(R rowKey, C columnKey, V value) {
        return (V) this.table.put(rowKey, columnKey, value);
    }

    @Override
    public void putAll(Table<? extends R, ? extends C, ? extends V> table) {
        this.table.putAll(table);
    }

    @Override
    public V remove(Object rowKey, Object columnKey) {
        return (V) this.table.remove(rowKey, columnKey);
    }

    @Override
    public Map<C, V> row(R rowKey) {
        return this.table.row(rowKey);
    }

    @Override
    public Map<R, V> column(C columnKey) {
        return this.table.column(columnKey);
    }

    @Override
    public Set<Cell<R, C, V>> cellSet() {
        return this.table.cellSet();
    }

    @Override
    public Set<R> rowKeySet() {
        return this.table.rowKeySet();
    }

    @Override
    public Set<C> columnKeySet() {
        return this.table.columnKeySet();
    }

    @Override
    public Collection<V> values() {
        return this.table.values();
    }

    @Override
    public Map<R, Map<C, V>> rowMap() {
        return this.table.rowMap();
    }

    @Override
    public Map<C, Map<R, V>> columnMap() {
        return this.table.columnMap();
    }

    @Override
    public String toString() {
        return table != null ? table.toString() : "{}";
    }

    @Override
    public boolean equals(Object obj) {
        return table != null && table.equals(obj);
    }

    @Override
    public int hashCode() {
        return table != null ? table.hashCode() : -1;
    }
}
