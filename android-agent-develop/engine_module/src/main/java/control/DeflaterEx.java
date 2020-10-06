package control;

import java.util.zip.Deflater;
import java.util.zip.Inflater;


public class DeflaterEx {
    private long strm;
    private byte[] buf = new byte[0];
    private int off, len;
    private int level, strategy;
    private boolean setParams;
    private boolean finish, finished;

    /**
     * Compression method for the deflate algorithm (the only one currently
     * supported).
     */
    public static final int DEFLATED = 8;

    /**
     * Compression level for no compression.
     */
    public static final int NO_COMPRESSION = 0;

    /**
     * Compression level for fastest compression.
     */
    public static final int BEST_SPEED = 1;

    /**
     * Compression level for best compression.
     */
    public static final int BEST_COMPRESSION = 9;

    /**
     * Default compression level.
     */
    public static final int DEFAULT_COMPRESSION = -1;

    /**
     * Compression strategy best used for data consisting mostly of small
     * values with a somewhat random distribution. Forces more Huffman coding
     * and less string matching.
     */
    public static final int FILTERED = 1;

    /**
     * Compression strategy for Huffman coding only.
     */
    public static final int HUFFMAN_ONLY = 2;

    /**
     * Default compression strategy.
     */
    public static final int DEFAULT_STRATEGY = 0;

    static {
        /* Zip library is loaded from System.initializeSystemClass */
        initIDs();
    }

    /**
     * Creates a new compressor using the specified compression level.
     * If 'nowrap' is true then the ZLIB header and checksum fields will
     * not be used in order to support the compression format used in
     * both GZIP and PKZIP.
     *
     * @param level  the compression level (0-9)
     * @param nowrap if true then use GZIP compatible compression
     */
    public DeflaterEx(int level, boolean nowrap) {
        this.level = level;
        this.strategy = DEFAULT_STRATEGY;
        strm = init(level, DEFAULT_STRATEGY, nowrap);
    }

    public DeflaterEx(int level, boolean nowrap, boolean finish) {
        this.level = level;
        this.strategy = DEFAULT_STRATEGY;
        strm = init(level, DEFAULT_STRATEGY, nowrap);
        this.finish = finish;
    }

    public DeflaterEx(int level, boolean nowrap, boolean finish, boolean compress) {
        this.level = level;
        this.strategy = DEFAULT_STRATEGY;
        strm = init2(level, DEFAULT_STRATEGY, nowrap);
        this.finish = finish;
    }

    /**
     * Creates a new compressor using the specified compression level.
     * Compressed data will be generated in ZLIB format.
     *
     * @param level the compression level (0-9)
     */
    public DeflaterEx(int level) {
        this(level, false);
    }

    /**
     * Creates a new compressor with the default compression level.
     * Compressed data will be generated in ZLIB format.
     */
    public DeflaterEx() {
        this(DEFAULT_COMPRESSION, false);
    }

    /**
     * Sets input data for compression. This should be called whenever
     * needsInput() returns true indicating that more input data is required.
     *
     * @param b   the input data bytes
     * @param off the start offset of the data
     * @param len the length of the data
     * @see Deflater#needsInput
     */
    public synchronized void setInput(byte[] b, int off, int len) {
        if (b == null) {
            throw new NullPointerException();
        }
        if (off < 0 || len < 0 || off > b.length - len) {
            throw new ArrayIndexOutOfBoundsException();
        }
        this.buf = b;
        this.off = off;
        this.len = len;
    }

    /**
     * Sets input data for compression. This should be called whenever
     * needsInput() returns true indicating that more input data is required.
     *
     * @param b the input data bytes
     * @see Deflater#needsInput
     */
    public void setInput(byte[] b) {
        setInput(b, 0, b.length);
    }

    /**
     * Sets preset dictionary for compression. A preset dictionary is used
     * when the history buffer can be predetermined. When the data is later
     * uncompressed with Inflater.inflate(), Inflater.getAdler() can be called
     * in order to get the Adler-32 value of the dictionary required for
     * decompression.
     *
     * @param b   the dictionary data bytes
     * @param off the start offset of the data
     * @param len the length of the data
     * @see Inflater#inflate
     * @see Inflater#getAdler
     */
    public synchronized void setDictionary(byte[] b, int off, int len) {
        if (strm == 0 || b == null) {
            throw new NullPointerException();
        }
        if (off < 0 || len < 0 || off > b.length - len) {
            throw new ArrayIndexOutOfBoundsException();
        }
        setDictionary(strm, b, off, len);
    }

    /**
     * Sets preset dictionary for compression. A preset dictionary is used
     * when the history buffer can be predetermined. When the data is later
     * uncompressed with Inflater.inflate(), Inflater.getAdler() can be called
     * in order to get the Adler-32 value of the dictionary required for
     * decompression.
     *
     * @param b the dictionary data bytes
     * @see Inflater#inflate
     * @see Inflater#getAdler
     */
    public void setDictionary(byte[] b) {
        setDictionary(b, 0, b.length);
    }

    /**
     * Sets the compression strategy to the specified value.
     *
     * @param strategy the new compression strategy
     * @throws IllegalArgumentException if the compression strategy is
     *                                  invalid
     */
    public synchronized void setStrategy(int strategy) {
        switch (strategy) {
            case DEFAULT_STRATEGY:
            case FILTERED:
            case HUFFMAN_ONLY:
                break;
            default:
                throw new IllegalArgumentException();
        }
        if (this.strategy != strategy) {
            this.strategy = strategy;
            setParams = true;
        }
    }

