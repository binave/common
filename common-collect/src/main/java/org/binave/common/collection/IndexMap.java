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

import org.binave.common.util.ArrayUtil;

import java.util.*;

/**
 * 基于数组索引的 {@link Map} 实现
 *
 * 键固定为 Integer 类型
 * 适用于存储【键】连续的数据，如配置表
 * todo 未实现缩小
 *
 * 注意：
 *      此 Map 不是线程安全的
 *      不支持 clone
 *      不支持 jdk 原生序列化
 *      使用 {@link TreeMap} 作为对照组，通过了随机测试，但不保证没有问题
 *
 * @author bin jin
 * @since 1.8
 */
public class IndexMap<V> implements SortedMap<Integer, V> {

    /**
     * @see ArrayList#elementData
     */
    private Object[] elementData;

    private int offset; // index 下标与 key 的差值

    private int head, tail; // [head, tail) 左闭右开区间。最小、最大有值索引

    private int modCount; // 修改次数，用来监控循环中的删除操作

    private int size; // 存储元素的个数

    private boolean trim; // 是否进行 trim

    /**
     * 初始容量
     *
     * @see ArrayList#DEFAULT_CAPACITY
     */
    private static final int DEFAULT_CAPACITY = 10;

    /**
     * 最大容量
     *
     * @see ArrayList#MAX_ARRAY_SIZE
     */
    private static final int MAX_MAP_SIZE = Integer.MAX_VALUE - 8;

    /**
     * 数组的初始容量
     *
     * @param initialCapacity the initial capacity of the array
     */
    public IndexMap(int initialCapacity, boolean trim) {
        init(initialCapacity, trim);
    }

    public IndexMap(int initialCapacity) {
        init(initialCapacity, false);
    }

    public IndexMap() {
        init(0, false);
    }

    public IndexMap(Map<? extends Integer, ? extends V> m) {
        init(0, false);
        putAll(m);
    }

    @Override
    public int size() {
        return size;
    }

    private int size(int head, int tail) {
        if (isEmpty() || head > tail || tail < this.head || head > this.tail) return 0;
        // 全部
        if (head <= this.head && tail >= this.tail) return size();
        int subSize = 0;
        for (int i = head; i <= tail; i++) if (elementData[i] != null) ++subSize;
        return subSize;
    }

    @Override
    public boolean isEmpty() {
        return size == 0;
    }

    private boolean preEmpty(int head, int tail) {
        return isEmpty() || head > tail || tail < this.head || head > this.tail;
    }

    private boolean isEmpty(int head, int tail) {
        if (preEmpty(head, tail)) return true;

        // 全部
        if (head <= this.head && tail >= this.tail) return isEmpty();

        for (int i = 0; i <= (tail - head) / 2; i++) {
            if (elementData[head + i] != null) return false;
            if (elementData[tail - i] != null) return false;
        }
        return true;
    }

    @Override
    public boolean containsKey(Object key) {
        return containsKey(0, length() - 1, key);
    }

    private boolean containsKey(int head, int tail, Object key) {
        int index = index((Integer) key);
        return index >= head && index <= tail && elementData[index] != null;
    }

    @Override
    public boolean containsValue(Object value) {
        return indexOfValue(head, tail, value) > 0;
    }

    /**
     * 获得 value 的索引
     *
     * @param head index
     * @param tail index
     */
    private int indexOfValue(int head, int tail, Object value) {
        if (value == null || preEmpty(head, tail)) return -1;

        for (int i = 0; i <= (tail - head) / 2; i++) {
            if (value.equals(elementData[head + i])) return head + i;
            if (value.equals(elementData[tail - i])) return tail - i;
        }
        return -1;
    }

    private Comparator comparator = null;

    @Override
    public Comparator<? super Integer> comparator() {
        if (comparator == null) comparator = new Comparator<Integer>() {
            @Override
            public int compare(Integer o1, Integer o2) {
                return Integer.compare(o1, o2);
            }

            @Override
            public boolean equals(Object obj) {
                throw new UnsupportedOperationException();
            }
        };
        return comparator;
    }

