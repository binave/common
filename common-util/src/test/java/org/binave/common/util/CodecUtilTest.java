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

    /**
     * 使用 oathtool 验证 TOTP 生成是否符合标准
     *
     * oathtool 命令说明:
     * - 生成当前时间的 TOTP: oathtool --base32 --totp <seed>
     * - 生成指定计数器的 TOTP: oathtool --base32 --totp -c <counter> <seed>
     */
    @Test
    public void testPasscodeWithOathtool() {
        String seed = "QD3GUF7F77HJKQW5GLWATRQCYA";
        TOTP generator = CodecUtil.generateTOTP(seed);

        long time = System.currentTimeMillis();
        long counter = time / 30000; // 30秒时间步长
        String code = generator.generateTOTP(time);

        System.out.println("Seed: " + seed);
        System.out.println("Time: " + time);
        System.out.println("Counter: " + counter);
        System.out.println("Generated TOTP: " + code);
        System.out.println();
        System.out.println("使用 oathtool 验证:");
        System.out.println("  oathtool --base32 --totp -c " + counter + " " + seed);

        // 执行 oathtool 命令进行验证
        try {
            ProcessBuilder pb = new ProcessBuilder("oathtool", "--base32", "--totp", "-c", String.valueOf(counter), seed);
            pb.redirectErrorStream(true);
            Process process = pb.start();

            java.io.BufferedReader reader = new java.io.BufferedReader(
                    new java.io.InputStreamReader(process.getInputStream())
            );
            String oathtoolResult = reader.readLine().trim();
            int exitCode = process.waitFor();

            System.out.println("  oathtool result: " + oathtoolResult);
            System.out.println("  exit code: " + exitCode);
            System.out.println();
            System.out.println("验证结果: " + (code.equals(oathtoolResult) ? "✓ 一致" : "✗ 不一致"));
        } catch (Exception e) {
            System.out.println("无法执行 oathtool，请确保已安装:");
            System.out.println("  - Windows: choco install oathtool");
            System.out.println("  - Linux/Mac: brew install oathtool");
            System.out.println("  错误: " + e.getMessage());
        }
    }

}
