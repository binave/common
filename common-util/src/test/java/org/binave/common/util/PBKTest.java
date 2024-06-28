package org.binave.common.util;

import org.junit.Test;


/**
 * @author by bin jin on 2017/3/29.
 */
public class PBKTest {

    /**
     * 测试密码验证
     */
    @Test
    public void test1() {
        byte[] salt = CodecUtil.getSalt();
        byte[] cipher = CodecUtil.getCipherText("123", salt);
        System.out.println(CodecUtil.authenticate(salt, "123", cipher));
    }

    @Test
    public void test2() {

        for (int j = 1; j < 1000; j++) {
            byte[] b = CharUtil.toBytes(j);
            System.out.println(
                    "\nhashcode " + Long.hashCode(j)
            );
            System.out.println(
                    "murmur3  " + Long.hashCode(CodecUtil.ConsistentHash.MURMUR3.hash(b, 0, b.length)) + ", " + j
            );
            System.out.println(
                    "crc32    " + Long.hashCode(CodecUtil.ConsistentHash.CRC32.hash(b, 0, b.length)) + ", " + j
            );
        }

    }


}