    @Override
    public V get(Object key) {
        try {
            return (V) elementData[index((Integer) key)];
        } catch (RuntimeException e) {
            // ArrayIndexOutOfBoundsException
            return null;
        }
    }

    private V get(int head, int tail, Object key) {
        int index = index((Integer) key);
        return index >= head && index <= tail ? get(key) : null;
    }

    @Override
    public V put(Integer key, V value) {
        if (key == null || value == null) return null;

        // 初始化 offset
        if (isEmpty()) initIndex(key);

        int index = index(key);
        return index < 0 ?
                extendHead(key, value) :
                (
                        index >= length() ?
                                extendTail(key, value) :
                                extendNil(key, value)
                );
    }

    @Override
    public V remove(Object key) {
        if (key == null) return null;
        Integer k = (Integer) key;
        int index = index(k);
        boolean inRange = index >= head && index <= tail;
        Object v = inRange ? elementData[index] : null;
        if (v != null) rectifyDel(index);
        // offset maybe chang
        if (inRange) elementData[index(k)] = null;
        return (V) v;
    }

    @Override
    public void putAll(Map<? extends Integer, ? extends V> m) {

        if (m == null || m.isEmpty()) return;

        if (m.size() == 1) {
            Entry<? extends Integer, ? extends V> entry = m.entrySet().iterator().next();
            IndexMap.this.put(entry.getKey(), entry.getValue());
            return;
        }

        int tmpHead = MAX_MAP_SIZE, tmpTail = 0;

        if (m instanceof SortedMap) {
            // 如果是 sortedMap
            SortedMap<? extends Integer, ? extends V> s = (SortedMap) m;
            tmpHead = s.firstKey();
            tmpTail = s.lastKey() + 1;
        } else {
            // 确定最大和最小 key
            for (Integer i : m.keySet()) {
                if (tmpHead > i) tmpHead = i;
                if (i > tmpTail) tmpTail = i;
            }
        }

        // 如果原来是空
        if (isEmpty()) initIndex(tmpHead);

        // 为新元素准备好空间
        extendBoth(tmpHead, tmpTail);

        // 将元素加入
        for (Entry<? extends Integer, ? extends V> entry : m.entrySet())
            extendNil(entry.getKey(), entry.getValue());

    }

    @Override
    public void clear() {
        init(0, false);
    }

    private void clear(int head, int tail) {
        if (isEmpty(head, tail)) return;
        if (head <= this.head && tail >= this.tail) {
            clear(); // 全部
        } else if (head <= this.head) {
            // 当头部边界重合时，从尾部开始删除，防止多次 trim 导致频繁数组复制
            for (int i = tail; i >= head; i--)
                if (elementData[i] != null) remove(key(i));
        } else {
            for (int i = head; i <= tail; i++)
                if (elementData[i] != null) remove(key(i));
        }
    }

    private void init(int initialCapacity, boolean trim) {
        // 将头部标记初始化到最后
        head = initialCapacity > 0 ?
                initialCapacity + DEFAULT_CAPACITY * 2 :
                DEFAULT_CAPACITY * 2;
        tail = 0;
        offset = 0;
        size = 0;
        elementData = new Object[head];
        this.trim = trim;
    }

    /**
     * 使用 key 获得数组索引
     *
     * @param key 键
     */
    private int index(int key) {
        return key - offset;
    }

    /**
     * 使用数组索引得到 key
     *
     * @param index 数组索引
     */
    private int key(int index) {
        return index + offset;
    }

    // 初始化 offset 数值
    private void initIndex(int key) {
        if (offset != 0 || modCount++ != 0) return;
        offset = key - DEFAULT_CAPACITY;
    }

    // 补齐，用于扩展数组大小到整数的十倍
    private int pads(int index) {
        if (index < 0) index *= -1;

        // 去除个位数，加 10，如果个位大于 6 ，再加 10
        return (index / DEFAULT_CAPACITY + 1)
                * DEFAULT_CAPACITY + (index % DEFAULT_CAPACITY > 6 ? DEFAULT_CAPACITY : 0);
    }

    private int length() {
        return elementData.length;
    }

