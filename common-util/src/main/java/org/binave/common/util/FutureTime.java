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

import java.util.Calendar;
import java.util.TimeZone;

/**
 * 未来时间
 *
 * 获得“给定时间”为基础的某个“时间点”或“时间差”的毫秒值
 * 可用于 redis
 *
 * 注意：
 * <code>index</code> 必须是 大于等于 0 的数字
 * <code>size</code> 必须是 大于 0 的数字
 *
 * <code>isWatch</code>
 * 为 true 时：
 *      可以获得如下一个 2 点钟的时间，或者本月末尾等
 * 为 false 时：
 *      仅仅是给定时间加上传入的参数，对于月份，年份会使用 Calendar 进行处理，
 *      其结果由 Calendar 的特性决定。
 *
 * <code>index</code>
 *      统一从 0 开始，超过其所用范围则默认使用范围上限，而不会计入下一个周期中。
 *      如果 <code>index</code> 取值 60，则是所有周期的末尾。如 59 秒，59 分，23 点，周末，月末，年末
 *      如果 <code>size</code> 为 0 ，只是给定时间取整，只有大于等于 1 时，才会进行未来时间的计算
 *
 *  e.g.
 * HOUR_OF_DAY.getMillisecond(13, 1, true, false)
 *      // 获得下 1 个 13 点钟的毫秒值，
 *      // 如果现在是 12 点整，则指的是 60 分钟后的时间点，如果现在是 14 点整，则指的是 23 小时后的时间点。
 *
 * HOUR_OF_DAY.getMillisecond(2, 1, false, false)
 *      // 获得距离当前时间点 1 天（24 小时）后的毫秒值，其中 2 会被忽略
 *      // 只是简单的加上一个时钟周期
 *
 * getMillisecond(60, 1, true, false)
 *      // 获得对应时间单位的末尾时间点。
 *      // HOUR_OF_DAY 是今天末尾，MONTH_OF_YEAR 周末，等等
 *
 * 2017/4/18.
 *
 * @author bin jin
 * @since 1.8
 */
public enum FutureTime {

    MILLISECOND_OF_SECOND {
        /**
         * @param sm            毫秒数（绝对时间）（<code>isWatch = false</code> 时无效）
         * @param seconds       个数
         * @param isWatch       钟表时间（整点）
         * @param isTimeLag     差值
         */
        public long getMillisecond(int sm, int seconds, boolean isWatch, boolean isTimeLag, long milliSeconds) {

            testArgs(sm, seconds);
            long value;

            if (isWatch) {
                value = milliSeconds / 1000 * 1000 // 以秒取整
                        + (sm < 1000 ? sm : 999);
                value += (value > milliSeconds ? seconds - 1 : seconds) * 1000L;

            } else value = milliSeconds + seconds * 1000L;

            return isTimeLag
                    ? value - milliSeconds
                    : value;

        }

    },

    SECOND_OF_MINUTE {
        /**
         * isWatch ?
         *      获得未来第 min 个，秒数为 sec 秒的毫秒值 :
         *      获得给定时间 min 分钟之后的毫秒值
         *
         * @see #MINUTE_OF_HOUR
         *
         * @param sec       第几秒钟（绝对时间）（<code>isWatch = false</code> 时无效）
         * @param min       未来第几分钟（相对时间）
         * @return isTimeLag ? 时间差 : 时间点
         */
        @Override
        public long getMillisecond(int sec, int min, boolean isWatch, boolean isTimeLag, long milliSeconds) {

            testArgs(sec, min);
            long value;

            if (isWatch) {
                value = milliSeconds / 1000 / 60 * 1000 * 60 // 以秒向下取整
                        + (sec < 60 ? sec : 59) * 1000;
                value += (value > milliSeconds ? min - 1 : min) * 60L * 1000;

            } else value = milliSeconds + min * 60L * 1000;

            return isTimeLag
                    ? value - milliSeconds
                    : value;
        }
    },

    MINUTE_OF_HOUR {
        /**
         * isWatch ?
         *      获得未来第 hour 个，分钟数为 min 分钟的毫秒值 :
         *      获得给定时间 hour 小时后的毫秒值
         *
         * e.g. 假设现在是 13:04，getMillisecond(2, 1, true, false) 返回今天 14:02 的毫秒值
         *      如果现在是是 13:01，则返回 13:02 的毫秒值。
         * e.g. getMillisecond(0, 3, false, false) 获得 3 小时候后的时刻的毫秒值
         *
         * @param min       第几分钟。小于零时，为当前所在分钟（绝对时间）（<code>isWatch = false</code> 时无效）
         * @param hour      未来第几个小时。小于等于零时，为当前所在小时（相对时间）
         * @return isTimeLag ? 时间差 : 时间点
         */
        @Override
        public long getMillisecond(int min, int hour, boolean isWatch, boolean isTimeLag, long milliSeconds) {

            testArgs(min, hour);
            long value;

            if (isWatch) {
                value = milliSeconds / 1000 / 60 / 60 * 1000 * 60 * 60 // 以分钟向下取整
                        + (min < 60 ? min : 59) * 60 * 1000;
                value += (value > milliSeconds ? hour - 1 : hour) * 60L * 60 * 1000;

            } else value = milliSeconds + hour * 60L * 60 * 1000;

            return isTimeLag
                    ? value - milliSeconds
                    : value;
        }

    },

