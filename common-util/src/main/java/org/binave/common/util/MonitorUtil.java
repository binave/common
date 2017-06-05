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

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 监测用工具
 *
 * @author bin jin
 * @since 1.8
 */
public class MonitorUtil {

    /**
     * 获得代码行号
     */
    public static int lineNum() {
        // return Thread.currentThread().getStackTrace()[2].getLineNumber();
        return new Throwable().getStackTrace()[1].getLineNumber();
    }

    /**
     * 获得当前最大内存
     */
    public static String totalMemory() {
        return printSize(Runtime.getRuntime().totalMemory());
    }

    /**
     * 获得当前使用内存
     */
    public static String useMemory() {
        return printSize(Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory());
    }

    /**
     * @see #printSize(long)
     */
    private static String[] unit = new String[]{"", "K", "M", "G", "T", "P", "E"};

    /**
     * 打印带单位的体积
     */
    private static String printSize(long size) {
        double s = size;
        int n = 0;
        while (s >= 1024) {
            n++;
            s /= 1024;
        }
        return String.format("%.2f ", s).replace(".00", "") + unit[n] + "B";
    }

    /**
     * for Test
     */
    public static String timeMillisFormat(long timeMillis) {

        StringBuilder sb = new StringBuilder();
        sb.append(timeMillis % 1000).append(" millisecond");

        if (timeMillis >= 1000) {
            sb.insert(0, " sec, ").insert(0, timeMillis % (60 * 1000) / 1000);

            if (timeMillis >= 60 * 1000) {
                sb.insert(0, " min, ").insert(0, timeMillis % (60 * 60 * 1000) / 60 / 1000);

                if (timeMillis >= 60 * 60 * 1000) {
                    sb.insert(0, " hour, ").insert(0, timeMillis % (24 * 60 * 60 * 1000) / 60 / 60 / 1000);

                    if (timeMillis >= 24 * 60 * 60 * 1000)
                        sb.insert(0, " days, ").insert(0, timeMillis / 24 / 60 / 60 / 1000);
                }
            }
        }
        return sb.toString();
    }

    public static String nanoTimeFormat(long nanoTime) {

        StringBuilder sb = new StringBuilder();
        sb.append(nanoTime % 1000).append(" nanosecond");

        if (nanoTime >= 1000) {
            sb.insert(0, " microsecond, ").insert(0, nanoTime % (1000 * 1000) / 1000);

            if (nanoTime >= 1000 * 1000) {
                sb.insert(0, " millisecond, ").insert(0, nanoTime % (1000 * 1000 * 1000) / 1000 / 1000);

                if (nanoTime >= 1000 * 1000 * 1000) {
                    sb.insert(0, " sec, ").insert(0, nanoTime / 1000 / 1000 / 1000);
                }
            }
        }
        return sb.toString();
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

    /**
     * 获得当前时间戳
     */
    public static String getFormatTime(String format, long timeMillis) {
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        return sdf.format(new Date(timeMillis));
    }

}
