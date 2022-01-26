package org.uniroma2.it.ZKUtilTests;

import org.apache.zookeeper.ZKUtil;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.data.ACL;
import org.apache.zookeeper.data.Id;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static org.junit.Assert.assertEquals;


@RunWith(value = Parameterized.class)
public class AclToStringTest {

    private List<ACL> acls = new ArrayList<>();
    private String expectedResult;

    @Parameters
    public static Collection getParams(){

        return Arrays.asList(new Object[][] {
                {"digest"},
                {null}
        });
    }

    @After
    public void cleanEnv(){
        this.acls.clear();
    }
    public AclToStringTest(String scheme){
        this.configure(scheme);
    }

    private void configure(String scheme) {
        if (scheme!=null){
            ACL acl = new ACL(ZooDefs.Perms.ALL, new Id("digest", "id"));
            this.acls.add(acl);
            this.expectedResult = "digest:id:cdrwa";
        }
        else {
            this.expectedResult = "";
        }
    }

    @Test
    public void aclToStringTest(){
        String result = ZKUtil.aclToString(this.acls);
        assertEquals(this.expectedResult, result);
    }
}
