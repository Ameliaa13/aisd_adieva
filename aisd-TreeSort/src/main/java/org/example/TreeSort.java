package org.example;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;


public class TreeSort {

    private static class Node {
        int value;
        Node left, right;

        Node(int value) {
            this.value = value;
            right = null;
            left = null;
        }
    }


    private static Node insert(Node root, int value, long[] comparisons) {
        if (root == null) {
            return new Node(value);
        }
        //здесь присваивается новый лист как либо левый, либо правый потомок
        comparisons[0]++;
        if (value < root.value) {
            root.left = insert(root.left, value, comparisons);
        } else {
            root.right = insert(root.right, value, comparisons);
        }
        return root;
    }


    private static void inorder(Node root, List<Integer> result) {
        if (root != null) {
            inorder(root.left, result);
            result.add(root.value);
            inorder(root.right, result);
        }
    }


    public static List<Integer> treeSort(List<Integer> list, long[] comparisons) {
        if (list == null || list.isEmpty()) {
            comparisons[0] = 0;
            return new ArrayList<>();
        }

        Node root = null;
        //подсчет количества итераций
        comparisons[0] = 0;
        //построение бинарного дерева
        for (int value : list) {
            root = insert(root, value, comparisons);
        }

        List<Integer> sorted = new ArrayList<>(list.size());
        //выполняем симметричный обход
        inorder(root, sorted);
        //возвращается отсортированная последовательность
        return sorted;
    }

    public static boolean isSorted(List<Integer> data) {
        boolean flag = true;
        for (Integer i = 0; i < data.size()-1; i++) {

            if (data.get(i) > data.get(i+1)) {
                flag = false;
            }
        }
        return flag;
    }

    public static void main(String[] args) throws IOException {

        File data = new File("test_data.txt");
        FileReader fr = new FileReader(data);
        BufferedReader br = new BufferedReader(fr);
        String nabor;
        List<List<Integer>> testSets = new ArrayList<>();
        while ((nabor = br.readLine()) != null) {
            testSets.add(Arrays.stream(nabor.split(" ")).filter(d -> !d.equals("")).map(d -> Integer.parseInt(d)).toList());

        }

        try (BufferedWriter timeWriter = new BufferedWriter(new FileWriter("time_data.txt"));
             BufferedWriter iterWriter = new BufferedWriter(new FileWriter("iterations_data.txt"))) {

            for (int i = 0; i < testSets.size(); i++) {
                List<Integer> dataSet = testSets.get(i);
                long[] comparisons = new long[1];

                long start = System.nanoTime();
                List<Integer> sorted = treeSort(dataSet, comparisons);
                long end = System.nanoTime();

                double timeMs = (end - start) / 1_000_000.0;

                timeWriter.write(String.format(Locale.US, "%.6f\t%d", timeMs, dataSet.size()));
                timeWriter.newLine();
                iterWriter.write(String.format("%d\t%d", comparisons[0], dataSet.size()));
                iterWriter.newLine();

            }

        } catch (IOException e) {
            System.err.println("Ошибка записи в файл: " + e.getMessage());
        }

    }
}