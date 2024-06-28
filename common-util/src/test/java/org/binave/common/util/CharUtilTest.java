package org.binave.common.util;

import org.junit.Test;

/**
 * @author by bin jin on 2019/03/21 0:30.
 */
public class CharUtilTest {

    @Test
    public void lcsTest() {

        System.out.println(
                CharUtil.reverse(123312312L)
        );

        System.out.println(
                CharUtil.lcs("1234567", "456789")
        );
        System.out.println(
                CharUtil.lcs("abc_1112", "bcd_11122")
        );
        System.out.println(
                CharUtil.lcs("fff", "1x2")
        );
    }

}
