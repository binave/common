package org.binave.common;

import org.binave.common.collection.IndexMap;
import org.junit.Test;

import java.util.*;

/**
 * @author by bin jin on 2017/3/29.
 */
public class ArrayTest {

    private int[][] ks = {
            {18, 20, 21, 22, 23, 24, 25, 26},
            {-5, -10, -15, -20, -25, -30, -35, -40},
            {30, 40, 50, 60, 70, 80, 90, 100},
            {100, 200, 300, 400, 500, 600, 700, 800},
            {9, 8, 7, 6, 5, 4, 3, 2},
            {-50, -100, -150, -200, -250, -300, -350, -400}
    };

    private Random random = new Random();

    private int code = 0;

    private boolean equalsPrintln(Object obj1, Object obj2, String tag) {
        String str1 = obj1.toString();
        String str2 = obj2.toString();
        boolean b = Objects.equals(str1, str2);

        if (!b) code = 1;

        String prefix = b ? "    " : "### ";

        tag = tag == null ? " " : "[" + tag + "] ";

        System.out.println(prefix + "* " + tag + str1 + "\n" + prefix + "- " + tag + str2);
        return b;
    }

    private boolean equalsPrintln(Object obj1, Object obj2) {
        return equalsPrintln(obj1, obj2, null);
    }

