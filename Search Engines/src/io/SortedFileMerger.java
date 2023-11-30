package io;

import model.MergedPosting;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;

public class SortedFileMerger {
    /**
     * Merges sorted temporary files into a single file.
     * @param sourcePath Path to the directory containing temporary files.
     * @param destinationPath Path for the merged output file.
     */
    public static void mergeSortedFiles(String sourcePath, String destinationPath) {
        PriorityQueue<MergedPosting> postingsQueue = new PriorityQueue<>();
        Map<Integer, BufferedReader> readersMap = new HashMap<>();

        try (BufferedWriter mergedFileWriter = new BufferedWriter(new FileWriter(destinationPath))) {
            // Open and initialize readers for each file
            // Java's Stream API
            Files.list(Paths.get(sourcePath))
                    .filter(path -> path.toString().endsWith(".txt"))
                    .forEach(path -> initializeReader(path, postingsQueue, readersMap));

            // Merge process
            // Maintain the sorted order of postings
            while (!postingsQueue.isEmpty()) {
                MergedPosting currentPosting = postingsQueue.poll();
                processNextPosting(currentPosting, readersMap, postingsQueue, mergedFileWriter);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            closeAllReaders(readersMap);
        }
    }

    private static void initializeReader(Path filePath, PriorityQueue<MergedPosting> queue, Map<Integer, BufferedReader> readers) {
        try {
            String fileName = filePath.getFileName().toString();
            int fileIndex = Util.getIndexFromFilename(fileName);
            BufferedReader reader = new BufferedReader(new FileReader(filePath.toFile()));
            readers.put(fileIndex, reader);
            String line = reader.readLine();
            if (line != null) {
                queue.add(new MergedPosting(line, fileIndex));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void processNextPosting(MergedPosting posting, Map<Integer, BufferedReader> readers, PriorityQueue<MergedPosting> queue, BufferedWriter writer) throws IOException {
        BufferedReader currentReader = readers.get(posting.getFileIndex());
        String nextLine = currentReader.readLine();
        if (nextLine != null) {
            queue.add(new MergedPosting(nextLine, posting.getFileIndex()));
        }
        writer.write(posting.toString());
        if (!queue.isEmpty()) writer.newLine();
    }

    private static void closeAllReaders(Map<Integer, BufferedReader> readers) {
        readers.values().forEach(reader -> {
            try {
                if (reader != null) reader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    public static void main(String[] args) {
        String tempDirectoryPath = "/Users/sammita/Projects/nyu-grad/CS-GY 6913 Web Search Engines/Search Engines/src/files/temp/";
        String mergedFilePath = "/Users/sammita/Projects/nyu-grad/CS-GY 6913 Web Search Engines/Search Engines/src/files/merged.txt";

        // Start timer
        long startTime = System.currentTimeMillis();
        mergeSortedFiles(tempDirectoryPath, mergedFilePath);

        // End timer and calculate elapsed time
        long endTime = System.currentTimeMillis();
        long elapsedTime = endTime - startTime;

        // Format and print the elapsed time
        String formattedTime = Util.formatTime(elapsedTime);
        System.out.println("Merging completed in: " + formattedTime); // Merging completed in: 00:37:000055 (20.03 GB)
    }
}
