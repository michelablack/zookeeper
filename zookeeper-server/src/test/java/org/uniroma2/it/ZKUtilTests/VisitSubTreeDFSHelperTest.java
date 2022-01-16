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
import org.mockito.ArgumentCaptor;
import org.mockito.stubbing.Answer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;
import static utils.NodeTree.*;
import static utils.NodeTree.getChildrenNodes;

@RunWith(value = Parameterized.class)
public class VisitSubTreeDFSHelperTest {
    private ZooKeeper zk;
    private Node root;
    private boolean watch;
    private StringCallback cb;
    private int expectedResult;
    private Class<? extends Exception> expectedException;
    final ArgumentCaptor<String> child =  ArgumentCaptor.forClass(String.class);
    final ArgumentCaptor<String> childPath = ArgumentCaptor.forClass(String.class);
    final ArgumentCaptor<Integer> code = ArgumentCaptor.forClass(Integer.class);


    @Parameters
    public static Collection getParams(){
        return Arrays.asList(new Object[][] {
                //minimal test suite
                {newNode("root1"), false, 3},
                {newNode(""), false, 2},
                {newNode("\ud800"), false, -1},
                {newNode("."), false, -1},

                //added to kill mutations
                {newNode("root2"),false, 5},
        });
    }

    @Before
    public void setUp() throws Exception {
        zk = mock(ZooKeeper.class);
        cb = mock(StringCallback.class);

        System.out.println(zk);
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


    public VisitSubTreeDFSHelperTest(Node root, boolean watch, int expectedResult){
        this.configure(root, watch, expectedResult);
    }

    private void configure(Node root, boolean watch, int expectedResult){
        this.root = root;
        this.watch = watch;
        this.expectedResult = expectedResult;
        if (expectedResult == -1) {
            this.expectedException = IllegalArgumentException.class;
            System.out.println(this.expectedException);
        }
        if (root.data.equals("root1")){
            this.root.left = newNode("child1");
            this.root.right = newNode("child2");

        }
        if (root.data.equals("")){
            this.root.left = newNode("child1");
        }
        if (root.data.equals("root2")){
            this.root.left = newNode("child1");
            this.root.right = newNode("child2");
            this.root.left.left = newNode("child3");
            this.root.right.right = newNode("child4");
        }

    }

    @Test
    public void visitTest() {
        doAnswer((Answer<Void>) invocationOnMock -> null).
                when(cb).processResult(anyInt(), anyString(), eq(null), anyString());
        try {
            ZKUtil.visitSubTreeDFS(zk, "/" + this.root.data, this.watch, cb);
            verify(cb, times(this.expectedResult)).
                    processResult(code.capture(), childPath.capture(), eq(null), child.capture());
        } catch (Exception e){
            assertEquals(this.expectedException, e.getClass());
        }
    }


}
