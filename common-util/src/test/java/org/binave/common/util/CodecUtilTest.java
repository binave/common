package org.binave.common.util;

import org.binave.common.otp.TOTP;
import org.junit.Test;

/**
 * @author by bin jin on 2019-09-16 13:14.
 */
public class CodecUtilTest {

    @Test
    public void testHash() {
        System.out.println(
                CodecUtil.Hash.SHA1.hash("123")
        );
        System.out.println(
                CodecUtil.Hash.MD5.hash("123")
        );
    }

    /**
     * 测试数字令牌
     */
    @Test
    public void testPasscode() {
        TOTP generator6 = CodecUtil.
                generateTOTP("QD3GUF7F77HJKQW5GLWATRQCYA");

        TOTP generator4 = CodecUtil.
                generateTOTP("QD3GUF7F77HJKQW5GLWATRQCYA", 4);

        long time = System.currentTimeMillis();
        String code6 = generator6.generateTOTP(time);
        String code4 = generator4.generateTOTP(time);
        System.out.printf("time: %s => %s/%s, %s/%s%n", time, code6, code4, generator6.verifyCode(time, code6), generator4.verifyCode(time, code4));

//        ++time;
//
//        System.out.println();
//
//        for (long l = time; l <= time + 10000; l++) {
//            code9 = generator6.generateTOTP(l);
//            code4 = generator4.generateTOTP(l);
//            System.out.printf("time: %s => %s/%s, %s/%s%n", l, code9, code4, generator6.verifyCode(l, code9), generator4.verifyCode(l, code4));
//
//        }

    }

}
