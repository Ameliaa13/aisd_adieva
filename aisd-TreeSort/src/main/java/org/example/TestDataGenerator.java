package org.example;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;


public class TestDataGenerator {

    private static final int COUNT_SETS = 80;
    private static final int MIN_SIZE = 100;
    private static final int MAX_SIZE = 10_000;
    private static final int MIN_VALUE = 0;
    private static final int MAX_VALUE = 100_000;
    private static final String OUTPUT_FILE = "test_data.txt";
    private static final boolean APPEND = false;

    public static void main(String[] args) {
        Random random = new Random();


        try (BufferedWriter writer = new BufferedWriter(new FileWriter(OUTPUT_FILE, APPEND))) {
            for (int setIdx = 1; setIdx <= COUNT_SETS; setIdx++) {
                int size = MIN_SIZE + random.nextInt(MAX_SIZE - MIN_SIZE + 1);

                int[] data = new int[size];
                for (int i = 0; i < size; i++) {
                    data[i] = MIN_VALUE + random.nextInt(MAX_VALUE - MIN_VALUE + 1);
                }

                StringBuilder line = new StringBuilder();
                for (int value : data) {
                    line.append(' ').append(value);
                }
                writer.write(line.toString());
                writer.newLine();

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

