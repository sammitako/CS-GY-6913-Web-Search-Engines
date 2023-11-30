package compression;

import model.Posting;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;
/**
 * Utilities for compressing and decompressing blocks of postings data.
 */
public class PostingBlockCompressor {
    /**
     * Compresses a block of postings and updates metadata.
     * @param postings List of postings to compress.
     * @param indexFile RandomAccessFile to write compressed data.
     * @param lastDocIds List to store the last document IDs of each block.
     * @param docIdBlockStarts List to store start positions of each docID block.
     * @param docIdBlockSizes List to store sizes of each docID block.
     * @param termFreqBlockStarts List to store start positions of each frequency block.
     * @param termFreqBlockSizes List to store sizes of each frequency block.
     * @throws IOException If an I/O error occurs.
     */
    public static void compressPostingBlock(List<Posting> postings, RandomAccessFile indexFile,
                                     List<Integer> lastDocIds, List<Long> docIdBlockStarts,
                                     List<Integer> docIdBlockSizes, List<Long> termFreqBlockStarts,
                                     List<Integer> termFreqBlockSizes) throws IOException {

        long docIdBlockStart = indexFile.getFilePointer(); // Get starting position of the docID block
        int docIdBlockSize = compressDocIds(postings, indexFile); // Compress document IDs
        long termFreqBlockStart = indexFile.getFilePointer(); // Get starting position of the term frequency block
        int termFreqBlockSize = compressTermFreqs(postings, indexFile); // Compress term frequencies

        // Updating metadata lists with new block information
        lastDocIds.add(postings.get(postings.size() - 1).docId);
        docIdBlockStarts.add(docIdBlockStart);
        docIdBlockSizes.add(docIdBlockSize);
        termFreqBlockStarts.add(termFreqBlockStart);
        termFreqBlockSizes.add(termFreqBlockSize);
    }

    /**
     * Compresses document IDs from a list of postings.
     * @param postings List of postings.
     * @param indexFile RandomAccessFile to write to.
     * @return Size of the compressed block.
     * @throws IOException If an I/O error occurs.
     */
    private static int compressDocIds(List<Posting> postings, RandomAccessFile indexFile) throws IOException {
        int lastDocId = 0;
        int blockSize = 0;
        for (Posting posting : postings) {
            int delta = posting.docId - lastDocId;  // Calculate difference (delta) encoding for docIDs
            VarByte.encodeVarInt(indexFile, delta); // Encode and write the delta value
            lastDocId = posting.docId; // Update lastDocId for next iteration
            blockSize++; // Increment block size
        }
        return blockSize; // Return the size of the compressed block
    }

    /**
     * Compresses frequencies from a list of postings.
     * @param postings List of postings.
     * @param indexFile RandomAccessFile to write to.
     * @return Size of the compressed block.
     * @throws IOException If an I/O error occurs.
     */
    private static int compressTermFreqs(List<Posting> postings, RandomAccessFile indexFile) throws IOException {
        int blockSize = 0;
        for (Posting posting : postings) {
            VarByte.encodeVarInt(indexFile, posting.termFreq); // Encode and write term frequency
            blockSize++; // Increment block size
        }
        return blockSize; // Return the size of the compressed block
    }

    /**
     * Decompresses document IDs from a compressed block.
     * @param indexFile RandomAccessFile to read from.
     * @param blockSize Size of the block to decompress.
     * @return List of decompressed document IDs.
     * @throws IOException If an I/O error occurs.
     */
    public static List<Integer> decompressDocIds(RandomAccessFile indexFile, int blockSize) throws IOException {
        List<Integer> docIds = new ArrayList<>(blockSize);
        int lastDocId = 0;
        for (int i = 0; i < blockSize; i++) {
            int delta = VarByte.decodeVarInt(indexFile); // Decode delta value
            lastDocId += delta; // Restore original document ID
            docIds.add(lastDocId);  // Add to list of document IDs
        }
        return docIds; // Return the list of decompressed document IDs
    }

    /**
     * Decompresses frequencies from a compressed block.
     * @param indexFile RandomAccessFile to read from.
     * @param blockSize Size of the block to decompress.
     * @return List of decompressed frequencies.
     * @throws IOException If an I/O error occurs.
     */
    public static List<Integer> decompressTermFreqs(RandomAccessFile indexFile, int blockSize) throws IOException {
        List<Integer> freqs = new ArrayList<>(blockSize);
        for (int i = 0; i < blockSize; i++) {
            int freq = VarByte.decodeVarInt(indexFile); // Decode frequency value
            freqs.add(freq); // Add to list of frequencies
        }
        return freqs; // Return the list of decompressed frequencies
    }
}
