import io.DocumentSerializer;
import model.*;
import io.InvertedIndexAccessor;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.*;


public class QueryProcessor {

    // Method for processing 'AND' type queries
    public static PriorityQueue<DocumentScore> processAndQuery(String query, Map<String, Lexicon> lexiconMap,
                                                               RandomAccessFile metadataFile, RandomAccessFile indexFile,
                                                               List<Document> documents, int totalDocuments,
                                                               double averageDocumentLength, double k1, double b, int resultSize) throws IOException {
        // A priority queue to hold the top results with a comparator that orders by score.
        PriorityQueue<DocumentScore> topResults = new PriorityQueue<>(Comparator.comparingDouble(DocumentScore::getScore));

        String[] terms = query.split(" ");
        int termLength = terms.length;

        // A list to hold the TermIndexList for each term in the query
        List<TermIndexList> lists = new ArrayList<>();
        for (String term : terms) {
            // If the term is not in the lexicon, skip processing this term
            if (!lexiconMap.containsKey(term)) {
                System.out.println("Term '" + term + "' not found in lexicon.");
                return topResults;
            }
            // Add the list corresponding to the term to the 'lists'
            lists.add(InvertedIndexAccessor.openList(term, lexiconMap, metadataFile));
        }
        // Sort the lists by their document frequency to optimize the merging process
        lists.sort(Comparator.comparingInt(a -> lexiconMap.get(a.getTerm()).getDocumentFrequency()));

        // Determine the maximum document ID to bound the search
        int maxDocID = lists.get(0).getMetadata().lastDocIds[lists.get(0).getMetadata().lastDocIds.length-1];
        int did = 0, d = 0;
        // Iterate through the documents
        while (did < maxDocID) {
            // Find the next document that contains the first term
            did = InvertedIndexAccessor.nextGEQ(did, lists.get(0), indexFile);
            // Check if the document contains all other terms
            for (int i = 1; i < termLength && (d = InvertedIndexAccessor.nextGEQ(did, lists.get(i), indexFile)) == did; i++);

            if (d > did) {
                did = d;
            } else if (d == -1) {
                // Exit if there are no more documents to process
                break;
            } else {
                // Calculate the total score for this document
                double score = 0d;
                for (int i = 0; i < termLength; i++) {
                    score += InvertedIndexAccessor.calculateTermImpactScore(lists.get(i), documents, indexFile, lexiconMap.get(lists.get(i).getTerm()), totalDocuments, k1, b, averageDocumentLength, did);
                }
                // Add the document to the top results if it is among the top scores
                if (topResults.size() < resultSize) {
                    topResults.offer(new DocumentScore(did, score));
                } else if (score > Objects.requireNonNull(topResults.peek()).getScore()) {
                    topResults.poll(); // Remove the lowest scoring document
                    topResults.offer(new DocumentScore(did, score)); // Add the new document
                }
                did++;
            }
        }
        return topResults;
    }

    // Method for processing 'OR' type queries
    public static PriorityQueue<DocumentScore> processOrQuery(String query, Map<String, Lexicon> lexiconMap,
                                                              RandomAccessFile metadataFile, RandomAccessFile indexFile,
                                                              List<Document> documents, int totalDocuments,
                                                              double averageDocumentLength, double k1, double b, int resultSize) throws IOException {
        PriorityQueue<DocumentScore> topResults = new PriorityQueue<>(Comparator.comparingDouble(DocumentScore::getScore));
        String[] terms = query.split(" ");
        int termLength = terms.length;

        List<TermIndexList> lists = new ArrayList<>();
        for (String term : terms) {
            if (!lexiconMap.containsKey(term)) {
                System.out.println("Term '" + term + "' not found in lexicon.");
                return topResults;
            }
            lists.add(InvertedIndexAccessor.openList(term, lexiconMap, metadataFile));
        }
        lists.sort(Comparator.comparingInt(a -> lexiconMap.get(a.getTerm()).getDocumentFrequency()));

        Map<Integer, Double> scoreTable = new HashMap<>();
        for (int i = 0; i < termLength; i++) {
            TermIndexList list = lists.get(i);
            InvertedIndexAccessor.updateScoreTable(scoreTable, list, indexFile, documents, lexiconMap.get(list.getTerm()), totalDocuments, k1, b, averageDocumentLength);
        }


        for (Map.Entry<Integer, Double> entry : scoreTable.entrySet()) {
            int docId = entry.getKey();
            double score = entry.getValue();
            if (topResults.size() < resultSize || score > Objects.requireNonNull(topResults.peek()).getScore()) {
                if (topResults.size() == resultSize) {
                    topResults.poll(); // Remove the lowest scoring document if the queue is full
                }
                topResults.offer(new DocumentScore(docId, score));
            }
        }
        return topResults;
    }
    // Method for processing a single term query
    public static PriorityQueue<DocumentScore> processSingleTermQuery(String term, Map<String, Lexicon> lexiconMap,
                                                                      RandomAccessFile metadataFile, RandomAccessFile indexFile,
                                                                      List<Document> documents, int totalDocuments,
                                                                      double averageDocumentLength, double k1, double b, int resultSize) throws IOException {
        PriorityQueue<DocumentScore> topResults = new PriorityQueue<>(Comparator.comparingDouble(DocumentScore::getScore));

        if (!lexiconMap.containsKey(term)) {
            System.out.println("Term '" + term + "' not found in lexicon.");
            return topResults;
        }

        TermIndexList termList = InvertedIndexAccessor.openList(term, lexiconMap, metadataFile);
        Metadata metadata = termList.getMetadata();
        int maxDocID = metadata.getLastDocIds()[metadata.getLastDocIds().length - 1];

        for (int did = 0; did <= maxDocID; did = InvertedIndexAccessor.nextGEQ(did, termList, indexFile)) {
            if (did == -1) break; // No more documents

            double score = InvertedIndexAccessor.calculateTermImpactScore(termList, documents, indexFile, lexiconMap.get(term), totalDocuments, k1, b, averageDocumentLength, did);

            // Check if the current document's score qualifies it to be in the top results.
            if (topResults.size() < resultSize) {
                topResults.offer(new DocumentScore(did, score));
            } else if (score > Objects.requireNonNull(topResults.peek()).getScore()) {
                topResults.poll(); // Remove the lowest scoring document
                topResults.offer(new DocumentScore(did, score)); // Add the new high scoring document
            }
        }

        return topResults;
    }
}
