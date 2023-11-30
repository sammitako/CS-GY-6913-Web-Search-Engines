package io;


import model.Document;
import java.io.*;
import java.util.List;

public class DocumentSerializer {
    /**
     * Saves a list of Document objects to a file.
     * @param documentList The list of Document objects to be saved.
     * @param filename The name of the file to save to.
     */
    public static void serializeDocumentList(List<Document> documentList, String filename) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filename))) {
            oos.writeObject(documentList);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Loads a list of Document objects from a file.
     * @param filename The name of the file to load from.
     * @return The list of Document objects.
     */
    @SuppressWarnings("unchecked")
    public static List<Document> deserializeDocumentList(String filename) {
        List<Document> documentList = null;
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(filename))) {
            documentList = (List<Document>) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return documentList;
    }
}
