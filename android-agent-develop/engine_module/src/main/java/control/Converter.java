package control;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

public class Converter {

    // 1) �⺻ �ڷ��� =>byte[]�� ��ȯ (�򿣵������)
    public static byte[] getBytesFromShort(short n) {
        byte[] b = new byte[2];
        b[0] = (byte) ((n >> 8) & 0xff);
        b[1] = (byte) (n & 0xff);
        return b;
    }

    public static byte[] getBytesFromChar(char n) {
        byte[] b = new byte[2];
        b[0] = (byte) ((n >> 8) & 0xff);
        b[1] = (byte) (n & 0xff);
        return b;
    }

    public static byte[] getBytesFromInt(int n) {
        byte[] b = new byte[4];
        b[0] = (byte) ((n >> 24) & 0xff);
        b[1] = (byte) ((n >> 16) & 0xff);
        b[2] = (byte) ((n >> 8) & 0xff);
        b[3] = (byte) (n & 0xff);
        return b;
    }

    public static byte[] getBytesFromLong(long n) {
        byte[] b = new byte[8];
        b[0] = (byte) ((n >> 56) & 0xff);
        b[1] = (byte) ((n >> 48) & 0xff);
        b[2] = (byte) ((n >> 40) & 0xff);
        b[3] = (byte) ((n >> 32) & 0xff);
        b[4] = (byte) ((n >> 24) & 0xff);
        b[5] = (byte) ((n >> 16) & 0xff);
        b[6] = (byte) ((n >> 8) & 0xff);
        b[7] = (byte) (n & 0xff);
        return b;
    }

    // 2) �⺻ �ڷ��� =>byte[]�� ��ȯ (��Ʋ����� ��ȯ)
    public static byte[] getBytesFromShortLE(short n) {
        byte[] b = new byte[2];
        b[0] = (byte) n;
        b[1] = (byte) (n >> 8);
        return b;
    }

    public static byte[] getBytesFromCharLE(char n) {
        byte[] b = new byte[2];
        b[0] = (byte) n;
        b[1] = (byte) (n >> 8);
        return b;
    }

    public static byte[] getBytesFromIntLE(int n) {
        byte[] b = new byte[4];
        b[0] = (byte) n;
        b[1] = (byte) (n >> 8);
        b[2] = (byte) (n >> 16);
        b[3] = (byte) (n >> 24);
        return b;
    }

    public static byte[] getBytesFromIntForColor(int n) {
        byte[] b = new byte[4];
        b[0] = (byte) (n >> 16);
        b[1] = (byte) (n >> 8);
        b[2] = (byte) n;
//		b[3] = (byte) (n >> 24);
        return b;
    }

    public static byte[] getBytesFromLongLE(long n) {
        byte[] b = new byte[8];
        b[0] = (byte) n;
        b[1] = (byte) (n >> 8);
        b[2] = (byte) (n >> 16);
        b[3] = (byte) (n >> 24);
        b[4] = (byte) (n >> 32);
        b[5] = (byte) (n >> 40);
        b[6] = (byte) (n >> 48);
        b[7] = (byte) (n >> 56);
        return b;
    }

    public static byte[] getBytesFromFloat(float n) {
        return getBytesFromInt(Float.floatToIntBits(n));
    }

    public static byte[] getBytesFromDouble(double n) {
        return getBytesFromLong(Double.doubleToLongBits(n));
    }

    // 3) byte[]���� �⺻�ڷ������� (�ڷ����� �򿣵��)

    public static short getShortFromBytes(byte[] b, int n) {
        return (short) ((b[n] & 0xff) << 8 | (b[n + 1] & 0xff));
    }

    // byte[]���� short�� ��ȯ�� �����÷ξ ��ܼ� int�� ġȯ��Ŵ (HO_PCARD���� ���)
    public static int getIntFrom2Bytes(byte[] b, int n) {
        return (int) ((b[n] & 0xff) << 8 | (b[n + 1] & 0xff));
    }

    public static char getCharFromBytes(byte[] b, int n) {
        return (char) ((b[n] & 0xff) << 8 | (b[n + 1] & 0xff));
    }

    public static int getIntFromBytes(byte[] b, int n) {
        // return ((b[n]) << 24 & 0xff) | (b[n + 1] & 0xff) << 16 | (b[n + 2] &
        // 0xff) << 8
        return ((b[n]) << 24) | (b[n + 1] & 0xff) << 16
                | (b[n + 2] & 0xff) << 8 | (b[n + 3] & 0xff);
    }

    public static long getLongFromBytes(byte[] b, int n) {
        return (long) (b[n] & 0xff) << 56 |
                /* long cast needed or shift done modulo 32 */
                (long) (b[n + 1] & 0xff) << 48 | (long) (b[n + 2] & 0xff) << 40
                | (long) (b[n + 3] & 0xff) << 32
                | (long) (b[n + 4] & 0xff) << 24
                | (long) (b[n + 5] & 0xff) << 16
                | (long) (b[n + 6] & 0xff) << 8 | (long) (b[n + 7] & 0xff);
    }