    private V extendHead(int key, V value) {
        int index = index(key);

        // 多线程支持
        if (index >= 0) {
            return index >= length() ?
                    extendTail(key, value) :
                    extendNil(key, value);
        }

        // 向左扩充，获得扩充的长度
        int offsetDiff = pads(index);
        testCapacity(offsetDiff); // 测试数组是否过大
        // 右移数组
        elementData = ArrayUtil.offsetCopyOf(elementData, offsetDiff, Object[].class);
        offset -= offsetDiff;
        // index change
        index = index(key);
        elementData[index] = value;
        tail += offsetDiff;
        head += offsetDiff;
        rectifyAdd(index);
        return null;
    }

    private V extendTail(int key, V value) {
        int index = index(key);
        int lengthDiff = pads(index);
        testCapacity(lengthDiff); // 测试数组是否过大
        // 向右扩充
        elementData = Arrays.copyOf(elementData, lengthDiff, Object[].class);
        rectifyAdd(index);
        elementData[index] = value;
        return null;
    }

    private V extendNil(int key, V value) {
        int index = index(key);
        // 看看原来位置上有没有
        Object v = elementData[index];
        // 如果原来的位置为空，则进行添加
        if (v == null) rectifyAdd(index);
        elementData[index] = value;
        return (V) v;
    }

    private void extendBoth(int headKey, int tailKey) {

        // 左越界，需要与新集合对齐
        int offsetPlus = index(headKey) < 0 ? pads(index(headKey)) : 0;

        // 右越界
        int lengthPlus = index(tailKey) + offsetPlus > length() - 1 ?
                pads(index(tailKey) + offsetPlus)
                : length();

        // 需要数组进行变化
        if (offsetPlus == 0 && lengthPlus <= length()) return;

        int newLength = offsetPlus + lengthPlus;
        testCapacity(newLength - length());

        // 扩展数组
        elementData = ArrayUtil.offsetCopyOf(elementData, offsetPlus,
                newLength, Object[].class);

        // 在不出现错误的情况下，提交对 offset 的修改
        offset -= offsetPlus;

        // 维护有效边界
        if (offsetPlus > 0) {
            head = index(headKey);
            tail += offsetPlus; // 有 offset 变动的情况，tail 要相应修正
        } else if (head > index(headKey)) head = index(headKey);

        if (lengthPlus > length())
            tail = index(tailKey);
        else if (tail < index(tailKey)) tail = index(tailKey);

    }

    // 测试区间范围是否合理
    private void testInterval(Integer fromKey, Integer toKey) {
        if (fromKey == null || toKey == null || fromKey > toKey)
            throw new IllegalArgumentException("fromKey > toKey: fromKey=" + fromKey + ", toKey=" + toKey);
    }

    // 测试容量范围
    private void testCapacity(int newCapacity) {
        if (newCapacity + length() > MAX_MAP_SIZE || newCapacity + length() < 0)
            throw new OutOfMemoryError("capacity=" + newCapacity);
    }

    // 处理增加元素所带来的影响
    private void rectifyAdd(int index) {
        // 增加的情况
        if (index < head) head = index;
        if (index > tail) tail = index;
        modCount++;
        size++;
    }

    // 处理减少元素带来的影响
    private void rectifyDel(int index) {
        if (size > 0) {
            if (index == head) {
                head = nextHeadIndex(head);
                if (trim && head > tail - head) {
                    // todo trim left
                }
            } else if (index == tail) {
                tail = nextTailIndex(tail);
                if (trim && length() - tail > tail - head) {
                    // todo trim right
                }
            }

            modCount++; // 记录修改次数
            size--;

        } else clear();
    }

    private int nextHeadIndex(int index) {
        int i = index + 1;
        for (; i < tail; i++) if (elementData[i] != null) return i;
        return i;
    }

    private int nextTailIndex(int index) {
        int i = index - 1;
        for (; i > head; i--) if (elementData[i] != null) return i;
        return i;
    }

    @Override
    public String toString() {
        return toString(entrySet(), this);
    }

