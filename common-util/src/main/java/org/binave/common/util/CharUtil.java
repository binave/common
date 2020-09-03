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

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * 一些字符处理的工具
 *
 * @author bin jin
 * @since 1.8
 */
public class CharUtil {

    /* ******** format ******** */

    /**
     * 将字符串中指定占位符包含的部分，替换成传入的参数
     *
     * \@param allowPlaceholderTextSurplus 允许占位符中有其他字符
     * @param allowParametersSurplus    允许传入参数剩余
     * @param allowPlaceholderSurplus   允许占位符剩余
     * @param prefixSeparator           前占位符
     * @param suffixSeparator           后占位符
     * @param format                    带占位符的格式化字符
     */
    public static String resolvePlaceholders(/*boolean allowPlaceholderTextSurplus,*/
            boolean allowParametersSurplus, boolean allowPlaceholderSurplus,
            String prefixSeparator, String suffixSeparator, String format, Object... args) {

        if (prefixSeparator == null || suffixSeparator == null || format == null || args == null)
            throw new IllegalArgumentException(
                    replacePlaceholders("?",
                            "argument: prefixSeparator=?, suffixSeparator=?, format=?, args=?",
                            prefixSeparator, suffixSeparator, format, Arrays.toString(args))
            );

        StringBuilder sb = new StringBuilder(format);
        int argsIndex = 0;
        int prefixIndex = sb.indexOf(prefixSeparator);

        while (prefixIndex != -1) {
            int suffixIndex = sb.indexOf(suffixSeparator, prefixIndex + prefixSeparator.length());
            if (suffixIndex != -1 && argsIndex < args.length) {

                /*
                if (!allowPlaceholderTextSurplus &&
                        sb.substring(prefixIndex + prefixSeparator.length(), suffixIndex).length() > 0)
                    throw new IllegalArgumentException(
                            exceptionText("Placeholder have text", prefixSeparator, suffixSeparator, format, args)
                    );
                */

                String arg = args[argsIndex] != null ? args[argsIndex].toString() : "NULL";
                sb.replace(prefixIndex, suffixIndex + suffixSeparator.length(), arg);
                prefixIndex = sb.indexOf(prefixSeparator, prefixIndex + arg.length());
                ++argsIndex;

            } else if (!allowPlaceholderSurplus && argsIndex >= args.length)
                throw new IllegalArgumentException(
                        exceptionText("Placeholder surplus", prefixSeparator, suffixSeparator, format, args)
                );
            else prefixIndex = -1;

        }

        if (!allowParametersSurplus && argsIndex < args.length)
            throw new IllegalArgumentException(
                    exceptionText("Parameters surplus", prefixSeparator, suffixSeparator, format, args)
            );

        return sb.toString();
    }

    /**
     * 将字符串中指定占位符包含的部分，替换成传入的参数
     *
     * @param prefixSeparator           前占位符
     * @param suffixSeparator           后占位符
     * @param format                    带占位符的格式化字符
     */
    public static String resolvePlaceholders(String prefixSeparator, String suffixSeparator, String format, Object... args) {
        return resolvePlaceholders(/*true,*/ false, false, prefixSeparator,
                suffixSeparator, format, args);
    }

    private static String exceptionText(String subject, String prefixSeparator, String suffixSeparator,
                                        String format, Object... args) {
        return replacePlaceholders("?", "?: prefixSeparator=?, suffixSeparator=?, format=?, args=?",
                subject, prefixSeparator, suffixSeparator, format, Arrays.toString(args));
    }

    /**
     * 替换字符串中的占位符为数组元素
     *
     * @param allowParametersSurplus 允许传入参数剩余
     * @param allowPlaceholderSurplus 允许占位符剩余
     * @param separator 占位符
     * @param format 带占位符的格式化字符
     */
    public static String replacePlaceholders(boolean allowParametersSurplus, boolean allowPlaceholderSurplus,
                                             String separator, String format, Object... args) {
        if (separator == null || format == null || args == null)
            throw new IllegalArgumentException(
                    exceptionText("argument", separator, format, args)
            );

        StringBuilder sb = new StringBuilder(format);
        int argsIndex = 0;
        int placeIndex = sb.indexOf(separator);

        while (placeIndex != -1) {
            if (argsIndex < args.length) {
                String arg = args[argsIndex] != null ? args[argsIndex].toString() : "NULL";
                sb.replace(placeIndex, placeIndex + separator.length(), arg);
                placeIndex = sb.indexOf(separator, placeIndex + arg.length());
                ++argsIndex;

            } else if (!allowPlaceholderSurplus && argsIndex >= args.length)
                throw new IllegalArgumentException(
                        exceptionText("Placeholder surplus", separator, format, args)
                );

            else placeIndex = -1;
        }

        if (!allowParametersSurplus && argsIndex < args.length)
            throw new IllegalArgumentException(
                    exceptionText("Parameters surplus", separator, format, args)
            );
        return sb.toString();
    }

