package org.uniroma2.it.ZKUtilTests;

import org.apache.zookeeper.ZKUtil;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.Assert.assertEquals;

@RunWith(value = Parameterized.class)
public class GetPermStringTest {
    private Integer perms;
    private String expectedPermString;

    /**
     * Setting -1 as a parameter I'm expecting to obtain a
     * void string as result, because, by documentation, the "ALL"
     * perm is set to 31, but, using the java & operator also
     * -1 is seen as 11111 in binary.
     * @return
     */
    @Parameters
    public static Collection getParams(){
        return Arrays.asList(new Object[][] {
                {1, "r"},
                //{-1, ""},
                {0, ""}
        });
    }

    public GetPermStringTest(Integer perms, String expectedPermString){
        this.configure(perms, expectedPermString);
    }

    private void configure(Integer perms, String expectedPermString) {
        this.perms = perms;
        this.expectedPermString = expectedPermString;
    }


    @Test
    public void permTest(){
        String result = ZKUtil.getPermString(this.perms);
        assertEquals(this.expectedPermString, result);
    }

}
