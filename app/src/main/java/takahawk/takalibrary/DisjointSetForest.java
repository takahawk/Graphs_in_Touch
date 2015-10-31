package takahawk.takalibrary;

import java.util.HashMap;
import java.util.Map;

/**
 * From wikipedia:
 * In computer science, a disjoint-set data structure, also called a union–find data structure or merge–find set, is a data structure that keeps track of a set of elements partitioned into a number of disjoint (nonoverlapping) subsets. It supports two useful operations:
 * - Find: Determine which subset a particular element is in. Find typically returns an item from this set that serves as its "representative"; by comparing the result of two Find operations, one can determine whether two elements are in the same subset.
 * - Union: Join two subsets into a single subset.
 * @author takahawk
 */
public class DisjointSetForest<T> {

    Map<T, Node> map = new HashMap<>();

    private class Node {
        T label;
        Node parent;
        int rank;

        public Node(T label) {
            this.label = label;
            parent = this;
            rank = 0;
        }
    }
    /**
     * Creates new disjoint set forest structure
     */
    public DisjointSetForest() {
    }

    /**
     * Makes new tree with only one node (singleton)
     * @param x label of node
     */
    public void makeSet(T x) {
        if (!map.containsKey(x))
            map.put(x, new Node(x));
    }

    /**
     * Join two trees (or subsets) into a single tree (subset), if not already joined
     * @param x first tree node
     * @param y second tree node
     */
    public void union(T x, T y) {
        Node xRoot = find(map.get(x));
        Node yRoot = find(map.get(y));
        if (xRoot == yRoot)
            return;

        // x and y not in the same set, merge them
        if (xRoot.rank < yRoot.rank)
            xRoot.parent = yRoot;
        else if (xRoot.rank > yRoot.rank)
            yRoot.parent = xRoot;
        else {
            yRoot.parent = xRoot;
            xRoot.rank++;
        }
    }

    /**
     * Returns root node (used as id of subset) of a given node
     * @param x number of node
     * @return root node
     */
    public T find(T x) {
        return find(map.get(x)).label;
    }

    private Node find(Node x) {
        if (x.parent != x)
            x.parent = find(x.parent);
        return x.parent;
    }
}
