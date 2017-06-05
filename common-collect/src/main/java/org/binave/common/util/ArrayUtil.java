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

package org.binave.common.util;

import java.lang.reflect.Array;

/**
 * 对数组的一些操作
 *
 * @author bin jin
 * @since 1.8
 */
public class ArrayUtil {

    public static <T> T[] offsetCopyOf(T[] original, int offset) {
        return (T[]) offsetCopyOf(original, offset, original.getClass());
    }

    /**
     * 向数组左面加入空位
     *
     * @param original  原数组
     * @param offset    向右移动的位移
     * @param newType   新数组类型
     * @return 新数组
     */
    public static <T, U> T[] offsetCopyOf(U[] original, int offset, Class<? extends T[]> newType) {
        if (offset < 1) throw new IllegalArgumentException("offset: " + offset);
        return offsetCopyOf(original, offset, offset + original.length, newType);
    }

    /**
     * 向数组左面加入空位
     *
     * @param original  原数组
     * @param offset    向右移动的位移
     * @param newLength 新长度
     * @param newType   新数组类型
     * @return 新数组
     */
    public static <T, U> T[] offsetCopyOf(U[] original, int offset, int newLength, Class<? extends T[]> newType) {
        if (offset < 0) throw new IllegalArgumentException("offset: " + offset);
        // 注意：(Object) newType 的 (Object) 不可省略，否则可能会无法编译通过
        T[] copy = ((Object) newType == (Object) Object[].class)
                ? (T[]) new Object[newLength]
                : (T[]) Array.newInstance(newType.getComponentType(), newLength);
        System.arraycopy(original, 0, copy, offset, original.length);
        return copy;
    }

    /**
     * 向 byte[] 数组左面加入空位
     *
     * @param original  原数组
     * @param offset    向右移动的位移
     * @param newLength 新长度
     * @return 新数组
     */
    public static byte[] offsetCopyOf(byte[] original, int offset, int newLength) {
        if (offset < 0) throw new IllegalArgumentException("offset: " + offset);
        byte[] copy = new byte[newLength];
        System.arraycopy(original, 0, copy, offset, original.length);
        return copy;
    }

}
