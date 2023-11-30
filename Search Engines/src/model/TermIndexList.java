package model;
/**
 * Represents an inverted list for a term in an index, containing metadata
 * about document IDs and frequencies.
 */
public class TermIndexList {
    private String term;
    private Metadata metadata;
    private int currentBlockIndex;
    private int positionInCurrentBlock;

    /**
     * Constructs a TermIndexList with the given term and associated metadata.
     * @param term The term associated with this inverted list.
     * @param metadata The metadata associated with the term in the index.
     */
    public TermIndexList(String term, Metadata metadata) {
        this.term = term;
        this.metadata = metadata;
    }

    public String getTerm() {
        return term;
    }

    public void setTerm(String term) {
        this.term = term;
    }

    public Metadata getMetadata() {
        return metadata;
    }

    public void setMetadata(Metadata metadata) {
        this.metadata = metadata;
    }

    public int getCurrentBlockIndex() {
        return currentBlockIndex;
    }

    public void setCurrentBlockIndex(int currentBlockIndex) {
        this.currentBlockIndex = currentBlockIndex;
    }

    public int getPositionInCurrentBlock() {
        return positionInCurrentBlock;
    }

    public void setPositionInCurrentBlock(int positionInCurrentBlock) {
        this.positionInCurrentBlock = positionInCurrentBlock;
    }

    @Override
    public String toString() {
        return "Term: " + term + ", Metadata: " + metadata;
    }
}
