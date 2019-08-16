package com.twitter.service.snowflake;

/**
 * Copyright 2010 Twitter, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this
 * file except in compliance with the License. You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed
 * under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 * https://github.com/twitter-archive/snowflake
 *
 * 雪花算法
 * https://github.com/twitter-archive/snowflake/blob/scala_28/src/main/scala/com/twitter/service/snowflake/IdWorker.scala
 *
 * SnowFlake 的结构如下(每部分用-分开):
 *      0 - 0000000000 0000000000 0000000000 0000000000 0 - 00000 - 00000 - 000000000000
 *
 * 1 位标识，由于long基本类型在Java中是带符号的，最高位是符号位，正数是0，负数是1，所以id一般是正数，最高位是 0
 *
 * 41 位时间截(毫秒级)
 *      注意，41位时间截不是存储当前时间的时间截，而是存储时间截的差值（当前时间截 - 开始时间截)得到的值，
 *      41 位的时间截，可以使用69年，年T = (1L << 41) / (1000L * 60 * 60 * 24 * 365) = 69
 *
 * 10 位的实例位，可以部署在 1024 个节点，包括 5 位 dataCenterId 和 5 位 workerId
 *
 * 12 位序列，毫秒内的计数。
 *      12 位的计数顺序号支持每个节点每毫秒(同一实例，同一时间截)产生 4096 个 ID 序号
 *
 * 加起来刚好64位，为一个Long型。
 *
 * SnowFlake的优点是：
 *      整体上按照时间自增排序，并且整个分布式系统内不会产生ID碰撞(由数据中心ID和实例ID作区分)，
 *      并且效率较高，经测试，SnowFlake每秒能够产生26万ID左右。
 */
public class IdWorker {

    // date -d "2019-01-01 00:00:00" +%s
    private final static long TW_EPOCH = 1546272000000L;     // 开始时间截 (2019-01-01)

    private final static long WORKER_ID_BITS = 5L;                              // 实例 id 所占位数
    private final static long MAX_WORKER_ID = -1L ^ (-1L << WORKER_ID_BITS);    // 最大实例 id

    private final static long DATA_CENTER_ID_BITS = 5L;                                 // 数据中心 id 所占的位数
    private final static long MAX_DATA_CENTER_ID = -1L ^ (-1L << DATA_CENTER_ID_BITS);  // 最大数据中心 id

    private final static long SEQUENCE_BITS = 12L;  // 序列号占的位数
    private final static long SEQUENCE_MASK = -1L ^ (-1L << SEQUENCE_BITS); // 掩码 (0b111111111111=0xfff=4095)

    // 位移
    private final static long WORKER_ID_SHIFT = SEQUENCE_BITS;
    private final static long DATA_CENTER_ID_SHIFT = SEQUENCE_BITS + WORKER_ID_BITS;
    private final static long TIMESTAMP_LEFT_SHIFT = SEQUENCE_BITS + WORKER_ID_BITS + DATA_CENTER_ID_BITS;

    private long lastTimestamp = -1L;   // 上次的时间截

    private long workerId;      // 实例 ID(0~31)
    private long dataCenterId;  // 数据中心 ID(0~31)
    private long sequence = 0L; // 毫秒内序列(0~4095)


    /**
     * @param workerId      实例 ID (0~31)
     * @param dataCenterId  数据中心 ID (0~31)
     */
    public IdWorker(long workerId, long dataCenterId) {
        if (workerId > MAX_WORKER_ID || workerId < 0) {
            throw new IllegalArgumentException(
                    String.format("worker Id can't be greater than %d or less than 0", MAX_WORKER_ID)
            );
        }
        if (dataCenterId > MAX_DATA_CENTER_ID || dataCenterId < 0) {
            throw new IllegalArgumentException(
                    String.format("dataCenter Id can't be greater than %d or less than 0", MAX_DATA_CENTER_ID)
            );
        }
        this.workerId = workerId;
        this.dataCenterId = dataCenterId;
    }

    /**
     * @return 获得下一个ID
     */
    public synchronized long nextId() {
        long timestamp = timeGen();

        if (timestamp < lastTimestamp) {
            // 如果系统时钟回退
            throw new RuntimeException(
                    String.format(
                            "Clock moved backwards. Refusing to generate id for %d milliseconds",
                            lastTimestamp - timestamp
                    )
            );
        }

        // 当前调用和上一次调用落在了相同毫秒内，只能通过第三部分，序列号自增来判断为唯一，所以+1.
        if (lastTimestamp == timestamp) {
            sequence = (sequence + 1L) & SEQUENCE_MASK;

            if (sequence == 0L) { // 同一毫秒的序列数已经达到最大，只能等待下一个毫秒
                // 阻塞到下一个毫秒，获得新的时间戳
                timestamp = tilNextMillis(lastTimestamp);
            }
        } else {
            sequence = 0L;
        }

        // 保存上次生成ID的时间截
        lastTimestamp = timestamp;

        // 移位并通过或运算拼到一起组成64位的ID
        return ((timestamp - TW_EPOCH) << TIMESTAMP_LEFT_SHIFT) // 时间戳部分
                | (dataCenterId << DATA_CENTER_ID_SHIFT)        // 数据中心部分
                | (workerId << WORKER_ID_SHIFT)                 // 实例标识部分
                | sequence;                                     // 序列号部分
    }

    /**
     * 阻塞到下一个毫秒，直到获得新的时间戳
     *
     * @param lastTimestamp     上次生成ID的时间截
     * @return 当前时间戳
     */
    private long tilNextMillis(long lastTimestamp) {
        long timestamp = timeGen();
        while (timestamp <= lastTimestamp) {
            timestamp = timeGen();
        }
        return timestamp;
    }

    private long timeGen() {
        return System.currentTimeMillis();
    }

}
