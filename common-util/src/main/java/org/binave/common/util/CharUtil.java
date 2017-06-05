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
import java.util.*;
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

    private static final String DEFAULT_SEPARATOR = "{}";

    /**
     * 将字符串中的 {} 替换成参数
     *
     * @see java.text.MessageFormat#subformat
     *
     * @param format    包含占位符 {} 的字符串
     * @param args      {@link Object#toString()}
     */
    public static String format(String format, Object... args) {
        return replacePlaceholders(false, true, DEFAULT_SEPARATOR, format, args);
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
        Matcher matcher = Pattern.compile("(?<=<" + tag + "([^<]+)?>).*?(?=</" + tag + ")").
                matcher(xml);
        List<String> bodyList = new ArrayList<>();
        while (matcher.find()) bodyList.add(matcher.group());
        return bodyList.size() == 0 ? new ArrayList<>(0) : bodyList;
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

    /* ******** other ******** */

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
        return String.valueOf(source).getBytes(Charset.forName("UTF-8"));
    }

}
