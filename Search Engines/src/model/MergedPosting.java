package model;

public class MergedPosting implements Comparable<MergedPosting> {
    private Posting posting;
    private int fileIndex;

    public MergedPosting(String line, int fileIndex) {
        this.posting = parsePostingLine(line);
        this.fileIndex = fileIndex;
    }

    public int getFileIndex() {
        return fileIndex;
    }

    private Posting parsePostingLine(String line) {
        String[] components = line.split(" ");
        return new Posting(components[0], Integer.parseInt(components[1]), Integer.parseInt(components[2]));
    }

    @Override
    public int compareTo(MergedPosting other) {
        return this.posting.compareTo(other.posting);
    }

    @Override
    public String toString() {
        return posting.toString();
    }
}