package org.uniroma2.it.ZKUtilTests;

import org.apache.zookeeper.AsyncCallback.StringCallback;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZKUtil;
import org.apache.zookeeper.ZooKeeper;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.mockito.stubbing.Answer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;
import static utils.NodeTree.*;
import static utils.NodeTree.getChildrenNodes;

/**
 * Test to find if, deleting a znode while visiting
 * in a DFS fashion the subtree, an Exception is thrown.
 */
@RunWith(value = Parameterized.class)
public class VisitSubTreeDFSDeletionTest {
    private ZooKeeper zk;
    private Node root;
    private boolean watch;
    private StringCallback cb;
    private Class<? extends Exception> expectedDelException;


    @Parameters
    public static Collection getParams(){
        return Arrays.asList(new Object[][] {
                {newNode("root1"), false, false},
                {newNode("root2"),false, true},
                {newNode("zookeeper"), false, false}
        });
    }

    @Before
    public void setUp() throws Exception {
        zk = mock(ZooKeeper.class);
        cb = mock(StringCallback.class);

        if (this.root.right==null) {
            doAnswer((Answer<Void>) invocationOnMock -> null).
                    when(cb).processResult(anyInt(), anyString(), eq(null), anyString());
        }
        else doAnswer(invocationOnMock -> {
            deleteNode();
            return null;
        }).when(cb).processResult(anyInt(), anyString(), eq(null), anyString());

        when(zk.getData("/" + this.root.data, false, null)).thenReturn(null);
        when(zk.getChildren("/" + this.root.data, false, null)).
                thenReturn(getChildrenNodes(this.root, new ArrayList<>()));
        when(zk.getChildren("/" + this.root.data+"/"+"child1", false, null)).
                thenReturn(getChildrenNodes(this.root.left, new ArrayList<>()));
        when(zk.getChildren("/" + this.root.data+"/"+"child2", false, null)).
                thenReturn(getChildrenNodes(this.root.right, new ArrayList<>()));
    }

    @Rule
    public ExpectedException thrown = ExpectedException.none();


    public VisitSubTreeDFSDeletionTest(Node root, boolean watch, boolean expectedDel){
        this.configure(root, watch, expectedDel);
    }

    private void configure(Node root, boolean watch, boolean expectedDel){
        this.root = root;
        this.watch = watch;
        if (expectedDel) {
            this.expectedDelException = KeeperException.NoNodeException.class;
        }
        if (root.data.equals("root1")){
            this.root.left = newNode("child1");
            this.root.right = newNode("child2");
        }
        else if (root.data.equals("root2")){
            this.root.left = newNode("child1");
        }
    }

    public void deleteNode() throws InterruptedException, KeeperException {
        zk.delete("/"+this.root.right.data, -1);
    }

    @Test
    public void deleteTest(){
        try {
            ZKUtil.visitSubTreeDFS(zk, "/" + this.root.data, this.watch, cb);
        } catch (Exception e){
            System.out.println("deleted node " + this.root.data);
            assertEquals(this.expectedDelException, e.getClass());
        }
    }
}