    public static float getFloatFromBytes(byte[] b, int n) throws IOException {
        return Float.intBitsToFloat(getIntFromBytes(b, n));
    }

    public final double getDoubleFomBytes(byte[] b, int n) throws IOException {
        return Double.longBitsToDouble(getLongFromBytes(b, n));
    }

    public static short getShortFromBytes(byte[] b) {
        return (short) ((b[0] & 0xff) << 8 | (b[1] & 0xff));
    }

    public static char getCharFromBytes(byte[] b) {
        return (char) ((b[0] & 0xff) << 8 | (b[1] & 0xff));
    }

    public static int getIntFromBytes(byte[] b) {
        return ((b[0]) << 24 & 0xff) | (b[1] & 0xff) << 16 | (b[2] & 0xff) << 8
                | (b[3] & 0xff);
    }

    public static long getLongFromBytes(byte[] b) {
        return (long) (b[0] & 0xff) << 56 |
                /* long cast needed or shift done modulo 32 */
                (long) (b[1] & 0xff) << 48 | (long) (b[2] & 0xff) << 40
                | (long) (b[3] & 0xff) << 32 | (long) (b[4] & 0xff) << 24
                | (long) (b[5] & 0xff) << 16 | (long) (b[6] & 0xff) << 8
                | (long) (b[7] & 0xff);
    }

    public static float getFloatFromBytes(byte[] b) throws IOException {
        return Float.intBitsToFloat(getIntFromBytes(b));
    }

    public final double getDoubleFomBytes(byte[] b) throws IOException {
        return Double.longBitsToDouble(getLongFromBytes(b));
    }

    // ------------------------------------------------------------

    // 4) byte[]���� �⺻�ڷ������� (�ڷ����� ��Ʋ�����)
    // little endian => big endian
    public static short readShortLittleEndian(byte[] b, int n) {
        // 2 bytes
        int low = b[n] & 0xff;
        int high = b[n + 1] & 0xff;
        return (short) (high << 8 | low);
    }

    // 2byte�� ����ؼ� int���� ����
    public static int readIntLittleEndianFrom2B(byte[] b, int n) {
        // 2 bytes
        int low = b[n] & 0xff;
        int high = b[n + 1] & 0xff;
        return (high << 8 | low);
    }

    // 3byte�� ����ؼ� int���� ����
    public static int readIntLittleEndianFrom3B(byte[] b, int n) {
        // 3 bytes
        int accum = 0;
        int i = 0;
        for (int shiftBy = 0; shiftBy < 24; shiftBy += 8, i++) {
            // must cast to long or shift done modulo 32
            accum |= (int) (b[n + i] & 0xff) << shiftBy;
        }
        return accum;
    }

    public static int readIntLittleEndian(byte[] b, int n) {
        // 4 bytes
        int accum = 0;
        int i = 0;
        for (int shiftBy = 0; shiftBy < 32; shiftBy += 8, i++) {
            // must cast to long or shift done modulo 32
            accum |= (int) (b[n + i] & 0xff) << shiftBy;
        }
        return accum;
    }

    public static int readIntLittleEndian2(byte[] b) {
        int len = b.length;
        int accum = 0;
        int i = 0;
        for (int shiftBy = 0; shiftBy < 32; shiftBy += 8, i++) {
            // must cast to long or shift done modulo 32
            if (len <= i) break;
            accum |= (int) (b[i] & 0xff) << shiftBy;
        }
        return accum;
    }

    public static long readLongLittleEndian4B(byte[] b, int n) {
        // 4 bytes
        long accum = 0;
        int i = 0;
        for (int shiftBy = 0; shiftBy < 32; shiftBy += 8, i++) {
            // must cast to long or shift done modulo 32
            accum |= (long) (b[n + i] & 0xff) << shiftBy;
        }
        return accum;
    }

    public static long readLongLittleEndian(byte[] b, int n) {
        // 8 bytes
        long accum = 0;
        int i = 0;
        for (int shiftBy = 0; shiftBy < 64; shiftBy += 8, i++) {
            // must cast to long or shift done modulo 32
            accum |= (long) (b[n + i] & 0xff) << shiftBy;
        }
        return accum;
    }

    public static long readLongLittleEndian2(byte[] b, int n) {
        // 8 bytes
        int len = b.length;
        long accum = 0;
        int i = 0;
        for (int shiftBy = 0; shiftBy < 64; shiftBy += 8, i++) {
            // must cast to long or shift done modulo 32
            if (len <= (n + i)) break;
            accum |= (long) (b[n + i] & 0xff) << shiftBy;
        }
        return accum;
    }

    public static double readDoubleLittleEndian(byte[] b, int n) {
        long accum = 0;
        int i = 0;
        for (int shiftBy = 0; shiftBy < 64; shiftBy += 8, i++) {
            // must cast to long or shift done modulo 32
            accum |= ((long) (b[n + i] & 0xff)) << shiftBy;
        }
        return Double.longBitsToDouble(accum);
    }

