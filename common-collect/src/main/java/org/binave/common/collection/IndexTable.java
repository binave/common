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

package org.binave.common.collection;

import com.google.common.collect.Table;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * @author by bin jin on 2017/5/13.
 * @since 1.8
 */
public class IndexTable<V> implements SortedTable<Integer, Integer, V> {

    private Object[][] elementData;

    private int rowOffset; // index 下标与 key 的差值

    private int[] columnOffset;

    private int[] head, tail;

    private int modCount; // 修改次数

    private int size; // 存储元素的个数

    /**
     * 初始容量
     *
     * @see java.util.ArrayList#DEFAULT_CAPACITY
     */
    private static final int DEFAULT_CAPACITY = 10;


    @Override
    public boolean contains(Object rowKey, Object columnKey) {
        int rowIndex = rowIndex((Integer) rowKey);
        int columnIndex = columnIndex((Integer) rowKey, (Integer) columnKey);
        return elementData[rowIndex][columnIndex] != null;
    }

    @Override
    public boolean containsRow(Object rowKey) {
        return false;
    }

    @Override
    public boolean containsColumn(Object columnKey) {
        return false;
    }

    @Override
    public boolean containsValue(Object value) {
        return false;
    }

    @Override
    public V get(Object rowKey, Object columnKey) {
        try {
            return (V) elementData[rowIndex((Integer) rowKey)][columnIndex((Integer) rowKey, (Integer) columnKey)];
        } catch (RuntimeException e) {
            // ArrayIndexOutOfBoundsException
            return null;
        }
    }

    /**
     * 行数位移
     * @param rowKey 行
     */
    private int rowIndex(int rowKey) {
        return rowKey - rowOffset;
    }

    private int columnIndex(int rowKey, int columnKey) {
        return columnKey - columnOffset[rowIndex(rowKey)];
    }


    @Override
    public boolean isEmpty() {
        return size == 0;
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public void clear() {
        init();
    }

    private void init() {
        elementData = null;
        rowOffset = 0;
        columnOffset = null;
        head = null;
        tail = null;
        modCount = 0;
        size = 0;
    }

    @Override
    public V put(Integer rowKey, Integer columnKey, V value) {
        if (rowKey == null || columnKey == null || value == null) return null;
        int row = rowIndex(rowKey);
        return null;
    }

    @Override
    public void putAll(Table<? extends Integer, ? extends Integer, ? extends V> table) {

    }

    @Override
    public V remove(Object rowKey, Object columnKey) {
        return null;
    }

    @Override
    public Map<Integer, V> row(Integer rowKey) {
        return null;
    }

    @Override
    public Map<Integer, V> column(Integer columnKey) {
        return null;
    }

    @Override
    public Set<Cell<Integer, Integer, V>> cellSet() {
        return null;
    }

    @Override
    public Set<Integer> rowKeySet() {
        return null;
    }

    @Override
    public Set<Integer> columnKeySet() {
        return null;
    }

    @Override
    public Collection<V> values() {
        return null;
    }

    @Override
    public Map<Integer, Map<Integer, V>> rowMap() {
        return null;
    }

    @Override
    public Map<Integer, Map<Integer, V>> columnMap() {
        return null;
    }

    @Override
    public SortedTable<Integer, Integer, V> subRowTable(Integer fromKey, Integer toKey) {
        return null;
    }

    @Override
    public SortedTable<Integer, Integer, V> headRowTable(Integer toKey) {
        return null;
    }

    @Override
    public SortedTable<Integer, Integer, V> tailRowTable(Integer fromKey) {
        return null;
    }

    @Override
    public Integer firstRowKey() {
        return null;
    }

    @Override
    public Integer lastRowKey() {
        return null;
    }
}

