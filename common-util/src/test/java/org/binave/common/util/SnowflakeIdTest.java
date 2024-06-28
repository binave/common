package org.binave.common.util;

import com.twitter.service.snowflake.IdWorker;
import org.junit.Test;

/**
 * @author by bin jin on 2019/08/16 10:07.
 */
public class SnowflakeIdTest {

    @Test
    public void testUUID() {
        IdWorker idWorker = new IdWorker(0, 0);
        for (int i = 0; i < 1000; i++) {
            long id = idWorker.nextId();
            System.out.println(Long.toBinaryString(id));
            System.out.println(id);
        }
    }
}