    /**
     * @see AbstractMap#toString
     */
    private String toString(Set<Entry<Integer, V>> entrySet, Map<Integer, V> map) {
        Iterator<Entry<Integer, V>> i = entrySet.iterator();
        if (!i.hasNext())
            return "{}";

        StringBuilder sb = new StringBuilder();
        sb.append('{');
        for (; ; ) {
            Entry<Integer, V> e = i.next();
            Integer key = e.getKey();
            V value = e.getValue();
            sb.append(key);
            sb.append('=');
            sb.append(value == map ? "(this Map)" : value);
            if (!i.hasNext())
                return sb.append('}').toString();
            sb.append(',').append(' ');
        }
    }

    // sub clone
    @Override
    public SortedMap<Integer, V> subMap(Integer fromKey, Integer toKey) {
        testInterval(fromKey, toKey);
        return new SubSortedMap(fromKey, toKey);
    }

    @Override
    public SortedMap<Integer, V> headMap(Integer toKey) {
        return subMap(firstKey(), toKey);
    }

    @Override
    public SortedMap<Integer, V> tailMap(Integer fromKey) {
        return subMap(fromKey, lastKey() + 1);
    }

    @Override
    public Integer firstKey() {
        if (isEmpty()) throw new NoSuchElementException();
        return key(head);
    }

    @Override
    public Integer lastKey() {
        if (isEmpty()) throw new NoSuchElementException();
        return key(tail);
    }

    private Set<Integer> keySet;

    @Override
    public Set<Integer> keySet() {
        Set<Integer> ks = keySet;
        if (ks == null) {
            ks = new IndexSet<Integer>() {

                @Override
                public Iterator<Integer> iterator() {

                    return new SubIterator<Integer>() {

                        @Override
                        public Integer next() {
                            checkForCoModification();
                            if (!hasNext()) throw new NoSuchElementException();
                            skip();
                            return globalKey();
                        }

                    };
                }

                @Override
                public boolean contains(Object o) {
                    return IndexMap.this.containsKey(o);
                }

                @Override
                public boolean remove(Object o) {
                    return IndexMap.this.remove(o) != null;
                }
            };
            keySet = ks;
        }
        return ks;
    }

    private Collection<V> coll;

    @Override
    public Collection<V> values() {
        Collection<V> vs = coll;
        if (vs == null) {
            vs = new IndexSet<V>() {

                @Override
                public Iterator<V> iterator() {
                    return new SubIterator<V>() {

                        @Override
                        public V next() {
                            checkForCoModification();
                            if (!hasNext()) throw new NoSuchElementException();
                            skip();
                            return IndexMap.this.get(globalKey());
                        }
                    };
                }

                @Override
                public boolean contains(Object o) {
                    return IndexMap.this.indexOfValue(head, tail, o) > 0;
                }

                @Override
                public boolean remove(Object o) {
                    int collIndex = IndexMap.this.indexOfValue(head, tail, o);
                    return collIndex > 0 && IndexMap.this.remove(IndexMap.this.key(collIndex)) != null;
                }
            };
            coll = vs;
        }
        return vs;
    }

    private Set<Entry<Integer, V>> entrySet;