    @Test
    public void TestMap() {

        for (int i = 0; i < 30; i++) {
            SortedMap<Integer, String> tree = new TreeMap<>();

            int s = random.nextInt(100);
            for (int j = s; j < s + random.nextInt(100) + 1; j++) {
                tree.put(j, "v" + j);
            }

            SortedMap<Integer, String> index = new IndexMap<>();
            index.putAll(tree);

            equalsPrintln(tree, index);
        }

        SortedMap<Integer, String> tree = new TreeMap<>();
        SortedMap<Integer, String> index = new IndexMap<>();

        for (int[] k : ks) {
            for (int i : k) {
                tree.put(i, "v" + i);
                index.put(i, "v" + i);
            }
        }

        Iterator treeIt = tree.keySet().iterator();
        while (treeIt.hasNext()) {
            treeIt.next();
            treeIt.remove();
        }

        Iterator indexIt = index.keySet().iterator();
        while (indexIt.hasNext()) {
            indexIt.next();
            indexIt.remove();
        }

        for (int i = 0; i < 10; i++) {
            for (int[] k : ks)
                for (int j : k) {
                    tree.put(j, "v" + j);
                    index.put(j, "v" + j);
                }

//            equalsPrintln(tree, index);
            System.out.println("    === === ===");

            int head = random.nextInt(500) - 200;
            int tail = head + random.nextInt(500) + 10;

            tree.subMap(head, tail).clear();
            index.subMap(head, tail).clear();
            equalsPrintln(tree, index, "sub " + head + "-" + tail);

            head = random.nextInt(500) - 200;
            tail = head + random.nextInt(500) + 10;

            equalsPrintln(tree.headMap(head), index.headMap(head), "head " + head);
            equalsPrintln(tree.tailMap(head), index.tailMap(head), "tail " + head);
            equalsPrintln(tree.headMap(tail), index.headMap(tail), "head " + tail);
            equalsPrintln(tree.tailMap(tail), index.tailMap(tail), "tail " + tail);
        }


//        System.exit(code);

        ///// putAll /////

        equalsPrintln(tree.subMap(20, 100), index.subMap(20, 100), "source 20-100");
        SortedMap<Integer, String> tree2 = new TreeMap<>();
        SortedMap<Integer, String> index2 = new IndexMap<>();
        tree2.putAll(tree.subMap(20, 100));
        index2.putAll(index.subMap(20, 100));
        equalsPrintln(tree2, index2, "putAll 20-100");

        System.out.println("    contains 20-100 ");

        for (int[] is : ks) {
            for (int i : is) {
                if (random.nextBoolean()) continue;
                Integer k = i;
                String v = "v" + (i - 1);
                equalsPrintln(tree2.containsKey(k), index2.containsKey(k), "containsKey");
                equalsPrintln(tree2.containsValue(v), index2.containsValue(v), "containsValue");
                equalsPrintln(tree2.keySet().contains(k), index2.keySet().contains(k), "keySet contains");
                equalsPrintln(tree2.values().contains(v), index2.values().contains(v), "values contains");
            }
        }

        tree = new TreeMap<>();
        index = new IndexMap<>();

        for (int i = 0; i < 100; i++) {

            try {


                if (random.nextInt(2) == 0) {
                    Integer k = random.nextInt(1000) - 200;
                    tree.put(k, "v" + (k - 1));
                    index.put(k, "v" + (k - 1));
                    equalsPrintln(
                            tree.firstKey() + "-" + tree.lastKey() + ", " + tree.size() + " " + tree,
                            index.firstKey() + "-" + index.lastKey() + ", " + index.size() + " " + index,
                            "put: " + k + ": v" + (k - 1));

                } else {
                    Integer k = random.nextInt(tree.lastKey() - tree.firstKey() + 1) + tree.firstKey() - 1;
                    tree.remove(k);
                    index.remove(k);
                    equalsPrintln(
                            tree.firstKey() + "-" + tree.lastKey() + ", " + tree.size() + " " + tree,
                            index.firstKey() + "-" + index.lastKey() + ", " + index.size() + " " + index,
                            "del: " + k);

                }
            } catch (NoSuchElementException ignored) {
            }
        }

        /////// keySet ///////

        StringBuilder treeContext = new StringBuilder();
        StringBuilder indexContext = new StringBuilder();
        for (Integer i : tree.keySet()) treeContext.append(" ").append(i);
        for (Integer i : index.keySet()) indexContext.append(" ").append(i);

        equalsPrintln(treeContext, indexContext, "keySet");

        /////// values ///////
        treeContext = new StringBuilder();
        indexContext = new StringBuilder();
        for (String i : tree.values()) treeContext.append(" ").append(i);
        for (String i : index.values()) indexContext.append(" ").append(i);
        equalsPrintln(treeContext, indexContext, "values");

        ///// entrySet /////
        treeContext = new StringBuilder();
        indexContext = new StringBuilder();
        for (Map.Entry<Integer, String> i : tree.entrySet()) {
            i.setValue("v" + (i.getKey() - 1));
            treeContext.append(" ").append(i.getKey()).append(":").append(i.getValue());
        }
        treeContext.append(" ").append(tree.entrySet().size());
        for (Map.Entry<Integer, String> i : index.entrySet()) {
            i.setValue("v" + (i.getKey() - 1));
            indexContext.append(" ").append(i.getKey()).append(":").append(i.getValue());
        }
        indexContext.append(" ").append(index.entrySet().size());
        equalsPrintln(treeContext, indexContext, "entrySet");

        //// ////


        equalsPrintln(
                tree.size() + " " + tree.firstKey() + "-" + tree.lastKey(),
                index.size() + " " + index.firstKey() + "-" + index.lastKey(),
                "1 size, 1 head-tail"
        );

        SortedMap<Integer, String> tree3 = tree.subMap(tree.firstKey(), tree.lastKey());
        SortedMap<Integer, String> index3 = index.subMap(index.firstKey(), index.lastKey());

        equalsPrintln(
                tree3.size() + " " + tree.firstKey() + "-" + tree.lastKey() + " " + tree3,
                index3.size() + " " + index.firstKey() + "-" + index.lastKey() + " " + index3,
                "3 size, 1 head-tail"
        );

        equalsPrintln(
                tree3.firstKey() + "-" + tree3.lastKey(),
                index3.firstKey() + "-" + index3.lastKey(),
                "3 head-tail"
        );

        ///// keySet.iterator /////
        treeContext = new StringBuilder();
        indexContext = new StringBuilder();
        Iterator<Integer> hashKeySet = tree.keySet().iterator();
        while (hashKeySet.hasNext()) {
            treeContext.append(hashKeySet.next()).append(", ");
//            hashKeySet.remove();
        }
        Iterator<Integer> indexKeySet = index.keySet().iterator();
        while (indexKeySet.hasNext()) {
            indexContext.append(indexKeySet.next()).append(", ");
//            indexKeySet.remove();
        }
        equalsPrintln(treeContext, indexContext, "keySet.it");


        ///// values.iterator /////
        treeContext = new StringBuilder();
        indexContext = new StringBuilder();

        Iterator<String> hashValues = tree.values().iterator();
        while (hashValues.hasNext()) {
            treeContext.append(hashValues.next()).append(", ");
//            hashValues.remove();
        }
        Iterator<String> indexValues = index.values().iterator();
        while (indexValues.hasNext()) {
            indexContext.append(indexValues.next()).append(", ");
//            indexValues.remove();
        }
        equalsPrintln(treeContext, indexContext, "values.it");


        ///// sub /////

        System.out.println("tree3 : " + tree3.size() + ", " + tree3 + "\nindex3: " + index3.size() + ", " + index3);

        SortedMap<Integer, String> tree4 = tree3.subMap(tree3.firstKey(), tree3.lastKey());
        SortedMap<Integer, String> index4 = index3.subMap(index3.firstKey(), index3.lastKey());

        System.out.println("tree4 : " + tree4.size() + ", " + tree4 + "\nindex4: " + index4.size() + ", " + index4);

        tree3.subMap(60, 90).clear();
        index3.subMap(60, 90).clear();
        equalsPrintln(tree3.size() + ", " + tree3, index3.size() + ", " + index3, "3 clear 30-90 ");

        SortedMap<Integer, String> tree5 = tree4.subMap(20, 30);
        SortedMap<Integer, String> index5 = index4.subMap(20, 30);
        equalsPrintln(tree5.size() + ", " + tree5, index5.size() + ", " + index5, "5 copy 20-30 ");

        equalsPrintln(tree4.subMap(30, 90).size(), index4.subMap(30, 90).size(), "4 key 30-90 ");

        treeContext = new StringBuilder();
        indexContext = new StringBuilder();
        for (Integer i : tree4.keySet())
            treeContext.append("[").append(i).append(",").append(tree4.subMap(30, 90).containsKey(i)).append("] ");
        for (Integer i : index4.keySet())
            indexContext.append("[").append(i).append(",").append(index4.subMap(30, 90).containsKey(i)).append("] ");
        equalsPrintln(treeContext, indexContext, "30-90");


        equalsPrintln(tree4.subMap(1, 20).size(), index4.subMap(1, 20).size(), "4 entry 1-20 ");

        treeContext = new StringBuilder();
        indexContext = new StringBuilder();
        for (Map.Entry<Integer, String> i : tree4.entrySet()) {
            treeContext.append("[").append(i.getKey()).append(":").append(i.getValue()).append(",").append(tree4.subMap(1, 20).entrySet().contains(i)).append("] ");
        }
        for (Map.Entry<Integer, String> i : index4.entrySet()) {
            indexContext.append("[").append(i.getKey()).append(":").append(i.getValue()).append(",").append(index4.subMap(1, 20).entrySet().contains(i)).append("] ");
        }
        equalsPrintln(treeContext, indexContext, "1-20");

        tree5 = tree4.subMap(tree4.firstKey(), tree4.lastKey());
        tree4.subMap(30, 90).clear();
        System.out.println(tree5.size() + ", " + tree5);

        tree5 = tree4.subMap(tree4.firstKey(), tree4.lastKey());
        tree4.subMap(30, 90).clear();
        System.out.println(tree5.size() + ", " + tree5);

        index5 = index4.subMap(index4.firstKey(), index4.lastKey());
        index5.subMap(30, 90).clear();
        System.out.println(index5.size() + ", " + index5);

        index5 = index4.subMap(index4.firstKey(), index4.lastKey());
        index5.subMap(30, 90).clear();
        System.out.println(index5.size() + ", " + index5);

//        tree5 = tree4.subMap(tree4.firstKey(), tree4.lastKey());
//        System.out.println(tree5.size() + ", " + tree5);
//        tree5.subMap(3000, 5000).clear();
//        System.out.println(tree5.size() + ", " + tree5);
//
//        index5 = index4.subMap(index4.firstKey(), index4.lastKey());
//        System.out.println(index5.size() + ", " + index5);
//        index5.subMap(3000, 5000).clear();
//        System.out.println(index5.size() + ", " + index5);

        System.exit(code);

    }


    @Test
    public void test2() {

        Map<Integer, String> map = new IndexMap<>();
        map.put(1, "1");
        map.put(2, "2");

        Iterator<Integer> it = map.keySet().iterator();
        it.remove();

        List<Integer> list = new ArrayList<>();
        list.add(1);
        list.add(2);

        Iterator<Integer> iterator = list.iterator();
        iterator.remove();
    }


}
