package org.binave.common.otp;

/**
 * Time-Based One-Time Password
 * TOTP 时间相关的密码生成器（数字令牌算法）
 *
 * @author by bin jin on 2019-10-09 15:22.
 */
public interface TOTP {

    String generateTOTP(long state);

    boolean verifyCode(long state, String code);

}
