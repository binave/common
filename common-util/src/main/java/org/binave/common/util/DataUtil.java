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


/**
 * @author by bin jin on 2017/5/11.
 * @since 1.8
 */
public class DataUtil {

    /**
     * 方便计算位移
     */
    public final static int LONG_FACTOR = Long.SIZE / Byte.SIZE;
    public final static int DOUBLE_FACTOR = Double.SIZE / Byte.SIZE;
    public final static int INT_FACTOR = Integer.SIZE / Byte.SIZE;
    public final static int FLOAT_FACTOR = Float.SIZE / Byte.SIZE;
    public final static int CHAR_FACTOR = Character.SIZE / Byte.SIZE;
    public final static int SHORT_FACTOR = Short.SIZE / Byte.SIZE;
    public final static int BOOLEAN_FACTOR = Byte.SIZE / Byte.SIZE;

    /**
     * 从 byte[] 中读取 long
     *
     * @param offset    数组下标位移
     */
    public static long readLong(byte[] data, int offset) {
        return readNumber(data, offset, Long.SIZE);
    }

    /**
     * 从 byte[] 中读取 double
     *
     * @param offset    数组下标位移
     */
    public static double readDouble(byte[] data, int offset) {
        return Double.longBitsToDouble(readNumber(data, offset, Double.SIZE));
    }

    /**
     * 从 byte[] 中读取 int
     *
     * @param offset    数组下标位移
     */
    public static int readInt(byte[] data, int offset) {
        return (int) readNumber(data, offset, Integer.SIZE);
    }

    /**
     * 从 byte[] 中读取 float
     *
     * @param offset    数组下标位移
     */
    public static float readFloat(byte[] data, int offset) {
        return Float.intBitsToFloat((int) readNumber(data, offset, Float.SIZE));
    }

    /**
     * 从 byte[] 中读取 char
     *
     * @param offset    数组下标位移
     */
    public static char readChar(byte[] data, int offset) {
        return (char) readNumber(data, offset, Character.SIZE);
    }

    /**
     * 从 byte[] 中读取 short
     *
     * @param offset    数组下标位移
     */
    public static short readShort(byte[] data, int offset) {
        return (short) readNumber(data, offset, Short.SIZE);
    }

    /**
     * 从 byte[] 中读取 boolean
     * 任何非 0 数值为 true
     *
     * @param offset    数组下标位移
     */
    public static boolean readBoolean(byte[] data, int offset) {
        return readNumber(data, offset, Byte.SIZE) > 0;
    }

    private static long readNumber(byte[] data, int offset, int size) {
        long l = 0;
        for (int i = offset; i < size / Byte.SIZE + offset; i++) {
            l <<= Byte.SIZE;
            l |= (data[i] & 0xFF);
        }
        return l;
    }

    /**
     * 从 byte[] 左边开始写入 long
     *
     * @param   offset      数组下标位移
     */
    public static byte[] writeLong(byte[] data, int offset, long l) {
        return writeNumber(data, offset, Long.SIZE, l);
    }

    /**
     * 从 byte[] 左边开始写入 double
     *
     * @param   offset      数组下标位移
     */
    public static byte[] writeDouble(byte[] data, int offset, double d) {
        return writeNumber(data, offset, Double.SIZE, Double.doubleToLongBits(d));
    }

    /**
     * 从 byte[] 左边开始写入 int
     *
     * @param   offset      数组下标位移
     */
    public static byte[] writeInt(byte[] data, int offset, int i) {
        return writeNumber(data, offset, Integer.SIZE, i);
    }

    /**
     * 从 byte[] 左边开始写入 float
     *
     * @param   offset      数组下标位移
     */
    public static byte[] writeFloat(byte[] data, int offset, float f) {
        return writeNumber(data, offset, Float.SIZE, Float.floatToIntBits(f));
    }

    /**
     * 从 byte[] 左边开始写入 char
     *
     * @param   offset      数组下标位移
     */
    public static byte[] writeChar(byte[] data, int offset, char c) {
        return writeNumber(data, offset, Character.SIZE, c);
    }

    /**
     * 从 byte[] 左边开始写入 short
     *
     * @param   offset      数组下标位移
     */
    public static byte[] writeShort(byte[] data, int offset, short i) {
        return writeNumber(data, offset, Short.SIZE, i);
    }

    /**
     * 从 byte[] 左边开始写入 boolean
     *
     * @param   offset      数组下标位移
     */
    public static byte[] writeBoolean(byte[] data, int offset, boolean b) {
        return writeNumber(data, offset, Byte.SIZE, b ? 1 : 0);
    }

    private static byte[] writeNumber(byte[] data, int offset, int size, long l) {
        for (int i = size / Byte.SIZE - 1 + offset; i >= offset; i--) {
            data[i] = (byte) (l & 0xFF);
            l >>= Byte.SIZE;
        }
        return data;
    }
}