    /**
     * 将字符串中指定占位符，替换成参数。
     * 占位符数量与参数数量不许一致
     *
     * @param separator 占位符
     * @param format    包含占位符的字符串
     */
    public static String replacePlaceholders(String separator, String format, Object... args) {
        return replacePlaceholders(false, false, separator, format, args);
    }

    /**
     * 将字符串中的 {} 替换成参数
     *
     * @see java.text.MessageFormat#subformat
     *
     * @param format    包含占位符 {} 的字符串
     * @param args      {@link Object#toString()}
     */
    public static String format(String format, Object... args) {
        return replacePlaceholders(false, true, "{}", format, args);
    }

    /**
     * @param subject 主题
     */
    private static String exceptionText(String subject, String separator, String format, Object... args) {
        return replacePlaceholders("?", "?: separator=?, format=?, args=?",
                subject, separator, format, Arrays.toString(args));
    }

    /**
     * 使用指定分隔符，拼接重复字符
     * @param separator 分隔符
     * @param text      字符
     * @param count     数量
     */
    public static String join(String separator, Object text, int count) {
        int iMax = count - 1;
        if (text == null || iMax <= -1) return null;

        StringBuilder b = new StringBuilder();
//        b.append(' ');
        for (int i = 0; ; i++) {
            b.append(text);
            if (i == iMax) return b.toString();
            b.append(separator);
        }
    }

    public static String join(String separator, Object[] a) {
        return join(separator, null, a, null);
    }

    /**
     * @see Arrays#toString
     */
    public static String join(String separator, String prefix, Object[] a, String suffix) {
        if (a == null) return null;
        int iMax = a.length - 1;

        separator = separator == null ? "" : separator;
        prefix = prefix == null ? "" : prefix;
        suffix = suffix == null ? "" : suffix;

        if (iMax == -1) return null;

        StringBuilder b = new StringBuilder();
//        b.append(' ');
        for (int i = 0; ; i++) {
            b.append(prefix).append(a[i]).append(suffix);
            if (i == iMax) return b.toString();
            b.append(separator);
        }
    }

    public static String join(String separator, Collection c) {
        return join(separator, null, c, null);
    }

    public static String join(String separator, String prefix, Collection c, String suffix) {
        if (c == null || c.size() == 0)
            return null;

        separator = separator == null ? "" : separator;
        prefix = prefix == null ? "" : prefix;
        suffix = suffix == null ? "" : suffix;

        StringBuilder b = new StringBuilder();

        Iterator i = c.iterator();
        b.append(prefix).append(i.next()).append(suffix);
        while (i.hasNext()) b.append(separator).
                append(prefix).append(i.next()).append(suffix);
        return b.toString();
    }

    /* ******** read ******** */

