package org.binave.common.serialize;

import org.junit.Test;

import java.util.Map;

/**
 * For ASM
 *
 * @author by bin jin on 2017/5/2.
 */
public class Foo<SS> extends OO<SS> {

    private int Testi = 0;

    public long i;

    private String f = "";

    private String[] fs;

    private Map<String, OO> tt;

    @Test
    public static <T extends Map> void execute(T t) throws RuntimeException {
        System.out.println("test changed method name");
    }

    public Object[] getParams() {
        return new Object[]{f, fs, tt};
    }

    public static void execute() throws RuntimeException {
        System.out.println("test changed method name");
    }

    public static void changeMethodContent() {
        System.out.println("test change method");
    }

    public int get1(int f, boolean i, Object o, double r) {
        return 0;
    }

    public Integer get2() {
        return null;
    }
}
