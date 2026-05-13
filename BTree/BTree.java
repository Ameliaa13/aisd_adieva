package org.example;

import java.util.Random;

public class BTree {

    static class BTreeNode {
        int[] keys;
        BTreeNode[] children;
        int n;
        boolean isLeaf;

        BTreeNode(int degree, boolean isLeaf) {
            this.isLeaf = isLeaf;
            this.keys = new int[2 * degree - 1];
            this.children = new BTreeNode[2 * degree];
            this.n = 0;
        }
    }

    private BTreeNode root;
    private final int degree;

    // Счётчики операций
    public long addOperations = 0;
    public long searchOperations = 0;
    public long removeOperations = 0;

    public BTree(int degree) {
        this.degree = degree;
        root = new BTreeNode(degree, true);
    }

    //операция поиска
    public boolean contains(int value) {
        return contains(root, value);
    }

    private boolean contains(BTreeNode node, int value) {
        int i = 0;
        while (i < node.n && value > node.keys[i]) {
            searchOperations++;
            i++;
        }

        if (i < node.n && value == node.keys[i]) {
            searchOperations++;
            return true;
        }

        if (node.isLeaf) {
            return false;
        }

        return contains(node.children[i], value);
    }

    public void add(int value) {
        BTreeNode r = root;
        if (r.n == 2 * degree - 1) {
            addOperations++;
            BTreeNode s = new BTreeNode(degree, false);
            root = s;
            s.children[0] = r;
            splitChild(s, 0, r);
            insertNonFull(s, value);
        } else {
            insertNonFull(r, value);
        }
    }

    private void insertNonFull(BTreeNode node, int value) {
        int i = node.n - 1;

        if (node.isLeaf) {
            while (i >= 0 && value < node.keys[i]) {
                addOperations++;
                node.keys[i + 1] = node.keys[i];
                i--;
            }
            node.keys[i + 1] = value;
            node.n++;
        } else {
            while (i >= 0 && value < node.keys[i]) {
                addOperations++;
                i--;
            }
            i++;

            if (node.children[i].n == 2 * degree - 1) {
                splitChild(node, i, node.children[i]);
                if (value > node.keys[i]) {
                    addOperations++;
                    i++;
                }
            }
            insertNonFull(node.children[i], value);
        }
    }
//вызывается, когда у ребенка 2t-1 ключей
    private void splitChild(BTreeNode parent, int i, BTreeNode fullChild) {
        BTreeNode newNode = new BTreeNode(degree, fullChild.isLeaf);
        newNode.n = degree - 1;

        for (int j = 0; j < degree - 1; j++) {
            addOperations++;
            newNode.keys[j] = fullChild.keys[j + degree];
        }

        if (!fullChild.isLeaf) {
            for (int j = 0; j < degree; j++) {
                addOperations++;
                newNode.children[j] = fullChild.children[j + degree];
            }
        }

        fullChild.n = degree - 1;

        for (int j = parent.n; j >= i + 1; j--) {
            parent.children[j + 1] = parent.children[j];
        }
        parent.children[i + 1] = newNode;

        for (int j = parent.n - 1; j >= i; j--) {
            parent.keys[j + 1] = parent.keys[j];
        }
        parent.keys[i] = fullChild.keys[degree - 1];
        parent.n++;
    }

    public void remove(int value) {
        remove(root, value);
        if (root.n == 0 && !root.isLeaf) {
            root = root.children[0];
        }
    }

    private void remove(BTreeNode node, int value) {
        int idx = findKeyIndex(node, value);

        // Случай 1: ключ найден в текущем узле
        if (idx < node.n && node.keys[idx] == value) {
            if (node.isLeaf) {
                // Случай 1а: лист — просто удаляем
                removeFromLeaf(node, idx);
            } else {
                // Случай 1б: внутренний узел
                removeFromNonLeaf(node, idx);
            }
        } else {
            // Случай 2: ключ не найден, спускаемся вниз
            if (node.isLeaf) {
                removeOperations++;
                return;
            }

            boolean flag = (idx == node.n);

            // Если ребёнок, в который нужно спуститься, имеет мало ключей
            if (node.children[idx].n < degree) {
                fill(node, idx);
            }

            // После заполнения могло измениться количество ключей
            if (flag && idx > node.n) {
                remove(node.children[idx - 1], value);
            } else {
                remove(node.children[idx], value);
            }
        }
    }

