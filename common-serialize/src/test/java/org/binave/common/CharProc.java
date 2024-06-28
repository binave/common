package org.binave.common;

import javax.xml.bind.DatatypeConverter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.zip.GZIPOutputStream;

/**
 *
 * 字符处理工具
 * Character Process Util
 */
public class CharProc {

    private static MessageDigest messageDigest;



    /**
     * @param text 要压缩的字符串
     * @return 压缩后的字符串，将超出可见字符集
     */
    public static String gzip(String text) {

        if (text == null || text.length() == 0)
            return null;

        try (ByteArrayOutputStream bao = new ByteArrayOutputStream()) {
            try (GZIPOutputStream gos = new GZIPOutputStream(bao)) {
                gos.write(text.getBytes("UTF-8"));
                return bao.toString("ISO-8859-1");
            }

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }


    /**
     * 获得随机的 base64 字符串
     *
     * @param l 1 ~ 24
     * @return base64
     */
    public static String getBase64UUID(int l) {
        if (l < 1 || l > 24)
            return null;
        UUID uuid = UUID.randomUUID();
        return DatatypeConverter.printBase64Binary(
                longsToByteArray(
                        uuid.getLeastSignificantBits(),
                        uuid.getMostSignificantBits()
                )
        ).substring(0, l);
    }

    private static char[] map;

    /**
     * 获得数字对应的 base64 编码
     *
     * @param i > 0 正整数 或 0
     * @param l > 0 要获得的字符串长度
     */
    public static String getBase64(int i, int l) {
        if (i < 0 || l < 1)
            return null;
        if (map == null)
            map = initEncodeMap();
        StringBuilder sb = new StringBuilder();
        do {
            sb.insert(0, map[i % 64]);
        } while ((i /= 64) > 0);
        while (sb.length() < l) {
            sb.insert(0, map[0]);
        }
        return sb.toString().substring(0, l);
    }

    /**
     * @param url URL 路径
     * @return 分级后的路径数组
     */
    public static List<String> url2List(String url) {

        StringBuilder context = new StringBuilder(url);
        List<String> list = new ArrayList<>();

        int index = 0;
        int offset = -1;

        while (index != -1) {
            index = context.indexOf("/", offset + 1);
            if (index - offset > 1) {
                list.add(context.substring(offset + 1, index));
                offset = index;
            } else ++offset;
        }

        if (list.size() > 0) {
            index = context.indexOf("?");
            list.add(context.substring(offset, index != -1 ? index : url.length()));
            return list;
        } else return null;
    }



    private static Map<Character, Integer> dMap;

    /**
     * 将 base64 字符串变成 16 进制数
     *
     * @param sourceStr 要转换的字符
     * @param len       要转换的长度， 大于 0，从左边开始，小于 0 从右侧开始
     */
    public static long base64ToNum(String sourceStr, int len) {
        int sourceStrLen;
        if (sourceStr == null || (sourceStrLen = sourceStr.length()) == 0 || len == 0)
            return -1;
        if (dMap == null)
            dMap = initDecodeMap();
        String subStr = len > 0
                ? sourceStr.substring(0, len > sourceStrLen ? sourceStrLen : len)
                : sourceStr.substring(
                sourceStrLen + len > 0 ? sourceStrLen + len : 0,
                sourceStrLen);
        long num = 0;
        for (int i = 0; i < subStr.length(); i++) {
            Integer charValue = dMap.get(subStr.charAt(i));
            if (charValue == null)
                return -1;
            num *= 64;
            num += charValue;
        }
        return num;
    }

    /**
     * @see java.security.SecureRandom
     */
    private static byte[] longsToByteArray(long... l) {
        if (l == null)
            return new byte[0];
        byte[] retVal = new byte[8 * l.length];

        for (int i = 0; i < 8 * l.length; i++) {
            retVal[i] = (byte) l[i / 8];
            l[i / 8] >>= 8;
        }
        return retVal;
    }

    /**
     * @see javax.xml.bind.DatatypeConverterImpl
     */
    private static char[] initEncodeMap() {
        char[] map = new char[64];
        int i;
        for (i = 0; i < 26; i++)
            map[i] = (char) ('A' + i);
        for (i = 26; i < 52; i++)
            map[i] = (char) ('a' + (i - 26));
        for (i = 52; i < 62; i++)
            map[i] = (char) ('0' + (i - 52));
        map[62] = '+';
        map[63] = '/';

        return map;
    }


    private static Map<Character, Integer> initDecodeMap() {
        Map<Character, Integer> map = new HashMap<Character, Integer>();
        for (int i = 0; i < 26; i++)
            map.put((char) ('A' + i), i);
        for (int i = 26; i < 52; i++)
            map.put((char) ('a' + (i - 26)), i);
        for (int i = 52; i < 62; i++)
            map.put((char) ('0' + (i - 52)), i);
        map.put('+', 62);
        map.put('/', 63);

        return map;
    }


    /**
     * 获得当前时间戳
     */
    public static String getFormatTime() {
        return getFormatTime("yyyyMMddHHmmss");
    }

    /**
     * 获得当前时间戳
     */
    public static String getFormatTime(String format) {
        return getFormatTime(format, new Date());
    }

    /**
     * 获得当前时间戳
     */
    public static String getFormatTime(String format, Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        return sdf.format(date);
    }


}