    @Override
    public Set<Entry<Integer, V>> entrySet() {
        Set<Map.Entry<Integer, V>> es = entrySet;
        if (es == null) {
            es = new IndexSet<Entry<Integer, V>>() {

                @Override
                public Iterator<Entry<Integer, V>> iterator() {
                    return new SubIterator<Entry<Integer, V>>() {

                        @Override
                        public Entry<Integer, V> next() {
                            checkForCoModification();
                            if (!hasNext()) throw new NoSuchElementException();
                            skip();
                            return nextEntry();
                        }
                    };
                }

                @Override
                public boolean contains(Object o) {
                    Entry<Integer, V> entry = (Entry<Integer, V>) o;
                    V v = IndexMap.this.get(entry.getKey());
                    return v != null && v.equals(entry.getValue());
                }

                /**
                 * @see IndexMap#putAll
                 */
                @Override
                public boolean addAll(Collection<? extends Entry<Integer, V>> c) {

                    if (c == null || c.isEmpty()) return false;

                    if (c.size() == 1) {
                        Entry<Integer, V> entry = c.iterator().next();
                        IndexMap.this.put(entry.getKey(), entry.getValue());
                        return true;
                    }

                    int headKey = MAX_MAP_SIZE, tailKey = 0;
                    for (Entry<Integer, V> entry : c) {
                        int i = entry.getKey();
                        if (headKey > i) headKey = i;
                        if (i > tailKey) tailKey = i;
                    }

                    if (isEmpty()) initIndex(headKey);

                    extendBoth(headKey, tailKey);
                    for (Entry<Integer, V> entry : c)
                        IndexMap.this.extendNil(entry.getKey(), entry.getValue());

                    return true;
                }

                @Override
                public boolean add(Entry<Integer, V> entry) {
                    IndexMap.this.put(entry.getKey(), entry.getValue());
                    return true;
                }

                @Override
                public boolean remove(Object o) {
                    IndexMap.this.remove(((Entry<Integer, V>) o).getKey());
                    return true;
                }
            };
            entrySet = es;
        }
        return es;
    }

    @Override
    public int hashCode() {
        return hashCode(entrySet());
    }

    private int hashCode(Set<Entry<Integer, V>> entrySet) {
        int h = 0;
        for (Entry<Integer, V> entry : entrySet) h += entry.hashCode();
        return h;
    }

    @Override
    public boolean equals(Object o) {
        return equals(o, this);
    }

    /**
     * @see AbstractMap#equals
     */
    private boolean equals(Object o, Map<Integer, V> map) {
        if (o == map)
            return true;

        if (!(o instanceof Map))
            return false;

        Map<?, ?> m = (Map<?, ?>) o;
        if (m.size() != map.size())
            return false;

        try {
            for (Entry<Integer, V> e : map.entrySet()) {
                Integer key = e.getKey();
                V value = e.getValue();
                if (value == null) {
                    if (!(m.get(key) == null && m.containsKey(key)))
                        return false;
                } else {
                    if (!value.equals(m.get(key)))
                        return false;
                }
            }
        } catch (ClassCastException | NullPointerException unused) {
            return false;
        }

        return true;
    }

    // 迭代器实现
    private abstract class SubIterator<E> implements Iterator<E> {

        // 初始化当前位置
        private int _position, _subTail;

        // 可删除标记
        private boolean _canDel = false;

        // 迭代器中的修改次数
        private int _expectedModCount = modCount;

        // 实现默认迭代器
        SubIterator() {
            _position = head;
            _subTail = tail;
        }

        // 实现 sub 迭代器
        SubIterator(int newHead, int newTail) {
            _position = newHead;
            _subTail = newTail;
        }

        @Override
        public boolean hasNext() {
            for (int i = _position; i <= _subTail; i++)
                if (elementData[i] != null) {
                    _position = i; // 记录有效值的位置，方便下次查询
                    return true;
                }
            return false;
        }

        // 删除键值对，此处不做 sub 情况下的 trim 优化
        @Override
        public void remove() {
            if (!_canDel) throw new IllegalStateException();
            checkForCoModification();
            IndexMap.this.remove(globalKey());
            syncModCount();
            _canDel = false;
        }

        // 拿到正确的索引
        int globalKey() {
            return IndexMap.this.key(_position) - 1; // 修正跳过记录位置
        }

        // 跳过当前有效索引
        void skip() {
            _position++; // 跳过记录位置
            _canDel = true;
        }

        // 检查删除途中是否有修改
        void checkForCoModification() {
            if (modCount != _expectedModCount)
                throw new ConcurrentModificationException();
        }

        // 同步修改次数
        void syncModCount() {
            _expectedModCount = modCount;
        }