    HOUR_OF_DAY {
        /**
         * isWatch ?
         *      获得未来第 day 个，小时数为 hour 小时的毫秒值 :
         *      获得给定时间 day 天后的毫秒值
         *
         * @see #MINUTE_OF_HOUR
         *
         * @param hour      钟点数（整点）（<code>isWatch = false</code> 时无效）
         * @param day       天数（相对时间）
         * @return isTimeLag ? 时间差 : 时间点
         */
        @Override
        public long getMillisecond(int hour, int day, boolean isWatch, boolean isTimeLag, long milliSeconds) {

            testArgs(hour, day);
            long value;

            if (isWatch) {
                value = milliSeconds / 1000 / 60 / 60 / 24 * 1000 * 60 * 60 * 24 // 以小时向下取整
                        - TIME_ZONE_OFFSET // 修正时区
                        + (hour < 24 ? hour : 23) * 60 * 60 * 1000;
                value += (value > milliSeconds ? day - 1 : day) * 24L * 60 * 60 * 1000;

            } else value = milliSeconds + day * 24L * 60 * 60 * 1000;

            return isTimeLag
                    ? value - milliSeconds
                    : value;
        }
    },

    /**
     * First Day Of Week = MONDAY
     * 一个周的第一天 = 星期一
     */
    DAY_OF_WEEK {
        /**
         * isWatch ? 获得未来第 size 个，周 weak 对应时间的毫秒值: 获得 size 周后的毫秒值
         * @see #MINUTE_OF_HOUR
         *
         * 注意：weak : 0 ~ 6 => MONDAY ~ SUNDAY
         *      为了统一规范，周一到周日对应： 0 ~ 6
         *
         * @param week      周几（绝对时间）（<code>isWatch = false</code> 时无效）
         * @param size      几周（相对时间）
         */
        @Override
        public long getMillisecond(int week, int size, boolean isWatch, boolean isTimeLag, long milliSeconds) {

            testArgs(week, size);
            long value;

            if (isWatch) {

                int sourceWeek = getCalendar(milliSeconds).get(Calendar.DAY_OF_WEEK);
                value = milliSeconds / 1000 / 60 / 60 / 24 * 1000 * 60 * 60 * 24
                        - TIME_ZONE_OFFSET + // 时区修正
                        ((week < 7 ? week : 6)
                                /* 将 MONDAY 当作 0，SUNDAY 当作 6 进行处理 */
                                - (sourceWeek > Calendar.SUNDAY ? sourceWeek - 2 : 7 - 1)
                        ) * 24 * 60 * 60 * 1000;
                value += (value > milliSeconds ? size - 1 : size) * 7L * 24 * 60 * 60 * 1000;

                /* 相对现在的 7 天后，星期 week 被当作天来处理 */
            } else value = milliSeconds + size * 7L * 24 * 60 * 60 * 1000;

            return isTimeLag
                    ? value - milliSeconds
                    : value;
        }

    },

    DAY_OF_MONTH {
        /**
         * isWatch ?
         *      获得未来第 mon 个，日数为 day 对应时间的毫秒值 :
         *      获得 mon 月后的毫秒值
         *
         * @see #MINUTE_OF_HOUR
         *
         * 注意：
         *      为了统一规范，一号到三十一号对应：0 ~ 30
         *      对于日期大于目标月天数的，将视为当月最后一天。
         *
         * @param day       月内几号（绝对时间）（<code>isWatch = false</code> 时无效）
         * @param mon       几个月
         */
        @Override
        public long getMillisecond(int day, int mon, boolean isWatch, boolean isTimeLag, long milliSeconds) {

            testArgs(day, mon);
            long value;

            if (isWatch) {
                /* 时间取整 */
                Calendar calendar = getCalendar(
                        milliSeconds / 1000 / 60 / 60 / 24 * 1000 * 60 * 60 * 24
                                - TIME_ZONE_OFFSET // 修正时区
                );
                /* 获得今天在本月的日号 */
                int dayOfMon = calendar.get(Calendar.DAY_OF_MONTH);
                /* 获得当月的天数 */
                int maxDays = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
                /* 如果月数超过月末，则认为是月末，并修正位移 */
                calendar.add(Calendar.MONTH,
                        dayOfMon < (
                                day < maxDays
                                        ? day + 1
                                        : maxDays
                        )
                                ? mon - 1
                                : mon
                );

                // 获得目标月的长度
                maxDays = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
                // 对月末进行处理
                calendar.set(Calendar.DAY_OF_MONTH,
                        day < maxDays
                                ? day + 1
                                : maxDays
                );
                value = calendar.getTimeInMillis();

            } else {
                Calendar calendar = getCalendar(milliSeconds);
                /* 增加月数 */
                calendar.add(Calendar.MONTH, mon);
                value = calendar.getTimeInMillis();
            }

            return isTimeLag
                    ? value - milliSeconds
                    : value;
        }
    },

