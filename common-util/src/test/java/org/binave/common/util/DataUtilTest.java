package org.binave.common.util;

import org.junit.Test;

import java.io.PrintStream;
import java.security.SecureRandom;
import java.util.Random;

/**
 * @author by bin jin on 2017/5/13.
 * @since 1.8
 */
public class DataUtilTest extends DataUtil {

    @Test
    public void test11() {

        Random ran = new SecureRandom();

        byte[] data = new byte[0];
        for (int n = 0; n < 1000; n++) {

            data = ArrayUtil.offsetCopyOf(data, LONG_FACTOR, data.length + LONG_FACTOR); // 扩大一个 long
            long l = ran.nextLong() * (ran.nextBoolean() ? -1 : 1);
            data = DataUtil.writeLong(data, 0, l); // 写入一个 long

            data = ArrayUtil.offsetCopyOf(data, DOUBLE_FACTOR, data.length + DOUBLE_FACTOR); // 扩大一个 double
            double d = ran.nextDouble() * (ran.nextBoolean() ? -1 : 1);
            data = DataUtil.writeDouble(data, 0, d);

            data = ArrayUtil.offsetCopyOf(data, INT_FACTOR, data.length + INT_FACTOR); // 扩大一个 int
            int i = ran.nextInt() * (ran.nextBoolean() ? -1 : 1);
            data = DataUtil.writeInt(data, 0, i); // 写入一个 int

            data = ArrayUtil.offsetCopyOf(data, FLOAT_FACTOR, data.length + FLOAT_FACTOR); // 扩大一个 float
            float f = ran.nextFloat() * (ran.nextBoolean() ? -1 : 1);
            data = DataUtil.writeFloat(data, 0, f);

            data = ArrayUtil.offsetCopyOf(data, CHAR_FACTOR, data.length + CHAR_FACTOR); // 扩大一个 char
            char c = (char) (ran.nextInt() * (ran.nextBoolean() ? -1 : 1));
            data = DataUtil.writeChar(data, 0, c);

            data = ArrayUtil.offsetCopyOf(data, SHORT_FACTOR, data.length + SHORT_FACTOR); // 扩大一个 short
            short s = (short) (ran.nextInt() * (ran.nextBoolean() ? -1 : 1));
            data = DataUtil.writeShort(data, 0, s);

            data = ArrayUtil.offsetCopyOf(data, BOOLEAN_FACTOR, data.length + BOOLEAN_FACTOR); // 扩大一个 boolean
            boolean b = ran.nextBoolean();
            data = DataUtil.writeBoolean(data, 0, b);

            ////////////////////////

            int offset = 0;

            boolean b2 = DataUtil.readBoolean(data, offset);
//            data = Arrays.copyOfRange(data, BOOLEAN_FACTOR, data.length);
            equalsNumber(b ? 1 : 0, b2 ? 1 : 0, "Boolean");

            offset += BOOLEAN_FACTOR;

            short s2 = DataUtil.readShort(data, offset);
//            data = Arrays.copyOfRange(data, SHORT_FACTOR, data.length);
            equalsNumber(s, s2, "Short");

            offset += SHORT_FACTOR;

            char c2 = DataUtil.readChar(data, offset);
//            data = Arrays.copyOfRange(data, CHAR_FACTOR, data.length);
            equalsNumber((int) c, (int) c2, "Char");

            offset += CHAR_FACTOR;

            float f2 = DataUtil.readFloat(data, offset);
//            data = Arrays.copyOfRange(data, FLOAT_FACTOR, data.length);
            equalsNumber(f, f2, "Float");

            offset += FLOAT_FACTOR;

            int i2 = DataUtil.readInt(data, offset);
//            data = Arrays.copyOfRange(data, INT_FACTOR, data.length);
            equalsNumber(i, i2, "Int");

            offset += INT_FACTOR;

            double d2 = DataUtil.readDouble(data, offset);
//            data = Arrays.copyOfRange(data, DOUBLE_FACTOR, data.length);
            equalsNumber(d, d2, "Double");

            offset += DOUBLE_FACTOR;

            long l2 = DataUtil.readLong(data, offset);
//            data = Arrays.copyOfRange(data, LONG_FACTOR, data.length);
            equalsNumber(l, l2, "Long");

            System.out.println();
        }
    }

    private void equalsNumber(Number n1, Number n2, String prefix) {
        boolean status = n1.equals(n2);
        PrintStream out = status ? System.out : System.err;
        out.println(status + " " + prefix + " " + n1 + ":" + n2);
        out.flush();
    }


}