        Entry<Integer, V> nextEntry() {
            return new Entry<Integer, V>() {

                private final Integer key = globalKey();
                private V value = IndexMap.this.get(key);

                @Override
                public Integer getKey() {
                    return key;
                }

                @Override
                public V getValue() {
                    return value;
                }

                @Override
                public V setValue(V value) {
                    this.value = value;
                    return IndexMap.this.extendNil(key, value);
                }

                @Override
                public int hashCode() {
                    return key ^ Objects.hashCode(value);
                }

                @Override
                public boolean equals(Object obj) {
                    if (obj == this) return true;
                    if (obj instanceof Entry) {
                        Map.Entry<?, ?> e = (Map.Entry<?, ?>) obj;
                        if (e.getKey() instanceof Integer) {
                            int i = (Integer) e.getKey();
                            return Objects.equals(i, key) &&
                                    Objects.equals(e.getValue(), value);
                        }
                    }
                    return false;
                }

                @Override
                public String toString() {
                    return key + "=" + value;
                }

            };
        }
    }

    // set 实现
    private abstract class IndexSet<E> extends AbstractSet<E> {

        @Override
        public int size() {
            return IndexMap.this.size();
        }

        @Override
        public boolean isEmpty() {
            return IndexMap.this.isEmpty();
        }

        @Override
        public boolean addAll(Collection<? extends E> c) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void clear() {
            IndexMap.this.clear();
        }

    }

    // sub set 实现
    private abstract class SubIndexSet<E> extends AbstractSet<E> {

        private int _subHead, _subTail;

        SubIndexSet(int newHead, int newTail) {
            _subHead = newHead;
            _subTail = newTail;
        }

        @Override
        public int size() {
            return IndexMap.this.size(_subHead, _subTail);
        }

        @Override
        public boolean isEmpty() {
            return IndexMap.this.isEmpty(_subHead, _subTail);
        }

        @Override
        public boolean addAll(Collection<? extends E> c) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void clear() {
            IndexMap.this.clear(_subHead, _subTail);
        }

        // 查看是否在规定范围内
        boolean inSubRange(Object key) {
            int subIndex = IndexMap.this.index((Integer) key);
            return (subIndex >= _subHead) && (subIndex <= _subTail);
        }

        int subIndexOfValue(Object o) {
            // IndexMap already trim
            return IndexMap.this.indexOfValue(_subHead, _subTail, o);
        }
    }

    private class SubSortedMap<E> implements SortedMap<Integer, V> {

        private int _headLimit, _tailLimit;

        SubSortedMap(Integer fromKey, Integer toKey) {
            _headLimit = IndexMap.this.index(fromKey);
            _tailLimit = IndexMap.this.index(toKey) - 1;
        }

        @Override
        public Comparator<? super Integer> comparator() {
            return IndexMap.this.comparator();
        }

        @Override
        public SortedMap<Integer, V> subMap(Integer fromKey, Integer toKey) {
            IndexMap.this.testInterval(fromKey, toKey);

            int fromLimit = IndexMap.this.key(_headLimit), toLimit = IndexMap.this.key(_tailLimit) + 1;
            return new SubSortedMap<>(
                    fromKey < fromLimit ? fromLimit : fromKey,
                    toKey > toLimit ? toLimit : toKey
            );
        }

        @Override
        public SortedMap<Integer, V> headMap(Integer toKey) {
            return new SubSortedMap(IndexMap.this.key(_headLimit), toKey);
        }

        @Override
        public SortedMap<Integer, V> tailMap(Integer fromKey) {
            return new SubSortedMap(fromKey, IndexMap.this.key(_tailLimit) + 1);
        }

        @Override
        public Integer firstKey() {
            if (isEmpty()) throw new NoSuchElementException();
            int newHead = IndexMap.this.nextHeadIndex(_headLimit - 1); // 允许当前位置为 head
            return IndexMap.this.key(newHead > _tailLimit ? _tailLimit : newHead);
        }

        @Override
        public Integer lastKey() {
            if (isEmpty()) throw new NoSuchElementException();
            int newTail = IndexMap.this.nextTailIndex(_tailLimit + 1); // 补偿
            return IndexMap.this.key(newTail < _headLimit ? _headLimit : newTail);
        }

        @Override
        public int size() {
            return IndexMap.this.size(_headLimit, _tailLimit);
        }

        @Override
        public boolean isEmpty() {
            return IndexMap.this.isEmpty(_headLimit, _tailLimit);
        }

