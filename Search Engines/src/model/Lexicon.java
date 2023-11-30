package model;
import java.io.Serial;
import java.io.Serializable;
public class Lexicon implements Serializable  {
    @Serial
    private static final long serialVersionUID = 1L;
    private long startOffset;
    private long endOffset;
    private int blockCount;
    private int documentFrequency;
    private long metadataPosition;

    public Lexicon(long startOffset, long endOffset, int blockCount, int documentFrequency, long metadataPosition) {
        this.startOffset = startOffset;
        this.endOffset = endOffset;
        this.blockCount = blockCount;
        this.documentFrequency = documentFrequency;
        this.metadataPosition = metadataPosition;
    }

    public int getDocumentFrequency() {
        return documentFrequency;
    }

    public long getMetadataPosition() {
        return metadataPosition;
    }

    @Override
    public String toString() {
        return String.format("Start: %d, End: %d, Blocks: %d, DocFreq: %d, MetaOffset: %d",
                startOffset, endOffset, blockCount, documentFrequency, metadataPosition);
    }
}
