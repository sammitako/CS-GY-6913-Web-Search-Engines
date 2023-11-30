package model;

import java.io.Serial;
import java.io.Serializable;

public class Document implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    public int docId;
    public String url;
    public int termCount;

    public Document(int docId, String url, int termCount) {
        this.docId = docId;
        this.url = url;
        this.termCount = termCount;
    }

    public int getTermCount() {
        return termCount;
    }

    @Override
    public String toString() {
        return "Document Id: " + docId + ", URL: " + url + ", Term Count: " + termCount;
    }
}