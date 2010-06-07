package com.magfa.sms.scribe_log4j;

import org.apache.log4j.Logger;
import org.junit.Test;

/**
 * User: amin
 * Date: Jun 1, 2009
 * Time: 3:42:45 PM
 */
public class ScribeAppenderTest {

    @Test
    public void test1() {
        Logger log = Logger.getLogger("scribe");
        log.info("test 1");
    }

    @Test
    public void test2() {
        Logger log = Logger.getLogger("scribe");

        for (int i = 0; i < 100; i++)
            log.info("test 2: " + i);
    }

    @SuppressWarnings({"ThrowableInstanceNeverThrown"})
    @Test
    public void testLogException() {
        Logger log = Logger.getLogger("scribe");

        log.info("This is an exception", new RuntimeException("Should Print this one too!"));
    }
}
