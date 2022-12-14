/* __________              _____                                                *\
** \______   \____   _____/ ____\____   ____    Copyright (c) 2017-2023 Ponfee  **
**  |     ___/  _ \ /    \   __\/ __ \_/ __ \   http://www.ponfee.cn            **
**  |    |  (  <_> )   |  \  | \  ___/\  ___/   Apache License Version 2.0      **
**  |____|   \____/|___|  /__|  \___  >\___  >  http://www.apache.org/licenses/ **
**                      \/          \/     \/                                   **
\*                                                                              */

package cn.ponfee.scheduler.supervisor.test.common.util;

import cn.ponfee.scheduler.common.util.Jsons;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.IntStream;

/**
 * @author Ponfee
 */
public class TreeMapTest {

    @Test
    @Ignore
    public void testTreeMap() {
        Map<String, String> executing = new TreeMap<>(); // TreeMap HashMap
        IntStream.range(0, 256)
            .mapToObj(i -> String.format("%03d", i))
            .forEach(e -> executing.put(e, "v-" + e));
        Map<String, String> finished = new TreeMap<>();

        System.out.println("-----------------before");
        int beforeSize = executing.size();
        String beforeFull = Jsons.toJson(executing);
        String beforeEmpty = Jsons.toJson(finished);
        System.out.println(beforeFull);
        System.out.println(beforeEmpty);
        for (int i = 0; !executing.isEmpty(); i++) {
            for (Iterator<Map.Entry<String, String>> iter = executing.entrySet().iterator(); iter.hasNext(); ) {
                Map.Entry<String, String> shard = iter.next();
                String value = shard.getValue();
                if (ThreadLocalRandom.current().nextInt(9) < 3) {
                    String s1 = shard.getKey();
                    iter.remove();
                    finished.put(shard.getKey(), value);
                    //finished.put(s1, value);
                    String s2 = shard.getKey();
                    System.out.println("remove_before_and_after: " + s1 + " -> " + s2);
                }
            }
        }

        System.out.println("\n\n-----------------after");
        String afterFull = Jsons.toJson(finished);
        String afterEmpty = Jsons.toJson(executing);
        System.out.println(afterFull);
        System.out.println(afterEmpty);

        int afterSize = finished.size();
        Assert.assertEquals(beforeEmpty, afterEmpty);
        Assert.assertEquals(beforeSize, afterSize);
        Assert.assertEquals(beforeFull, afterFull);
    }

    @Test
    public void testConcurrentHashSet() {
        Set<String> set = ConcurrentHashMap.newKeySet();
        Assert.assertTrue(set.isEmpty());

        set.add("a");
        set.add("b");
        Assert.assertEquals(set.size(), 2);

        set.add("b");
        set.add("c");
        Assert.assertEquals(set.size(), 3);
    }
}
