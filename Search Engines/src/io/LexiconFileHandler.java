package io;

import model.Lexicon;

import java.io.*;
import java.util.Map;
public class LexiconFileHandler {

    /**
     * Writes a lexicon map to a file.
     * @param lexicon Map to write.
     * @param filePath Path of the file.
     */
    public static void writeLexicon(Map<String, Lexicon> lexicon, String filePath) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filePath))) {
            oos.writeObject(lexicon);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Reads a lexicon map from a file.
     * @param filePath Path of the file to read from.
     * @return The read lexicon map.
     */
    public static Map<String, Lexicon> readLexicon(String filePath) {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(filePath))) {
            return (Map<String, Lexicon>) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }
}