    MONTH_OF_YEAR {
        /**
         * isWatch ?
         *      获得未来第 year 个，mon 对应月份的 1 号的毫秒值 :
         *      获得 year 年后的毫秒值
         *
         * @see #MINUTE_OF_HOUR
         *
         * 一月到十二月对应：0 ~ 11
         *
         * @param mon       第几月（绝对时间）（<code>isWatch = false</code> 时无效）
         * @param year      年数
         */
        @Override
        public long getMillisecond(int mon, int year, boolean isWatch, boolean isTimeLag, long milliSeconds) {

            testArgs(mon, year);
            long value;

            if (isWatch) {
                /* 时间取整 */
                Calendar calendar = getCalendar(
                        milliSeconds / 1000 / 60 / 60 / 24 * 1000 * 60 * 60 * 24
                                - TIME_ZONE_OFFSET // 修正时区
                );
                /* 将时间重置为本月一号 */
                calendar.set(Calendar.DAY_OF_MONTH, 1);
                /* 如果参数大于月上限，则视为末月 */
                calendar.set(Calendar.MONTH,
                        mon < Calendar.DECEMBER
                                ? mon
                                : Calendar.DECEMBER
                );
                /* 如果上部得到的日期，指向过去，则向前修正 */
                calendar.add(Calendar.YEAR,
                        calendar.getTimeInMillis() > milliSeconds
                                ? year - 1
                                : year
                );
                value = calendar.getTimeInMillis();

            } else {

                Calendar calendar = getCalendar(milliSeconds);
                /* 增加年数 */
                calendar.add(Calendar.YEAR, year);
                value = calendar.getTimeInMillis();
            }

            return isTimeLag
                    ? value - milliSeconds
                    : value;

        }
    };

    /**
     * 时区毫秒值
     *
     * 默认服务器运行时固定经度，否则将会出现问题
     */
    private static int TIME_ZONE_OFFSET = TimeZone.getDefault().getRawOffset();

    /**
     * 获得以“给定时间”为基础的某个“时间点”或“时间差”的毫秒值
     *
     * @param timePoint     时间点
     * @param size          周期数
     * @param isWatch       钟表时间
     * @param isTimeLag     差值
     * @param milliSeconds  距离 1970 年的毫秒值
     * @see java.util.Calendar
     */
    abstract public long getMillisecond(int timePoint, int size, boolean isWatch, boolean isTimeLag, long milliSeconds);

    /**
     * 获得以“当前时间”为基础的某个“时间点”或“时间差”的毫秒值
     *
     * @param timePoint     时间点
     * @param size          周期数
     * @param isWatch       钟表时间
     * @param isTimeLag     差值
     */
    public long getMillisecond(int timePoint, int size, boolean isWatch, boolean isTimeLag) {
        return getMillisecond(timePoint, size, isWatch, isTimeLag, System.currentTimeMillis());
    }

    /**
     * @see #getMillisecond 舍去毫秒部分
     */
    public int getSeconds(int timePoint, int size, boolean isWatch, boolean isTimeLag, long milliSeconds) {
        return (int) (getMillisecond(timePoint, size, isWatch, isTimeLag, milliSeconds) / 1000);
    }


    /**
     * @see #getSeconds 当前时间为基础
     */
    public int getSeconds(int timePoint, int size, boolean isWatch, boolean isTimeLag) {
        return getSeconds(timePoint, size, isWatch, isTimeLag, System.currentTimeMillis());
    }

    /**
     * 测试传入参数
     *
     * @param little        较小值
     * @param larger        较大值
     */
    private static void testArgs(int little, int larger) {
        if (little < 0 || larger <= 0)
            throw new IllegalArgumentException("testArgs item: " + little + ", " + larger);
    }

    /**
     * 对每个线程缓存对应的 Calendar
     */
    private static ThreadLocal<Calendar> calendarCache = new ThreadLocal<>();

    /**
     * 请使用线程池。
     * todo 注意：如果使用协程，则不可以使用此方式。请使用协程支持的方式
     */
    private static Calendar getCalendar(long milliSeconds) {

        Calendar calendar = calendarCache.get();
        if (calendar == null) {
            calendar = Calendar.getInstance();
            calendarCache.set(calendar);
        }
        // 设置时间
        calendar.setTimeInMillis(milliSeconds);
        return calendar;
    }

    private static FutureTime[] futureTimes = FutureTime.values();

    /**
     * get value by index
     */
    public static FutureTime get(int index) {
        try {
            return futureTimes[index];
        } catch (RuntimeException e) {
            throw new IllegalArgumentException("FutureTime.get item: " + index);
        }
    }

}
