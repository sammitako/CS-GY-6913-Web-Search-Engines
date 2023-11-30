import io.DocumentSerializer;
import model.Document;
import model.DocumentScore;
import model.Lexicon;
import io.LexiconFileHandler;
import io.Util;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.*;

public class SearchEngine {
    static String documentDataFilePath = "/Users/sammita/Projects/nyu-grad/CS-GY 6913 Web Search Engines/Search Engines/src/files/document_data.ser";
    static String invertedIndexFilePath = "/Users/sammita/Projects/nyu-grad/CS-GY 6913 Web Search Engines/Search Engines/src/files/inverted_index.bin";
    static String metadataFilePath = "/Users/sammita/Projects/nyu-grad/CS-GY 6913 Web Search Engines/Search Engines/src/files/metadata.bin";
    static String lexiconFilePath = "/Users/sammita/Projects/nyu-grad/CS-GY 6913 Web Search Engines/Search Engines/src/files/lexicon.bin";
    public static void main(String[] args) {
        long fileReadStartTime = System.currentTimeMillis(); // Start timing file reading
        System.out.println("Reading files...");


        // Read lexicon and document information from files
        Map<String, Lexicon> lexiconMap = LexiconFileHandler.readLexicon(lexiconFilePath);
        List<Document> documents = DocumentSerializer.deserializeDocumentList(documentDataFilePath);
        RandomAccessFile metadataFile;
        RandomAccessFile indexFile;
        int docCount = documents.size();
        double avgDocLen = documents.stream().mapToInt(Document::getTermCount).average().orElse(0.0);
        double k1 = 1.5;
        double b = 0.75;
        int resultSize = 10;

        // Open metadata and index files
        try {
            metadataFile = new RandomAccessFile(metadataFilePath, "r");
            indexFile = new RandomAccessFile(invertedIndexFilePath, "r");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        long fileReadEndTime = System.currentTimeMillis(); // End timing file reading
        String fileReadDuration = Util.calculateAndFormatDuration(fileReadStartTime, fileReadEndTime);
        System.out.println("Time taken to read files: " + fileReadDuration);



        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.println("=====================================================");

            System.out.print("Enter query type (AND/OR, type 'QUIT' to stop): ");
            String queryType = scanner.nextLine().trim().toUpperCase();

            // Stop the loop if the user types 'ESC'
            if ("QUIT".equals(queryType)) {
                break;
            }

            System.out.print("Enter query (separate by the space) : ");
            String query = scanner.nextLine();

            try {
                long startTime = System.currentTimeMillis(); // Start timing
                PriorityQueue<DocumentScore> results;

                switch (queryType) {
//                    case "SINGLE" -> {
//                        assert lexiconMap != null;
//                        results = QueryProcessor.processSingleTermQuery(query, lexiconMap, metadataFile, indexFile, documents, docCount, avgDocLen, k1, b, resultSize);
//                    }
                    case "AND" ->
                            results = QueryProcessor.processAndQuery(query, lexiconMap, metadataFile, indexFile, documents, docCount, avgDocLen, k1, b, resultSize);
                    case "OR" ->
                            results = QueryProcessor.processOrQuery(query, lexiconMap, metadataFile, indexFile, documents, docCount, avgDocLen, k1, b, resultSize);
                    default -> {
                        System.out.println("Invalid query type.");
                        continue;
                    }
                }

                long endTime = System.currentTimeMillis(); // End timing
                String duration = Util.calculateAndFormatDuration(startTime, endTime);
                System.out.println("Query processing time: " + duration);

                // Display results
                while (!results.isEmpty()) {
                    DocumentScore doc = results.poll();
                    // Document Id: " + docId + ", URL: " + url + ", Term Count: " + termCount
                    System.out.println(documents.get(doc.getDocId()) + ", Score: " + String.format("%.6f", doc.getScore()));
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        System.out.println("Bye!");
        scanner.close();
    }
}