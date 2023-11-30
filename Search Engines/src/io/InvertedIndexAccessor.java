package io;
import compression.MetadataCompressor;
import model.Document;
import model.TermIndexList;
import model.Lexicon;
import model.Metadata;
import compression.PostingBlockCompressor;

import java.io.*;
import java.util.*;

public class InvertedIndexAccessor {

    // Opens a term index list for a given term
    public static TermIndexList openList(String term, Map<String, Lexicon> lexiconMap, RandomAccessFile metadataFile) throws IOException {
        Lexicon lexicon = lexiconMap.get(term);
        metadataFile.seek(lexicon.getMetadataPosition());
        Metadata metadata = MetadataCompressor.decompressMetadata(metadataFile);
        return new TermIndexList(term, metadata);
    }


    // Finds the next document with docId >= targetDocID in the posting list
    public static int nextGEQ(int targetDocID, TermIndexList termList, RandomAccessFile indexFile) throws IOException {
        Metadata metadata = termList.getMetadata();
        int[] lastDocIds = metadata.getLastDocIds();

        int blockIndex = 0, indexInBlock = 0;
        while (lastDocIds[blockIndex] < targetDocID) {
            blockIndex++;
            if (blockIndex >= lastDocIds.length) {
                return -1; // Indicates no such element exists
            }
        }

        indexFile.seek(metadata.getDocIdBlockStarts()[blockIndex]);
        List<Integer> docIdBlock = PostingBlockCompressor.decompressDocIds(indexFile, metadata.getDocIdBlockSizes()[blockIndex]);

        while (docIdBlock.get(indexInBlock) < targetDocID) {
            indexInBlock++;
        }
        termList.setCurrentBlockIndex(blockIndex);
        termList.setPositionInCurrentBlock(indexInBlock);
        return docIdBlock.get(indexInBlock);
    }

    // Gets the frequency of the current posting in the list
    public static int getFreq(TermIndexList termList, RandomAccessFile indexFile) throws IOException {
        Metadata metadata = termList.getMetadata();

        indexFile.seek(metadata.getTermFreqBlockStarts()[termList.getCurrentBlockIndex()]);
        List<Integer> freqBlock = PostingBlockCompressor.decompressTermFreqs(indexFile, metadata.getTermFreqBlockSizes()[termList.getCurrentBlockIndex()]);

        return freqBlock.get(termList.getPositionInCurrentBlock());
    }

    // Calculates the impact score of the current posting in the term list
    public static double calculateTermImpactScore(TermIndexList termList, List<Document> pageInfo, RandomAccessFile indexFile, Lexicon lexicon, int totalDocCount, double k1, double b, double averageDocLength, int documentId) throws IOException {
        int termFrequencyInDoc = getFreq(termList, indexFile);
        return getScore(termFrequencyInDoc, pageInfo, lexicon, totalDocCount, k1, b, averageDocLength, documentId);
    }

    // Calculates the impact score given the term frequency in a document
    public static double getScore(int termFreqInDoc, List<Document> pageInfo, Lexicon lexicon, int totalDocCount, double k1, double b, double averageDocLength, int documentId) {
        int docFrequency = lexicon.getDocumentFrequency();
        int docLength = pageInfo.get(documentId).getTermCount();
        double idf = Math.log((totalDocCount - docFrequency + 0.5) / (docFrequency + 0.5));
        double termFreqFactor = ((k1 + 1) * termFreqInDoc) / (k1 * ((1 - b) + b * (docLength / averageDocLength)) + termFreqInDoc);
        return idf * termFreqFactor;
    }


    // Updates a score table with the scores of documents based on their term frequencies
    public static void updateScoreTable(Map<Integer, Double> scoreTable, TermIndexList termList, RandomAccessFile indexFile, List<Document> pageInfo, Lexicon lexicon, int totalDocCount, double k1, double b, double averageDocLength) throws IOException {
        Metadata metadata = termList.getMetadata();
        List<Integer> documentIdBlock, frequencyBlock;

        for (int blockIdx = 0; blockIdx < metadata.getLastDocIds().length; blockIdx++) {
            indexFile.seek(metadata.getDocIdBlockStarts()[blockIdx]);
            documentIdBlock = PostingBlockCompressor.decompressDocIds(indexFile, metadata.getDocIdBlockSizes()[blockIdx]);
            indexFile.seek(metadata.getTermFreqBlockStarts()[blockIdx]);
            frequencyBlock = PostingBlockCompressor.decompressTermFreqs(indexFile, metadata.getTermFreqBlockSizes()[blockIdx]);

            for (int idxInBlock = 0; idxInBlock < documentIdBlock.size(); idxInBlock++) {
                int docId = documentIdBlock.get(idxInBlock);
                int freq = frequencyBlock.get(idxInBlock);
                double score = calculateTermImpactScore(termList, pageInfo, indexFile, lexicon, totalDocCount, k1, b, averageDocLength, docId);
                scoreTable.put(docId, scoreTable.getOrDefault(docId, 0.0) + score);
            }
        }
    }
}
