package model;

public class Posting implements Comparable<Posting>{
    public String term;
    public int docId;
    public int termFreq;

    public Posting(String term, Integer docId, Integer termFreq) {
        this.term = term;
        this.docId = docId;
        this.termFreq = termFreq;
    }

    // DocumentParser - Collections.sort(postings)
    // Sort order: term, docId, termFreq

    /*
     * BEFORE SORTING
     * Posting("apple", 1, 5)
     * Posting("banana", 2, 3)
     * Posting("apple", 1, 2)
     *
     * AFTER SORTING
     * Posting("apple", 1, 2)
     * Posting("apple", 1, 5)
     * Posting("banana", 2, 3)
     * */

    public int compareTo(Posting posting) {
        int comparisonResult = this.term.compareTo(posting.term);
        // If terms are different, return the comparison result
        if (comparisonResult != 0) {
            return comparisonResult;
        }
        // If terms are the same, compare document IDs (avoid integer overflow)
        int docIdComparison = Integer.compare(this.docId, posting.docId);
        if (docIdComparison != 0) {
            return docIdComparison;
        }
        // If document IDs are also the same, compare term frequencies (avoid integer overflow)
        return Integer.compare(this.termFreq, posting.termFreq);
    }

    public String getTerm() {
        return term;
    }

    public int getDocId() {
        return docId;
    }

    public int getTermFreq() {
        return termFreq;
    }

    @Override
    public String toString() {
        return term + " " + docId + " " + termFreq;
    }

}