        @Override
        public boolean containsKey(Object key) {
            return IndexMap.this.containsKey(_headLimit, _tailLimit, key);
        }

        @Override
        public boolean containsValue(Object value) {
            return IndexMap.this.indexOfValue(_headLimit, _tailLimit, value) > 0;
        }

        @Override
        public V get(Object key) {
            return IndexMap.this.get(_headLimit, _tailLimit, key);
        }

        @Override
        public V put(Integer key, V value) {
            if (key == null || value == null) return null;
            if (outRange(key)) throw new IllegalArgumentException("key out of range");
            return IndexMap.this.put(key, value);
        }

        @Override
        public V remove(Object key) {
            if (key == null) return null;
            return outRange((Integer) key) ? null : IndexMap.this.remove(key);
        }

        private boolean outRange(Integer key) {
            int i = IndexMap.this.index(key);
            return i < _headLimit || i >= _tailLimit;
        }

        @Override
        public void putAll(Map<? extends Integer, ? extends V> m) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void clear() {
            IndexMap.this.clear(_headLimit, _tailLimit);
        }

        @Override
        public Set<Integer> keySet() {

            return new SubIndexSet<Integer>(_headLimit, _tailLimit) {

                @Override
                public Iterator<Integer> iterator() {
                    return new SubIterator<Integer>(_headLimit, _tailLimit) {

                        @Override
                        public Integer next() {
                            checkForCoModification();
                            if (!hasNext()) throw new NoSuchElementException();
                            skip();
                            return globalKey();
                        }
                    };
                }

                @Override
                public boolean contains(Object o) {
                    return inSubRange(o) && IndexMap.this.containsKey(o);
                }

                @Override
                public boolean remove(Object o) {
                    return inSubRange(o) && IndexMap.this.remove(o) != null;
                }

            };
        }

        @Override
        public Collection<V> values() {
            return new SubIndexSet<V>(_headLimit, _tailLimit) {

                @Override
                public Iterator<V> iterator() {
                    return new SubIterator<V>(_headLimit, _tailLimit) {
                        @Override
                        public V next() {
                            checkForCoModification();
                            if (!hasNext()) throw new NoSuchElementException();
                            skip();
                            return IndexMap.this.get(globalKey());
                        }
                    };
                }

                @Override
                public boolean contains(Object o) {
                    return subIndexOfValue(o) > 0;
                }

                @Override
                public boolean remove(Object o) {
                    int tmpIndex = subIndexOfValue(o);
                    return tmpIndex > 0 && IndexMap.this.remove(IndexMap.this.key(tmpIndex)) != null;
                }
            };
        }

        @Override
        public Set<Entry<Integer, V>> entrySet() {
            return new SubIndexSet<Entry<Integer, V>>(_headLimit, _tailLimit) {

                @Override
                public Iterator<Entry<Integer, V>> iterator() {
                    return new SubIterator<Entry<Integer, V>>(_headLimit, _tailLimit) {

                        @Override
                        public Entry<Integer, V> next() {
                            checkForCoModification();
                            if (!hasNext()) throw new NoSuchElementException();
                            skip();
                            return nextEntry();
                        }
                    };
                }

                @Override
                public boolean contains(Object o) {
                    Entry<Integer, V> entry = (Entry<Integer, V>) o;
                    int key = entry.getKey();
                    if (!inSubRange(key)) return false;
                    V v = IndexMap.this.get(key);
                    return v != null && v.equals(entry.getValue());
                }

                @Override
                public boolean remove(Object o) {
                    Entry<Integer, V> entry = (Entry<Integer, V>) o;
                    int key = entry.getKey();
                    if (!inSubRange(key)) return false;
                    IndexMap.this.remove(key);
                    return true;
                }
            };
        }

        @Override
        public int hashCode() {
            return IndexMap.this.hashCode(entrySet());
        }

        @Override
        public boolean equals(Object obj) {
            return IndexMap.this.equals(obj, this);
        }

        @Override
        public String toString() {
            return IndexMap.this.toString(entrySet(), this);
        }
    }

}