    /**
     * Sets the current compression level to the specified value.
     *
     * @param level the new compression level (0-9)
     * @throws IllegalArgumentException if the compression level is invalid
     */
    public synchronized void setLevel(int level) {
        if ((level < 0 || level > 9) && level != DEFAULT_COMPRESSION) {
            throw new IllegalArgumentException("invalid compression level");
        }
        if (this.level != level) {
            this.level = level;
            setParams = true;
        }
    }

    /**
     * Returns true if the input data buffer is empty and setInput()
     * should be called in order to provide more input.
     *
     * @return true if the input data buffer is empty and setInput()
     * should be called in order to provide more input
     */
    public boolean needsInput() {
        return len <= 0;
    }

    /**
     * When called, indicates that compression should end with the current
     * contents of the input buffer.
     */
    public synchronized void finish() {
        finish = true;
    }

    /**
     * Returns true if the end of the compressed data output stream has
     * been reached.
     *
     * @return true if the end of the compressed data output stream has
     * been reached
     */
    public synchronized boolean finished() {
        return finished;
    }

    /**
     * Fills specified buffer with compressed data. Returns actual number
     * of bytes of compressed data. A return value of 0 indicates that
     * needsInput() should be called in order to determine if more input
     * data is required.
     *
     * @param b   the buffer for the compressed data
     * @param off the start offset of the data
     * @param len the maximum number of bytes of compressed data
     * @return the actual number of bytes of compressed data
     */
    public synchronized int deflate(byte[] b, int off, int len) {
        if (b == null) {
            throw new NullPointerException();
        }
        if (off < 0 || len < 0 || off > b.length - len) {
            throw new ArrayIndexOutOfBoundsException();
        }
        return deflateBytes(b, off, len);
    }

    /**
     * Fills specified buffer with compressed data. Returns actual number
     * of bytes of compressed data. A return value of 0 indicates that
     * needsInput() should be called in order to determine if more input
     * data is required.
     *
     * @param b the buffer for the compressed data
     * @return the actual number of bytes of compressed data
     */
    public int deflate(byte[] b) {
        return deflate(b, 0, b.length);
    }

    /**
     * Returns the ADLER-32 value of the uncompressed data.
     *
     * @return the ADLER-32 value of the uncompressed data
     */
    public synchronized int getAdler() {
        ensureOpen();
        return getAdler(strm);
    }

    /**
     * Returns the total number of uncompressed bytes input so far.
     *
     * <p>Since the number of bytes may be greater than
     * Integer.MAX_VALUE, the {@link #getBytesRead()} method is now
     * the preferred means of obtaining this information.</p>
     *
     * @return the total number of uncompressed bytes input so far
     */
    public int getTotalIn() {
        return (int) getBytesRead();
    }

    /**
     * Returns the total number of uncompressed bytes input so far.</p>
     *
     * @return the total (non-negative) number of uncompressed bytes input so far
     * @since 1.5
     */
    public synchronized long getBytesRead() {
        ensureOpen();
        return getBytesRead(strm);
    }

    /**
     * Returns the total number of compressed bytes output so far.
     *
     * <p>Since the number of bytes may be greater than
     * Integer.MAX_VALUE, the {@link #getBytesWritten()} method is now
     * the preferred means of obtaining this information.</p>
     *
     * @return the total number of compressed bytes output so far
     */
    public int getTotalOut() {
        return (int) getBytesWritten();
    }

    /**
     * Returns the total number of compressed bytes output so far.</p>
     *
     * @return the total (non-negative) number of compressed bytes output so far
     * @since 1.5
     */
    public synchronized long getBytesWritten() {
        ensureOpen();
        return getBytesWritten(strm);
    }

    /**
     * Resets deflater so that a new set of input data can be processed.
     * Keeps current compression level and strategy settings.
     */
    public synchronized void reset() {
        ensureOpen();
        reset(strm);
        finish = false;
        finished = false;
        off = len = 0;
    }

    /**
     * Closes the compressor and discards any unprocessed input.
     * This method should be called when the compressor is no longer
     * being used, but will also be called automatically by the
     * finalize() method. Once this method is called, the behavior
     * of the Deflater object is undefined.
     */
    public synchronized void end() {
        if (strm != 0) {
            end(strm);
            strm = 0;
            buf = null;
        }
    }

    /**
     * Closes the compressor when garbage is collected.
     */
    protected void finalize() {
//    	if (GlobalStatic.isForceGarbageCollection) {
//    		end();
//    	}
    }

    private void ensureOpen() {
        if (strm == 0)
            throw new NullPointerException();
    }

    private native static void initIDs();

    private native static long init(int level, int strategy, boolean nowrap);

    private native static long init2(int level, int strategy, boolean nowrap);

    private native static void setDictionary(long strm, byte[] b, int off, int len);

    private native int deflateBytes(byte[] b, int off, int len);

    private native static int getAdler(long strm);

    private native static long getBytesRead(long strm);

    private native static long getBytesWritten(long strm);

    private native static void reset(long strm);

    private native static void end(long strm);
}
