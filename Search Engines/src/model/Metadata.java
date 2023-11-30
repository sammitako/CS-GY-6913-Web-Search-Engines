package model;

public class Metadata {
    public int[] lastDocIds;
    public long[] docIdBlockStarts;
    public int[] docIdBlockSizes;
    public long[] termFreqBlockStarts;
    public int[] termFreqBlockSizes;

    public Metadata(int[] lastDocIds, long[] docIdBlockStarts, int[] docIdBlockSizes, long[] termFreqBlockStarts, int[] termFreqBlockSizes) {
        this.lastDocIds = lastDocIds;
        this.docIdBlockStarts = docIdBlockStarts;
        this.docIdBlockSizes = docIdBlockSizes;
        this.termFreqBlockStarts = termFreqBlockStarts;
        this.termFreqBlockSizes = termFreqBlockSizes;
    }

    public int[] getLastDocIds() {
        return lastDocIds;
    }

    public long[] getDocIdBlockStarts() {
        return docIdBlockStarts;
    }

    public int[] getDocIdBlockSizes() {
        return docIdBlockSizes;
    }

    public long[] getTermFreqBlockStarts() {
        return termFreqBlockStarts;
    }

    public int[] getTermFreqBlockSizes() {
        return termFreqBlockSizes;
    }
}