package io;
import model.Document;
import model.Posting;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Pattern;

public class DocumentParser {
    /**
     * <DOC>
     *     <DOCNO>D301595</DOCNO>
     *     <TEXT>
     *         HyperLink
     *         Content
     *     </TEXT>
     * </DOC>
     */

    private static List<Document> documentList = new ArrayList<>();
    private static int docId = -1;
    public static List<Document> getdocumentList() {
        return documentList;
    }

    public static class IndexWrapper {
        public int value;

        public IndexWrapper(int value) {
            this.value = value;
        }
    }
    /**
     * Parses terms, sorts, and writes them to temporary files
     *
     * @param sourceFilePath    path of the source data file
     * @param temporaryFilePath path to store temporary files
     * @param metadataFilePath  path to store document metadata
     */


    public static void parseDocuments(String sourceFilePath, String temporaryFilePath, String metadataFilePath) throws IOException {
        // Initialization
        int termCount = 0;
        String currentDocumentURL = null;
        List<Posting> postings = new ArrayList<>();
        boolean isProcessingText = false;
        boolean isFirstLineInText = false;
        Map<String, Integer> termFrequencyMap = new HashMap<>();

        int tempFileCount = 0;
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(sourceFilePath), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                // Read Document
                if (line.startsWith("<TEXT>")) {
                    isProcessingText = true;
                    isFirstLineInText = true;
                    continue;
                }
                // Save Document
                if (line.startsWith("</TEXT>")) {
                    isProcessingText = false;

                    for (Map.Entry<String, Integer> entry : termFrequencyMap.entrySet()) {
                        postings.add(new Posting(entry.getKey(), docId, entry.getValue()));
                    }
                    termFrequencyMap = new HashMap<>();

                    if (postings.size() > 10000000) {
                        Collections.sort(postings);
                        writePostingsToTempFile(postings, temporaryFilePath + "temp" + tempFileCount++ + ".txt");
                        postings.clear();
                    }
                    documentList.add(new Document(docId, currentDocumentURL, termCount));
                    termCount = 0;
                    currentDocumentURL = null;
                    continue;
                }

                if (isProcessingText && isFirstLineInText) {
                    docId++;
                    currentDocumentURL = line.trim();
                    isFirstLineInText = false;
                    continue;
                }

                // Term extraction
                if (isProcessingText) {
                    termCount += tokenizeAndCount(line, termFrequencyMap);
                }
            }
            saveDocumentList(documentList, metadataFilePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
        // After completing the reading of the file, check if there are any remaining postings
        if (!postings.isEmpty()) {
            Collections.sort(postings);
            writePostingsToTempFile(postings, temporaryFilePath + "temp" + tempFileCount + ".txt");
            postings.clear();
        }
    }


    /**
     * Writes the postings buffer to a temporary file.
     * Each posting is written as a separate line.
     *
     * @param postingsBuffer The list of postings to write.
     * @param filePath       The path of the temporary file.
     */
    // Automatically closes resources at the end of the statement (BufferedWriter, ObjectOutputStream)
    private static void writePostingsToTempFile(List<Posting> postingsBuffer, String filePath) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(filePath), StandardCharsets.UTF_8))) {
            for (Posting posting : postingsBuffer) {
                writer.write(posting.toString());
                writer.newLine();
            }
        }

    }

    /**
     * Saves the list of parsed documents to a file.
     * Serialization is used to store the document data.
     *
     * @param documents The list of documents to save.
     * @param filename  The name of the file to save the data to.
     */
    private static void saveDocumentList(List<Document> documents, String filename) throws IOException {
        try (ObjectOutputStream objectOutputStream = new ObjectOutputStream(new FileOutputStream(filename))) {
            objectOutputStream.writeObject(documents);
        }
    }

    private static int tokenizeAndCount(String line, Map<String, Integer> termFrequencyMap) {
        line = removeHtmlTags(line);

        String[] splits = line.split("[\\s.]");
        int wordCount = 0;

        for (String split : splits) {
            split = cleanToken(split);
            if (!split.isEmpty()) {
                termFrequencyMap.put(split, termFrequencyMap.getOrDefault(split, 0) + 1);
                wordCount++;
            }
        }

        return wordCount;
    }

    private static String removeHtmlTags(String content) {
        return content.replaceAll("<[^>]*>", "");
    }

    private static String cleanToken(String token) {
        token = token.trim();
        token = token.replaceAll("^\\P{L}+", "").replaceAll("\\P{L}+$", "");
        return token.toLowerCase(Locale.ROOT);
    }

    /**
     * document_data.ser
     * Document ID: 1, URL: http://example.com/page1, Term Count: 150
     * Document ID: 2, URL: http://example.com/page2, Term Count: 200
     *
     * temp*.txt
     * term1 docId1 termFreq1
     * term1 docId2 termFreq2
     * term2 docId1 termFreq3
     */

    public static void main(String[] args) throws IOException {
        String srcFilePath = "/Users/sammita/Projects/nyu-grad/CS-GY 6913 Web Search Engines/Search Engines/src/files/msmarco-docs.trec";
        String tempFilePath = "/Users/sammita/Projects/nyu-grad/CS-GY 6913 Web Search Engines/Search Engines/src/files/temp/";
        String dataFilePath = "/Users/sammita/Projects/nyu-grad/CS-GY 6913 Web Search Engines/Search Engines/src/files/document_data.ser";

        // Start timer
        long startTime = System.currentTimeMillis();

        parseDocuments(srcFilePath, tempFilePath, dataFilePath);

        // End timer and calculate elapsed time
        long endTime = System.currentTimeMillis();
        long elapsedTime = endTime - startTime;

        // Format and print the elapsed time
        String formattedTime = Util.formatTime(elapsedTime);
        System.out.println("Parsing completed in: " + formattedTime); // Parsing completed in: 00:46:000034 (temp116.txt)
    }
}
