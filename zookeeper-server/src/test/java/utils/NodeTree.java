package utils;

import java.util.ArrayList;
import java.util.List;

/**
 * Custom class to create a subtree,
 * starting from a specific root.
 */
public class NodeTree {


    public static class Node {
        public String data;
        public Node left, right;
    }

    public static List<String> getChildrenNodes(Node root, List<String> leaves) {

        if (root == null) {
            System.out.println(leaves);
            return leaves;
        }
        if (root.left != null)
            leaves.add(root.left.data);

        if (root.right != null)
            leaves.add(root.right.data);


        return leaves;
    }

    public static List<String> getChildrenNodesNotOrdered(Node root, List<String> leaves) {

        if (root == null) {
            return leaves;
        }
        if (root.right != null)
            leaves.add(root.right.data);

        if (root.left != null)
            leaves.add(root.left.data);

        return leaves;
    }

    public static boolean hasChildrenLeft(Node root) {
        if (root == null) {
            return false;
        }
        if (root.left != null || root.right != null)
            return true;

        return false;
    }
    public static Node newNode(String data) {
        Node temp = new Node();
        temp.data = data;
        temp.left = null;
        temp.right = null;
        return temp;
    }
}