    public static float readFloatLittleEndian(byte[] b, int n) {
        int accum = 0;
        int i = 0;
        for (int shiftBy = 0; shiftBy < 32; shiftBy += 8, i++) {
            accum |= (b[n + i] & 0xff) << shiftBy;
        }
        return Float.intBitsToFloat(accum);
    }

    public static short readShortLittleEndian(byte[] b) {
        // 2 bytes
        int low = b[0] & 0xff;
        int high = b[1] & 0xff;
        return (short) (high << 8 | low);
    }

    public static int readIntLittleEndian(byte[] b) {
        // 4 bytes
        int accum = 0;
        int i = 0;
        for (int shiftBy = 0; shiftBy < 32; shiftBy += 8, i++) {
            // must cast to long or shift done modulo 32
            accum |= (int) (b[i] & 0xff) << shiftBy;
        }
        return accum;
    }

    public static int read2BytesToIntLittleEndian(byte[] b) {
        int accum = 0;
        int i = 0;
        accum = (b[0] & 0xff) << 0;
        accum |= (b[1] & 0xff) << 8;
        return accum;
    }

    public static int readByteToIntLittleEndian(byte b) {
        // 4 bytes
        int accum = 0;
        int i = 0;
        accum |= (int) (b & 0xff);
        return accum;
    }

    long readLongLittleEndian(byte[] b) {
        // 8 bytes
        long accum = 0;
        int i = 0;
        for (int shiftBy = 0; shiftBy < 64; shiftBy += 8, i++) {
            // must cast to long or shift done modulo 32
            accum |= (long) (b[i] & 0xff) << shiftBy;
        }
        return accum;
    }

    double readDoubleLittleEndian(byte[] b) {
        long accum = 0;
        int i = 0;
        for (int shiftBy = 0; shiftBy < 64; shiftBy += 8, i++) {
            // must cast to long or shift done modulo 32
            accum |= ((long) (b[i] & 0xff)) << shiftBy;
        }
        return Double.longBitsToDouble(accum);
    }

    float readFloatLittleEndian(byte[] b) {
        int accum = 0;
        int i = 0;
        for (int shiftBy = 0; shiftBy < 32; shiftBy += 8, i++) {
            accum |= (b[i] & 0xff) << shiftBy;
        }
        return Float.intBitsToFloat(accum);
    }

    //---------------------------------------------------------->>
    // unsigned Ÿ��ó��

    // unsigned type handling

    public static int getIntFromUnsignedByte(byte value) {
        return (int) (value & 0xff);
    }

    // c����� unsigned char�� ���� ó��, unsigned char -> short
    public static short toUnsignedChar(byte b) {
        return (short) (b & 0xff);
    }

    public static char getUnsignedChar(byte b) {
        return (char) (b & 0xff);
    }

    // c����� unsigned short�� ���� ó��, unsinged short -> int
    public static int toUnsignedShort(short s) {
        return s & 0xffff;
    }

    // c����� unsigned short�� ���� ó��, unsinged int -> long
    public static long toUnsignedInt(int n) {
        return n & 0xffffffffL;
    }

    // ���ڿ� str�� ������ bArr�迭�� 2byte(vc++�� Unicode����)���� �����Ѵ�.
    public static void convert2ByteArray(String str, byte[] bArr,
                                         boolean bIncludeNullCharacter) {
        char ch;
        int i = 0;
        for (i = 0; i < str.length(); i++) {
            ch = str.charAt(i);
            bArr[2 * i] = (byte) (ch & 0xff);
            bArr[2 * i + 1] = (byte) (ch >> 8 & 0xff);
        }
        //�ι��ڸ� ���Խ�ų��
        if (bIncludeNullCharacter) {
            bArr[2 * i] = '\0';
            bArr[2 * i + 1] = '\0';
        }
    }

    public static int readInteger(byte[] msgInfo, int pos) {
        byte[] dataType = new byte[4];
        System.arraycopy(msgInfo, pos, dataType, 0, 4);
        return readIntLittleEndian(dataType);
    }

    public static String readString(byte[] msgInfo, int pos, int strLen) {
        byte[] dataType = new byte[strLen];
        System.arraycopy(msgInfo, pos, dataType, 0, strLen);

        try {
            return new String(dataType, "EUC-KR");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static byte[] readByte(byte[] msgInfo, int pos, int len) {
        byte[] dataType = new byte[len];
        System.arraycopy(msgInfo, pos, dataType, 0, len);
        return dataType;
    }


    public static final long EPOCH_DIFF = 11644473600000L;

    public static long timeToLong(final int high, final int low) {
        final long filetime = ((long) high) << 32 | (low & 0xffffffffL);
        final long ms_since_16010101 = filetime / (1000 * 10);
        final long ms_since_19700101 = ms_since_16010101 - EPOCH_DIFF;
        return ms_since_19700101;
    }

}
