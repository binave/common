package org.binave.common.otp;

/**
 * 时间相关的密码生成器
 *
 * @author by bin jin on 2019-10-09 15:22.
 */
public interface TemporalPasscodeGenerator {

    String generateCode(long state);

    boolean verifyCode(long state, String code);

}
