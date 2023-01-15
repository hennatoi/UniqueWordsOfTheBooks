package com.anttijuustila.tira;

import java.io.*;
import java.nio.charset.Charset;

public class BookImplementation implements Book {

    private String fileName;
    private String ignoreWordsFile;
    WordCount[] words = new WordCount[4500000];
    String[] ignoreWords = new String[50];
    private int totalWordCounter = 0;
    private int collisionCount = 0;
    private int uniqueWordCounter = 0;
    private int ignoreWordsCounter = 0;
    private WordCount[] cleaned;
    private int ignoreWordsInBookFile = 0;

    private class WordCount {
        public String word;
        int counter;

        WordCount(String word) {
            this.word = word;
            counter = 1;
        }
    }

    @Override
    public void setSource(String fileName, String ignoreWordsFile) throws FileNotFoundException {

        File book = new File(fileName);

        if (book.isFile()) {
            System.out.println(book + " Exists");
        } else {
            throw new FileNotFoundException();
        }

        File file = new File(ignoreWordsFile);

        if (file.isFile()) {
            System.out.println(file + "Exists");
        } else {
            throw new FileNotFoundException();
        }

        this.fileName = fileName;
        this.ignoreWordsFile = ignoreWordsFile;

    }

    @Override
    public void countUniqueWords() throws IOException, OutOfMemoryError {

        String rivi;
        int j = 0;

        Charset utf8 = Charset.forName("UTF-8");
        try (BufferedReader file = new BufferedReader(new InputStreamReader(
                new FileInputStream(ignoreWordsFile), utf8))) {

            while ((rivi = file.readLine()) != null) {
                String[] words = rivi.split(",");
                for (int i = 0; i < words.length; i++) {
                    ignoreWords[j] = words[i];
                    j++;
                    ignoreWordsCounter++;
                }
            }
            file.close();

        } catch (IOException e) {
            System.out.println("Tiedostoa ei löydy");
            e.printStackTrace();
            throw new IOException();
        } catch (OutOfMemoryError e) {
            System.out.println("Tiedostoa ei löydy");
            e.printStackTrace();
            throw new OutOfMemoryError();
        }
        try (BufferedReader bookfile = new BufferedReader(new InputStreamReader(
                new FileInputStream(fileName), utf8))) {
            int[] sana = new int[500];
            int merkki;
            int merkkienLkm = 0;
            while ((merkki = bookfile.read()) != -1) {

                if (Character.isLetter(merkki)) {
                    sana[merkkienLkm] = merkki;
                    merkkienLkm++;
                } else {

                    String word = new String(sana, 0, merkkienLkm);
                    word = word.toLowerCase();
                    merkkienLkm = 0;

                    if (word.length() >= 2 && !onIgnorattava(word)) {
                        int hashValue = hashCode(word);
                        int index = ((hashValue) & 0x7fffffff) % words.length;

                        if (words[index] != null && words[index].word.equals(word)) {
                            words[index].counter++;
                            totalWordCounter++;
                        } else {
                            while (words[index] != null && !words[index].word.equals(word)) {
                                collisionCount++;
                                index = (index + 1) % words.length;
                                if(words[index] != null && words[index].word.equals(word)){
                                    words[index].counter++;
                                    totalWordCounter++;
                                    break;
                                }
                            }
                            if (words[index] == null) {
                                words[index] = new WordCount(word);
                                totalWordCounter++;
                                uniqueWordCounter++;
                            }
                        }
                    } else {
                        ignoreWordsInBookFile++;
                    }
                }
            }
            bookfile.close();

        } catch (IOException e) {
            System.out.println("Tiedostoa ei löydy");
            e.printStackTrace();
            throw new IOException();
        } catch (OutOfMemoryError e) {
            System.out.println("Tiedostoa ei löydy");
            e.printStackTrace();
            throw new OutOfMemoryError();
        }

        createNewTable();
    }

    public int hashCode(String word) {
        int hash = 31;
        for (int i = 0; i < word.length(); i++) {
            hash = (hash * 31 + word.charAt(i));
        }
        return hash;
    }

    public boolean onIgnorattava(String word) {
        for (String sana : ignoreWords) {
            if (sana == null) {
                break;
            }
            if (sana.equalsIgnoreCase(word)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void report() {

        if (cleaned.length < 100) {
            for (int i = 0; i < cleaned.length; i++) {
                System.out.println((i + 1) + " " + cleaned[i].word);
            }
        } else {
            for (int i = 0; i < 100; i++) {
                System.out.println((i + 1) + " " + cleaned[i].word);
            }
        }
        System.out.println("Total word counter: " + totalWordCounter);
        System.out.println("Unique words: " + uniqueWordCounter);
        System.out.println("Ignore words in Ignore file: " + ignoreWordsCounter);
        System.out.println("Ignore words in Book file: " + ignoreWordsInBookFile);
        System.out.println("Collisions: " + collisionCount);
    }

    public void heapsort(WordCount[] words, int length) {

        heapify(words, length);
        int end = length - 1;
        while (end > 0) {
            WordCount temp = words[0];
            words[0] = words[end];
            words[end] = temp;
            end--;
            siftDown(words, 0, end);
        }
    }

    public void heapify(WordCount[] words, int count) {
        int start = parent(count - 1);
        while (start >= 0) {
            siftDown(words, start, count - 1);
            start--;
        }
    }

    public void siftDown(WordCount[] words, int start, int end) {
        int root = start;
        while (leftChild(root) <= end) {
            int child = leftChild(root);
            int swap = root;
            if (words[swap].counter > words[child].counter) {
                swap = child;
            }
            if (child + 1 <= end && words[swap].counter > words[child + 1].counter) {
                swap = child + 1;
            }
            if (swap == root) {
                return;
            } else {
                WordCount temp = words[root];
                words[root] = words[swap];
                words[swap] = temp;
                root = swap;
            }
        }
    }

    public int parent(int count) {
        return (int) Math.floor((count - 1) / 2);
    }

    public int leftChild(int count) {
        return 2 * count + 1;
    }

    public int rightChild(int count) {
        return 2 * count + 2;
    }

    private void createNewTable() {
        int counter = 0;

        for (int i = 0; i < words.length; i++) {
            if (words[i] != null) {
                counter++;
            }
        }
        cleaned = new WordCount[counter];

        int j = 0;

        for (int i = 0; i < words.length; i++) {
            if (words[i] != null) {
                cleaned[j] = words[i];
                j++;
            }
        }

        heapsort(cleaned, cleaned.length);
    }

    @Override
    public void close() {
        cleaned = null;
    }

    @Override
    public int getUniqueWordCount() {
        return uniqueWordCounter;
    }

    @Override
    public int getTotalWordCount() {
        return totalWordCounter;
    }

    @Override
    public String getWordInListAt(int position) {

        if (position >= 100) {
            return null;
        }
        if (cleaned.length < 100 && position >= cleaned.length) {
            return null;
        }
        if (position < 0) {
            return null;
        }
        if (cleaned.length == 0) {
            return null;
        }
        String result = cleaned[position].word;
        return result;
    }

    @Override
    public int getWordCountInListAt(int position) {

        if (position >= 100) {
            return -1;
        }
        if (cleaned.length < 100 && position >= cleaned.length) {
            return -1;
        }
        if (position < 0) {
            return -1;
        }
        if (cleaned.length == 0) {
            return -1;
        }
        int result = cleaned[position].counter;
        return result;
    }
}