    private int findKeyIndex(BTreeNode node, int value) {
        int idx = 0;
        while (idx < node.n && node.keys[idx] < value) {
            removeOperations++;
            idx++;
        }
        return idx;
    }

    private void removeFromLeaf(BTreeNode node, int idx) {
        for (int i = idx + 1; i < node.n; i++) {
            removeOperations++;
            node.keys[i - 1] = node.keys[i];
        }
        node.n--;
        removeOperations++;
    }

    private void removeFromNonLeaf(BTreeNode node, int idx) {
        int key = node.keys[idx];

        if (node.children[idx].n >= degree) {
            // Берём предшественника (максимальный ключ в левом поддереве)
            int pred = getPredecessor(node, idx);
            node.keys[idx] = pred;
            removeOperations++;
            remove(node.children[idx], pred);
        } else if (node.children[idx + 1].n >= degree) {
            // Берём преемника (минимальный ключ в правом поддереве)
            int succ = getSuccessor(node, idx);
            node.keys[idx] = succ;
            removeOperations++;
            remove(node.children[idx + 1], succ);
        } else {
            // Объединяем левого и правого ребёнка
            merge(node, idx);
            remove(node.children[idx], key);
        }
    }

    private int getPredecessor(BTreeNode node, int idx) {
        BTreeNode cur = node.children[idx];
        while (!cur.isLeaf) {
            removeOperations++;
            if (cur.n == 0) {
                break;
            }
            cur = cur.children[cur.n];
        }
        removeOperations++;
        return (cur.n > 0) ? cur.keys[cur.n - 1] : node.keys[idx];
    }

    private int getSuccessor(BTreeNode node, int idx) {
        BTreeNode cur = node.children[idx + 1];
        while (!cur.isLeaf) {
            removeOperations++;
            if (cur.n == 0) {
                break;
            }
            cur = cur.children[0];
        }
        removeOperations++;
        return (cur.n > 0) ? cur.keys[0] : node.keys[idx];
    }

    private void fill(BTreeNode node, int idx) {
        if (idx != 0 && node.children[idx - 1].n >= degree) {
            borrowFromPrev(node, idx);
        } else if (idx != node.n && node.children[idx + 1].n >= degree) {
            borrowFromNext(node, idx);
        } else {
            if (idx != node.n) {
                merge(node, idx);
            } else {
                merge(node, idx - 1);
            }
        }
    }

    private void borrowFromPrev(BTreeNode node, int idx) {
        BTreeNode child = node.children[idx];
        BTreeNode sibling = node.children[idx - 1];

        for (int i = child.n - 1; i >= 0; i--) {
            removeOperations++;
            child.keys[i + 1] = child.keys[i];
        }

        if (!child.isLeaf) {
            for (int i = child.n; i >= 0; i--) {
                removeOperations++;
                child.children[i + 1] = child.children[i];
            }
        }

        child.keys[0] = node.keys[idx - 1];
        removeOperations++;

        if (!child.isLeaf) {
            child.children[0] = sibling.children[sibling.n];
            removeOperations++;
        }

        node.keys[idx - 1] = sibling.keys[sibling.n - 1];
        removeOperations++;

        child.n++;
        sibling.n--;
        removeOperations += 2;
    }

    private void borrowFromNext(BTreeNode node, int idx) {
        BTreeNode child = node.children[idx];
        BTreeNode sibling = node.children[idx + 1];

        child.keys[child.n] = node.keys[idx];
        removeOperations++;

        if (!child.isLeaf) {
            child.children[child.n + 1] = sibling.children[0];
            removeOperations++;
        }

        node.keys[idx] = sibling.keys[0];
        removeOperations++;

        for (int i = 1; i < sibling.n; i++) {
            removeOperations++;
            sibling.keys[i - 1] = sibling.keys[i];
        }

        if (!sibling.isLeaf) {
            for (int i = 1; i <= sibling.n; i++) {
                removeOperations++;
                sibling.children[i - 1] = sibling.children[i];
            }
        }

        child.n++;
        sibling.n--;
        removeOperations += 2;
    }

