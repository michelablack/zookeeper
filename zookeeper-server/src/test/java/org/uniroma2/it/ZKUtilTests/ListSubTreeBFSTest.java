package org.uniroma2.it.ZKUtilTests;

import org.apache.zookeeper.ZKUtil;
import org.apache.zookeeper.ZooKeeper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.util.*;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;
import static utils.NodeTree.*;

@RunWith(value = Parameterized.class)
public class ListSubTreeBFSTest {

    private ZooKeeper zk;
    private Node root;
    private List<String> bfs;
    private boolean exception;

    @Parameters
    public static Collection getParams(){
        return Arrays.asList(new Object[][] {
                {newNode("root1"), false},
                //{newNode(""), false},
                {newNode("\u0001"), true}
        });
    }

    @Before
    public void setUp() throws Exception {
        zk = mock(ZooKeeper.class);
    }

    public ListSubTreeBFSTest(Node root, boolean exception){
        this.configure(root, exception);
    }

    private void configure(Node root, boolean exception) {
        this.root = root;
        this.exception = exception;
        this.bfs  = new ArrayList<>();
        if (root.data.equals("root1")){
            this.root.left = newNode("child1");
            this.root.right = newNode("child2");
            this.bfs.add("/" + this.root.data);
            this.bfs.add("/" + this.root.data+"/"+"child1");
            this.bfs.add("/" + this.root.data+"/"+"child2");

        }
        else if (root.data.equals("")){
            this.root.left = newNode("child1");
            this.bfs.add("/" + this.root.data);
            this.bfs.add("/" + this.root.data+"child1");
        }
        else {
            this.bfs.add("/" + this.root.data);
        }

    }


    @Test
    public void listTest() throws Exception {

        when(zk.getChildren("/" + this.root.data, false)).
                thenReturn(getChildrenNodes(this.root, new ArrayList<>()));
        when(zk.getChildren("/" + this.root.data+"/"+"child1", false)).
                thenReturn(getChildrenNodes(this.root.left, new ArrayList<>()));
        when(zk.getChildren("/" + this.root.data+"/"+"child2", false)).
                    thenReturn(getChildrenNodes(this.root.right, new ArrayList<>()));

        List<String> result = ZKUtil.listSubTreeBFS(zk, "/" + this.root.data);

        assertEquals(this.bfs, result);
    }

}