    /**
     * 从文件读取全部文本内容
     */
    public static String readText(String path) {
        try {
            return readText(new FileInputStream(path));
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 从文件读取全部文本内容
     */
    public static String readText(File file) {
        try {
            return readText(new FileInputStream(file));
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private static String readText(InputStream in) {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(in))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) sb.append(line);
            return sb.toString();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /* ******** sub ******** */

    /**
     * 获得匹配的第一个标签内容（不含属性）
     *
     * @param xml xml 文本内容
     * @param tag 标签名称
     * @return 标签体内容
     */
    public static List<String> getLabelText(String xml, String tag) {
        List<String> list = new ArrayList<>();

        String tagPre = '<' + tag;
        String tagSuf = "</" + tag; // 此处不支持 </ tag> 这种形式

        int tagLen = tag.length() + 1; // labPre 长度
        int tagIndex = xml.indexOf(tagPre);
        int sufIndex; // 结尾坐标

        while (tagIndex != -1) {
            sufIndex = tagIndex;
            char c = xml.charAt(tagIndex + tagLen);
            if (c == ' ' || c == '>') {
                int preIndex;
                if ((preIndex = xml.indexOf(">", tagIndex + tagLen)) < 0) break;
                if ((sufIndex = xml.indexOf(tagSuf, preIndex + 1)) < 0) break;
                list.add(xml.substring(preIndex + 1, sufIndex));
            }
            tagIndex = xml.indexOf(tagPre, sufIndex + tagLen + 1);
        }
        return list;
    }

    /**
     * 对字符串进行切分
     *
     * @param str   [,;|]
     */
    public static List<String> splitString(String str, char... separators) {
        if (str == null || str.length() == 0)
            return new ArrayList<>(0);
        if (separators == null) throw new IllegalArgumentException("separators is null");

        List<String> list = new ArrayList<>();

        int p = 0;
        int i = 0;

        switch (separators.length) {
            case 1:
                for (; i < str.length(); i++) {
                    int c = str.charAt(i);
                    if (c == separators[0]) {
                        if (i - p > 0) list.add(str.substring(p, i));
                        p = i + 1;
                    }
                }
                break;
            case 2:
                for (; i < str.length(); i++) {
                    int c = str.charAt(i);
                    if (c == separators[0] || c == separators[1]) {
                        if (i - p > 0) list.add(str.substring(p, i));
                        p = i + 1;
                    }
                }
                break;
            case 3:
                for (; i < str.length(); i++) {
                    int c = str.charAt(i);
                    if (c == separators[0] || c == separators[1] || c == separators[2]) {
                        if (i - p > 0) list.add(str.substring(p, i));
                        p = i + 1;
                    }
                }
                break;
            case 4:
                for (; i < str.length(); i++) {
                    int c = str.charAt(i);
                    if (c == separators[0] || c == separators[1] || c == separators[2] || c == separators[3]) {
                        if (i - p > 0) list.add(str.substring(p, i));
                        p = i + 1;
                    }
                }
                break;
            case 5:
                for (; i < str.length(); i++) {
                    int c = str.charAt(i);
                    if (c == separators[0] || c == separators[1] ||
                            c == separators[2] || c == separators[3] || c == separators[4]) {
                        if (i - p > 0) list.add(str.substring(p, i));
                        p = i + 1;
                    }
                }
                break;
            default:
                throw new UnsupportedOperationException(String.valueOf(separators.length));
        }

        if (i - p > 0) list.add(str.substring(p, i));
        return list;
    }

    /**
     * get longest common sub sequence
     * 取得两个字符串的最大公共子串
     *
     * * 时间复杂度相当，但无需数组，空间占用固定，不会随着字符串不同而变化
     *
     * 12345678      7 - 7   * 12345     4 - 4
     *        abcd   0 - 0   *     abcde 0 - 0
     * 12345678      6 - 7   * 12345     3 - 4
     *       abcd    0 - 1   *    abcde  0 - 1
     * 12345678      5 - 7   * 12345     2 - 4
     *      abcd     0 - 2   *   abcde   0 - 2
     * 12345678      4 - 7   * 12345     1 - 4
     *     abcd      0 - 3   *  abcde    0 - 3
     * 12345678      3 - 6   * 12345     0 - 4
     *    abcd       0 - 3   * abcde     0 - 4
     * 12345678      2 - 5   *  12345    0 - 3
     *   abcd        0 - 3   * abcde     1 - 4
     * 12345678      1 - 4   *   12345   0 - 2
     *  abcd         0 - 3   * abcde     2 - 4
     * 12345678      0 - 3   *    12345  0 - 1
     * abcd          0 - 3   * abcde     3 - 4

     #  12345678     0 - 2   #     12345 0 - 0
     # abcd          1 - 3   # abcde     4 - 4
     *   12345678    0 - 1
     * abcd          2 - 3
     *    12345678   0 - 0
     * abcd          3 - 3
     * @return 公共子串，拥有多个长度相同的，则只返回第一个匹配的，没有则返回 null
     */
    public static String lcs(String str1, String str2) {

        if (str1 == null || str2 == null)
            throw new IllegalArgumentException();

        int str1Len = str1.length(), str2Len = str2.length();
        if (str1Len == 0 || str2Len == 0) return null;

        boolean stat = false;
        int m, n, y = 0, len = 0, pre = 0, sub, count;

        // 执行如上图的错位移动
        for (int x = str1Len - 1; x >= -1 * str2Len + 1; x--) {
            if (x >= 0) {
                m = x;
                n = 0;
            } else {
                m = 0;
                n = x * -1;
            }

            // 右下标
            if (y < str2Len) ++y;

            // 进行重合部分的比较
            sub = m;
            count = 0;
            for (int i = 0; i < y - n; i++) {
                if (str1.charAt(m + i) == str2.charAt(n + i)) {
                    if (!stat) {
                        sub = m + i; // 记录连续字符串起点未知
                        count = 0;   // 初始化公共子串长度计数
                        stat = true;
                    }
                    ++count;
                } else {
                    stat = false;
                    if (count > len) { // 比原来的更长
                        pre = sub;     // 记录起点
                        len = count;
                    }
                }
            }
            // 处理末尾字符
            if (count > len) {
                pre = sub;
                len = count;
            }

        }
        return len == 0 ? null : str1.substring(pre, pre + len);
    }

    /**
     * 获得文本中的 10 进制数字
     * 支持负数
     * 支持从后向前查询
     *
     * e.g.
     *      getInteger("100_20_3", -2, -1) == 20
     *      getInteger("100_20_3", 0, 1) == 20
     *      getInteger("100_20_3", 3, 0) == 20
     *
     * @param fromIndex     指定字符串初始下标 0 ~ -1
     * @param numberIndex   数字下标 0 ~ -1
     */
    public static int getInteger(String context, int fromIndex, int numberIndex) {

        int offset;

        // 计算开始坐标
        fromIndex = fromIndex >= 0 ? fromIndex : context.length() + fromIndex;

        if (numberIndex >= 0) {
            int number = 0; // 要拿到的数字
            int position = context.length(); // 缓存位置，每次数字字符都要执行，用于判断上一次字符类型
            offset = -1; // 从左向右第几个连续数字
            boolean negative = false; // 是否时负数

            for (int i = fromIndex; i < context.length(); i++) {
                int c = context.charAt(i);
                if (c >= '0' && c <= '9') {
                    if (i - position == 1) {// 上次还是数字
                        if (offset == numberIndex) {
                            number *= 10;
                            number += c - '0';
                        }
                    } else { // 上次是非数字字符，这次是数字字符
                        negative = i > 0 && c != '0' && context.charAt(i - 1) == '-';
                        ++offset; // 找的第几个连续数字字符
                        number = c - '0';
                    }
                    position = i;
                } else if (offset == numberIndex && i - position == 1) {
                    // 说明上一次是数字字符，这次是非数字字符
                    return negative ? number * -1 : number;
                }
            }

            if (offset == numberIndex) return negative ? number * -1 : number;

        } else {
            // 从右向左遍历
            int number = 0; // 要拿到的数字
            int position = 0; // 缓存位置，每次数字字符都要执行，用于判断上一次字符类型
            offset = 0; // 从右向左第几个连续数字
            int power = 1;

            for (int i = fromIndex; i >= 0; i--) { // 从后向前遍历
                int c = context.charAt(i);
                if (c >= '0' && c <= '9') {
                    // 是数字字符
                    if (position - i == 1) { // 上次还是数字
                        if (offset == numberIndex) {
                            power *= 10;
                            number += (c - '0') * power;
                        }
                    } else { // 上次是非数字字符，这次是数字字符
                        --offset; // 找的第几个连续数字字符
                        power = 1; // 初始化幂运算
                        number = c - '0';
                    }
                    position = i;
                } else if (offset == numberIndex && position - i == 1) {
                    // 说明上一次是数字字符，这次是非数字字符
                    if (c == '-' && context.charAt(i + 1) != '0') number *= -1;
                    // 如果匹配
                    return number;
                }
            }
            if (offset == numberIndex) return number;
        }

        // 没找到
        throw new IllegalArgumentException("no number found, max offset=" + offset);

    }

    /**
     * 获得文本中的 10 进制数字
     * 不支持指定字符串初始下标
     */
    public static int getInteger(String context, int numberIndex) {
        return getInteger(context, numberIndex < 0 ? -1 : 0, numberIndex);
    }

    /**
     * 搜索一个字符串在另一个字符串中出现的次数
     * （重复字符串，会按最大数量进行匹配）
     *
     * @param src   搜寻源
     * @param tag   在搜寻源中要寻找的字符串
     * @return 字符串个数
     */
    public static int getStrCount(String src, String tag) {
        int count = 0, offset = 0;
        while ((offset = src.indexOf(tag, offset)) >= 0) {
            ++count;
            ++offset; // 如果加上 tag 的长度，可以消除重复字符干扰
        }
        return count;
    }

    /* ******** other ******** */

    public static String randomBase62(int len) {
        return ThreadLocalRandom.current().
                ints().
                limit(len).
                collect(
                        StringBuilder::new,
                        (sb, i) -> {
                            i = Math.abs(i) % 62;
                            sb.append((char) (i > 9 ? (i < 36 ? (i + 65 - 10) : (i + 97 - 36)) : (i + 48)));
                        },
                        StringBuilder::append
                ).toString();
    }

    /**
     * 获得 uri 包含的参数列表，相同的 key，会被忽略
     * 注意：不支持编码后的字符
     */
    public static Map<String, String> getParameterMap(String url) {
        Map<String, String> map = new HashMap<>();
        StringBuilder context = new StringBuilder(url);
        int splitIndex;
        int borderIndex = context.indexOf("?");
        map.put("", context.substring(0, borderIndex));  // 不带参数的 uri 本身，被空字符标记
        while (borderIndex != -1) {
            splitIndex = context.indexOf("=", borderIndex);
            if (splitIndex != -1) {
                String key = context.substring(borderIndex + 1, splitIndex);
                borderIndex = context.indexOf("&", splitIndex);
                map.put(key, context.substring(splitIndex + 1, borderIndex != -1 ? borderIndex : url.length()));
            }
        }

        if (map.isEmpty()) throw new IllegalArgumentException("not have parameter");
        return map;
    }

    /**
     * 获得 uri 包含的参数列表
     * 注意：不支持编码后的字符
     */
    public static Map<String, List<String>> getParametersMapList(String url) {
        Map<String, List<String>> map = new HashMap<>();
        StringBuilder context = new StringBuilder(url);
        int splitIndex;
        int borderIndex = context.indexOf("?");
        while (borderIndex != -1) {
            splitIndex = context.indexOf("=", borderIndex);
            if (splitIndex != -1) {
                String key = context.substring(borderIndex + 1, splitIndex);
                borderIndex = context.indexOf("&", splitIndex);
                String value = context.substring(splitIndex + 1, borderIndex != -1 ? borderIndex : url.length());
                if (!map.containsKey(key)) {
                    List<String> valueList = new ArrayList<>();
                    valueList.add(value);
                    map.put(key, valueList);
                } else map.get(key).add(value);
            }
        }
        return map.isEmpty() ? null : map;
    }

    /**
     * 获得文件名，会剔除路径以及后缀
     * 等价于 batch 的 %~n1
     */
    public static String getNameWithoutSuffix(String path) {
        if (path == null || path.isEmpty())
            throw new IllegalArgumentException();

        int leftIndex = Math.max(path.lastIndexOf('\\'), path.lastIndexOf('/'));
        int rightIndex = path.lastIndexOf(".");

        return path.substring(leftIndex < 0 ? 0 : leftIndex + 1, rightIndex);
    }

    /**
     * 打印对象 {@link #toString()} 方法的 byte[]
     */
    public static byte[] toBytes(Object source) {
        return String.valueOf(source).getBytes(StandardCharsets.UTF_8);
    }

    /**
     * 将字符中的变量替换为对应环境变量的值
     * 也可以在启动参数中增加配置 ' -D{key}={value}'
     *
     * 为了兼容 linux 环境变量的命名方式，环境变量 {key} 仅允许使用字母数字下划线，并且首字符不可以是数字。
     * 此处不支持单个字符的环境变量名。
     */
    public static String envReplaceFilter(String src) {
        Matcher matcher = Pattern.compile("(?<=\\$\\{)[A-Za-z_][A-Za-z0-9_]+(?=})").matcher(src);
        while (matcher.find()) {
            String key = matcher.group();
            String value = System.getProperty(key, System.getenv(key));
            if (value == null) {
                throw new IllegalArgumentException(
                        String.format("${%s} not found in environment variable.", key)
                );
            }
            src = src.replaceAll(
                    "\\$\\{" + key + "}",
                    value.replaceAll("\\\\", "\\\\\\\\")
            );
        }
        return src;
    }

}