    private void merge(BTreeNode node, int idx) {
        BTreeNode child = node.children[idx];
        BTreeNode sibling = node.children[idx + 1];

        child.keys[degree - 1] = node.keys[idx];
        removeOperations++;

        for (int i = 0; i < sibling.n; i++) {
            removeOperations++;
            child.keys[i + degree] = sibling.keys[i];
        }

        if (!child.isLeaf) {
            for (int i = 0; i <= sibling.n; i++) {
                removeOperations++;
                child.children[i + degree] = sibling.children[i];
            }
        }

        for (int i = idx + 1; i < node.n; i++) {
            removeOperations++;
            node.keys[i - 1] = node.keys[i];
        }

        for (int i = idx + 2; i <= node.n; i++) {
            removeOperations++;
            node.children[i - 1] = node.children[i];
        }

        child.n += sibling.n + 1;
        node.n--;
        removeOperations += 2;
    }

    public void traverse() {
        traverse(root);
        System.out.println();
    }
    private void traverse(BTreeNode node) {
        int i;
        for (i = 0; i < node.n; i++) {
            if (!node.isLeaf) {
                traverse(node.children[i]);
            }
            System.out.print(node.keys[i] + " ");
        }
        if (!node.isLeaf) {
            traverse(node.children[i]);
        }
    }

    public static void main(String[] args) {
        Random random = new Random();
        BTree tree = new BTree(3);
        int[] data = new int[10000];

        // Генерация массива
        for (int i = 0; i < data.length; i++) {
            data[i] = random.nextInt(100000);
        }

        long totalAddTime = 0;
        long totalAddOps = 0;
        for (int value : data) {
            tree.addOperations = 0;
            long start = System.nanoTime();
            tree.add(value);
            long end = System.nanoTime();
            totalAddTime += (end - start);
            totalAddOps += tree.addOperations;
        }

        double avgAddTime = totalAddTime / 10000.0;
        double avgAddOps = totalAddOps / 10000.0;
        System.out.printf("Среднее время вставки: %.2f нс\n", avgAddTime);
        System.out.printf("Среднее число операций вставки: %.2f\n\n", avgAddOps);
        System.out.printf("Общее число вставки: %d\n\n", totalAddOps);

        long totalSearchTime = 0;
        long totalSearchOps = 0;
        for (int i = 0; i < 100; i++) {
            int value = data[random.nextInt(data.length)];
            tree.searchOperations = 0;
            long start = System.nanoTime();
            tree.contains(value);
            long end = System.nanoTime();
            totalSearchTime += (end - start);
            totalSearchOps += tree.searchOperations;
        }

        double avgSearchTime = totalSearchTime / 100.0;
        double avgSearchOps = totalSearchOps / 100.0;
        System.out.printf("Среднее время поиска: %.2f нс\n", avgSearchTime);
        System.out.printf("Среднее число операций поиска: %.2f\n\n", avgSearchOps);
        System.out.printf("Общее число поиска: %d\n\n", totalSearchOps);
        long totalRemoveTime = 0;
        long totalRemoveOps = 0;

        for (int i = 0; i < 1000; i++) {
            int value = data[random.nextInt(data.length)];
            tree.removeOperations = 0;
            long start = System.nanoTime();
            tree.remove(value);
            long end = System.nanoTime();
            totalRemoveTime += (end - start);
            totalRemoveOps += tree.removeOperations;
        }

        double avgRemoveTime = totalRemoveTime / 1000.0;
        double avgRemoveOps = totalRemoveOps / 1000.0;
        System.out.printf("Среднее время удаления: %.2f нс\n", avgRemoveTime);
        System.out.printf("Среднее число операций удаления: %.2f\n\n", avgRemoveOps);
        System.out.printf("Общее число удалений: %d\n\n", totalRemoveOps);

    }
}