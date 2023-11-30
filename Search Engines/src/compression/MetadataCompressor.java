package compression;

import model.Metadata;

import java.io.*;

/**
 * Utilities for compressing and decompressing metadata associated with blocks of postings.
 */
public class MetadataCompressor {

    /**
     * Compresses metadata and writes it to a file.
     * @param metadataFile File to write the compressed metadata.
     * @param lastDocIds Array of last document IDs for each block.
     * @param docIdBlockStarts Array of start positions for each docID block.
     * @param docIdBlockSizes Array of sizes for each docID block.
     * @param freqBlockStarts Array of start positions for each frequency block.
     * @param freqBlockSizes Array of sizes for each frequency block.
     * @throws IOException If an I/O error occurs.
     */
    public static void compressMetadata(
            RandomAccessFile metadataFile,
            int[] lastDocIds,
            long[] docIdBlockStarts,
            int[] docIdBlockSizes,
            long[] freqBlockStarts,
            int[] freqBlockSizes) throws IOException {

        metadataFile.writeInt(lastDocIds.length); // Writing the number of blocks

        int lastDocId = 0;
        for (int docId : lastDocIds) {
            VarByte.encodeVarInt(metadataFile, docId - lastDocId); // Compressing last docID offsets
            lastDocId = docId;
        }

        long lastStart = 0;
        for (long start : docIdBlockStarts) {
            VarByte.encodeVarLong(metadataFile, start - lastStart); // Compressing docID block start offsets
            lastStart = start;
        }

        for (int size : docIdBlockSizes) {
            VarByte.encodeVarInt(metadataFile, size); // Compressing docID block sizes
        }

        lastStart = 0;
        for (long start : freqBlockStarts) {
            VarByte.encodeVarLong(metadataFile, start - lastStart); // Compressing freq block start offsets
            lastStart = start;
        }

        for (int size : freqBlockSizes) {
            VarByte.encodeVarInt(metadataFile, size); // Compressing freq block sizes
        }
    }

    /**
     * Decompresses metadata from a file.
     * @param metadataFile File to read the compressed metadata from.
     * @return Metadata object containing decompressed metadata.
     * @throws IOException If an I/O error occurs.
     */
    public static Metadata decompressMetadata(RandomAccessFile metadataFile) throws IOException {
        int blocksCount = metadataFile.readInt(); // Reading the number of blocks

        int[] lastDocIds = new int[blocksCount];
        long[] docIdBlockStarts = new long[blocksCount];
        int[] docIdBlockSizes = new int[blocksCount];
        long[] freqBlockStarts = new long[blocksCount];
        int[] freqBlockSizes = new int[blocksCount];

        int lastDocId = 0;
        for (int i = 0; i < blocksCount; i++) {
            lastDocIds[i] = lastDocId += VarByte.decodeVarInt(metadataFile); // Decompressing last docID offsets
        }

        long lastStart = 0;
        for (int i = 0; i < blocksCount; i++) {
            docIdBlockStarts[i] = lastStart += VarByte.decodeVarLong(metadataFile); // Decompressing docID block start offsets
        }

        for (int i = 0; i < blocksCount; i++) {
            docIdBlockSizes[i] = VarByte.decodeVarInt(metadataFile); // Decompressing docID block sizes
        }

        lastStart = 0;
        for (int i = 0; i < blocksCount; i++) {
            freqBlockStarts[i] = lastStart += VarByte.decodeVarLong(metadataFile); // Decompressing freq block start offsets
        }

        for (int i = 0; i < blocksCount; i++) {
            freqBlockSizes[i] = VarByte.decodeVarInt(metadataFile); // Decompressing freq block sizes
        }

        return new Metadata(lastDocIds, docIdBlockStarts, docIdBlockSizes, freqBlockStarts, freqBlockSizes);
    }
}
