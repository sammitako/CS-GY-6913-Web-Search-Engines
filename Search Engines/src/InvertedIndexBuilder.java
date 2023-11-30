import compression.MetadataCompressor;
import io.LexiconFileHandler;
import io.Util;
import model.Lexicon;
import model.Posting;
import compression.PostingBlockCompressor;

import java.io.*;
import java.util.*;
public class InvertedIndexBuilder {
    private static final int MAX_BLOCK_SIZE = 128;
    private static Map<String, Lexicon> termLexicon = new HashMap<>();
    public static Map<String, Lexicon> getTermLexicon() {
        return termLexicon;
    }

    private static void createCompressedIndex(String sourceFilePath, String indexFilePath,
                                              String metadataPath, String lexiconPath) {
        try (
                BufferedReader fileReader = new BufferedReader(new FileReader(sourceFilePath));
                RandomAccessFile indexFile = new RandomAccessFile(indexFilePath, "rw");
                RandomAccessFile metadataFile = new RandomAccessFile(metadataPath, "rw")
        ) {
            String line;
            List<Posting> currentBlock = new ArrayList<>(MAX_BLOCK_SIZE);
            String currentTerm = "";
            int totalDocs = 0;
            int blockCount = 0;

            /*
             *  <METADATA>
             *  lastDocIds
             *  docIdBlockStarts
             *  docIdBlockSizes
             *  termFreqBlockStarts
             *  termFreqBlockSizes
             */
            List<Integer> lastDocIds = new ArrayList<>();
            List<Long> docIdBlockStarts = new ArrayList<>();
            List<Integer> docIdBlockSizes = new ArrayList<>();
            List<Long> termFreqBlockStarts = new ArrayList<>();
            List<Integer> termFreqBlockSizes = new ArrayList<>();

            // Track the starting position of the inverted list
            long startOffset = 0;

            while ((line = fileReader.readLine()) != null) {
                // Extracting Terms
                if(line.trim().isEmpty()) continue; // Skip empty lines
                String[] parts = line.split(" ");
                String term = parts[0];
                int docId = Integer.parseInt(parts[1]);
                int termFreq = Integer.parseInt(parts[2]);

                // When a new term is encountered
                if (!currentTerm.equals(term)) {
                    // Compress the block if it's not empty
                    if (!currentBlock.isEmpty()) {
                        blockCount++;
                        PostingBlockCompressor.compressPostingBlock(currentBlock, indexFile, lastDocIds, docIdBlockStarts, docIdBlockSizes, termFreqBlockStarts, termFreqBlockSizes);
                        currentBlock.clear();
                    }
                    // Write lexicon entry if the current term is not empty
                    if (!currentTerm.isEmpty()) {
                        writeLexiconEntry(currentTerm, totalDocs, blockCount, startOffset, indexFile, metadataFile, lastDocIds, docIdBlockStarts, docIdBlockSizes, termFreqBlockStarts, termFreqBlockSizes);
                    }
                    // Reset the current term and document counters
                    currentTerm = term;
                    totalDocs = 0;
                    blockCount = 0;

                    // Clear metadata lists
                    lastDocIds.clear();
                    docIdBlockStarts.clear();
                    docIdBlockSizes.clear();
                    termFreqBlockStarts.clear();
                    termFreqBlockSizes.clear();

                    // Update start offset for the new term
                    startOffset = indexFile.getFilePointer();
                }

                // Add the posting to the current block
                currentBlock.add(new Posting(term, docId, termFreq));
                totalDocs++;

                // If the block is full, compress it
                if (currentBlock.size() == MAX_BLOCK_SIZE) {
                    blockCount++;
                    PostingBlockCompressor.compressPostingBlock(currentBlock, indexFile, lastDocIds, docIdBlockStarts, docIdBlockSizes, termFreqBlockStarts, termFreqBlockSizes);
                    currentBlock.clear();
                }
            }

            // Compress the last block
            if (!currentBlock.isEmpty()) {
                blockCount++;
                PostingBlockCompressor.compressPostingBlock(currentBlock, indexFile, lastDocIds, docIdBlockStarts, docIdBlockSizes, termFreqBlockStarts, termFreqBlockSizes);
            }

            // Write the last lexicon entry
            writeLexiconEntry(currentTerm, totalDocs, blockCount, startOffset, indexFile, metadataFile, lastDocIds, docIdBlockStarts, docIdBlockSizes, termFreqBlockStarts, termFreqBlockSizes);

            // Write the term lexicon to a file
            LexiconFileHandler.writeLexicon(termLexicon, lexiconPath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /*
     *  <LEXICON>
     *  startOffset
     *  endOffset
     *  blockCount
     *  documentFrequency
     *  metadataPosition
     */
    private static void writeLexiconEntry(String term, int totalDocs, int blockCount, long startOffset,
                                          RandomAccessFile indexFile, RandomAccessFile metadataFile,
                                          List<Integer> lastDocIds, List<Long> docIdBlockStarts,
                                          List<Integer> docIdBlockSizes, List<Long> termFreqBlockStarts,
                                          List<Integer> termFreqBlockSizes) throws IOException {

        Lexicon lexiconEntry = new Lexicon(startOffset, indexFile.getFilePointer(), blockCount, totalDocs, metadataFile.getFilePointer());
        termLexicon.put(term, lexiconEntry);

        // Compress and write metadata
        MetadataCompressor.compressMetadata(metadataFile, lastDocIds.stream().mapToInt(i -> i).toArray(), docIdBlockStarts.stream().mapToLong(l -> l).toArray(), docIdBlockSizes.stream().mapToInt(i -> i).toArray(), termFreqBlockStarts.stream().mapToLong(l -> l).toArray(), termFreqBlockSizes.stream().mapToInt(i -> i).toArray());
    }


    // 1. DocumentParser
    // 2. SortedFileMerger
    // 3. InvertedIndexBuilder
    public static void main(String[] args) {
        String mergedFilePath = "/Users/sammita/Projects/nyu-grad/CS-GY 6913 Web Search Engines/Search Engines/src/files/merged.txt";
        String invertedIndexFilePath = "/Users/sammita/Projects/nyu-grad/CS-GY 6913 Web Search Engines/Search Engines/src/files/inverted_index.bin";
        String metadataFilePath = "/Users/sammita/Projects/nyu-grad/CS-GY 6913 Web Search Engines/Search Engines/src/files/metadata.bin";
        String lexiconFilePath = "/Users/sammita/Projects/nyu-grad/CS-GY 6913 Web Search Engines/Search Engines/src/files/lexicon.bin";

        // Start timer
        long startTime = System.currentTimeMillis();

        createCompressedIndex(mergedFilePath, invertedIndexFilePath, metadataFilePath, lexiconFilePath);

        // End timer and calculate elapsed time
        long endTime = System.currentTimeMillis();
        long elapsedTime = endTime - startTime;

        // Format and print the elapsed time
        String formattedTime = Util.formatTime(elapsedTime);

        /*
         * Building Inverted Index completed in: 01:55:000041 / 02:06:000007
         * inverted_index.bin: 2.72 GB
         * metadata.bin: 614.6 MB
         * lexicon.bin: 1.52 GB (00:11:000034)
         **/
        System.out.println("Building Inverted Index completed in: " + formattedTime);
    }
}