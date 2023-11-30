package model;

public class DocumentScore {
    int docId;
    double score;
    public DocumentScore(int docId, double score) {
        this.docId = docId;
        this.score = score;
    }

    public double getScore() {
        return score;
    }

    public int getDocId() {
        return docId;
    }

    @Override
    public String toString() {
        return "Document{" +
                "docID=" + docId +
                ", score=" + score +
                '}';
    }
}
