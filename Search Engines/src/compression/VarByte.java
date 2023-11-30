package compression;
import java.io.*;

public class VarByte {
    /**
     * Decodes a variable-length integer from a DataInputStream.
     */
    public static int decodeVarInt(DataInputStream dis) throws IOException {
        int result = 0, shift = 0;
        byte b;
        do {
            b = dis.readByte();
            result |= (b & 0x7F) << shift;
            shift += 7;
        } while (b < 0);
        return result;
    }

    /**
     * Decodes a variable-length integer from a RandomAccessFile.
     */
    public static int decodeVarInt(RandomAccessFile file) throws IOException {
        int result = 0, shift = 0;
        byte b;
        do {
            b = file.readByte();
            result |= (b & 0x7F) << shift;
            shift += 7;
        } while (b < 0);
        return result;
    }

    /**
     * Decodes a variable-length long from a DataInputStream.
     */
    public static long decodeVarLong(DataInputStream dis) throws IOException {
        long result = 0;
        int shift = 0;
        long b;
        do {
            b = dis.readByte();
            result |= (b & 0x7F) << shift;
            shift += 7;
        } while (b < 0);
        return result;
    }

    /**
     * Decodes a variable-length long from a RandomAccessFile.
     */
    public static long decodeVarLong(RandomAccessFile file) throws IOException {
        long result = 0;
        int shift = 0;
        long b;
        do {
            b = file.readByte();
            result |= (b & 0x7F) << shift;
            shift += 7;
        } while (b < 0);
        return result;
    }

    /**
     * Encodes and writes a variable-length integer to a DataOutputStream.
     */
    public static void encodeVarInt(DataOutputStream dos, int value) throws IOException {
        while ((value & 0xFFFFFF80) != 0L) {
            dos.writeByte((value & 0x7F) | 0x80);
            value >>>= 7;
        }
        dos.writeByte(value & 0x7F);
    }

    /**
     * Encodes and writes a variable-length integer to a RandomAccessFile.
     */
    public static void encodeVarInt(RandomAccessFile file, int value) throws IOException {
        while ((value & 0xFFFFFF80) != 0L) {
            file.writeByte((value & 0x7F) | 0x80);
            value >>>= 7;
        }
        file.writeByte(value & 0x7F);
    }

    /**
     * Encodes and writes a variable-length long to a DataOutputStream.
     */
    public static void encodeVarLong(DataOutputStream dos, long value) throws IOException {
        while ((value & 0xFFFFFFFFFFFFFF80L) != 0L) {
            dos.writeByte((int) (value & 0x7F) | 0x80);
            value >>>= 7;
        }
        dos.writeByte((int) value & 0x7F);
    }

    /**
     * Encodes and writes a variable-length long to a RandomAccessFile.
     */
    public static void encodeVarLong(RandomAccessFile file, long value) throws IOException {
        while ((value & 0xFFFFFFFFFFFFFF80L) != 0L) {
            file.writeByte((int) (value & 0x7F) | 0x80);
            value >>>= 7;
        }
        file.writeByte((int) value & 0x7F);
    }
}
