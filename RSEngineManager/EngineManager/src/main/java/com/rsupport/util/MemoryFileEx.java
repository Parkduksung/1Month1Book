// code from frameworks/base/core/java/android/os/MemoryFile.java
package com.rsupport.util;

import java.io.FileDescriptor;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.security.InvalidAlgorithmParameterException;

import android.os.Build;
import android.util.Log;

import com.rsupport.util.rslog.MLog;

public class MemoryFileEx
{
    private static String TAG = "MemoryFileEx";

    // mmap(2) protection flags from <sys/mman.h>
    private static final int PROT_READ = 0x1;
    private static final int PROT_WRITE = 0x2;

    private FileDescriptor mFD;        // ashmem file descriptor
    private long mAddress;   // address of ashmem memory
    private int mLength;    // total length of our ashmem region
    private boolean mAllowPurging = false;  // true if our ashmem region is unpinned

    /**
     * Allocates a new ashmem region. The region is initially not purgable.
     *
     * @param name optional name for the file (can be null).
     * @param length of the memory file in bytes.
     * @throws java.io.IOException if the memory file could not be created.
     * @throws java.security.InvalidAlgorithmParameterException
     */
    public MemoryFileEx(String name, int length) throws IOException, InvalidAlgorithmParameterException {
    	this(native_open(name, length), length);
    }
    public MemoryFileEx(int fd, int length) throws IOException, InvalidAlgorithmParameterException {
    	this(getFileDescriptor(fd), length);
    }

    public MemoryFileEx(FileDescriptor fd, int length) throws IOException, InvalidAlgorithmParameterException {
    	if (fd == null)
    		throw new InvalidAlgorithmParameterException("null fd");
    	if (length < 0)
    		length = MemoryFileEx.getSize(fd);
    	mFD = fd;
        mLength = length;
        if (length > 0) {
            mAddress = native_mmap(mFD, length, PROT_READ | PROT_WRITE);
        } else {
            mAddress = 0;
        }
    }

    /**
     * Closes the memory file. If there are no other open references to the memory
     * file, it will be deleted.
     */
    public void close() {
        deactivate();
        if (!isClosed()) {
            native_close(mFD);
        }
    }

    /**
     * Unmaps the memory file from the process's memory space, but does not close it.
     * After this method has been called, read and write operations through this object
     * will fail, but {@link #getFileDescriptor()} will still return a valid file descriptor.
     *
     * @hide
     */
    void deactivate() {
        if (!isDeactivated()) {
            try {
                native_munmap(mAddress, mLength);
                mAddress = 0;
            } catch (IOException ex) {
                Log.e(TAG, ex.toString());
            }
        }
    }

    /**
     * Checks whether the memory file has been deactivated.
     */
    private boolean isDeactivated() {
        return mAddress == 0;
    }

    /**
     * Checks whether the memory file has been closed.
     */
    private boolean isClosed() {
        return !mFD.valid();
    }

    @Override
    protected void finalize() {
        if (!isClosed()) {
            Log.e(TAG, "MemoryFile.finalize() called while ashmem still open");
            close();
        }
    }

    /**
     * Returns the length of the memory file.
     *
     * @return file length.
     */
    public int length() {
        return mLength;
    }

    /**
     * Is memory file purging enabled?
     *
     * @return true if the file may be purged.
     */
    public boolean isPurgingAllowed() {
        return mAllowPurging;
    }

    /**
     * Enables or disables purging of the memory file.
     *
     * @param allowPurging true if the operating system can purge the contents
     * of the file in low memory situations
     * @return previous value of allowPurging
     */
    synchronized public boolean allowPurging(boolean allowPurging) throws IOException {
        boolean oldValue = mAllowPurging;
        if (oldValue != allowPurging) {
            native_pin(mFD, !allowPurging);
            mAllowPurging = allowPurging;
        }
        return oldValue;
    }

    /**
     * Creates a new InputStream for reading from the memory file.
     *
     @return InputStream
     */
    public InputStream getInputStream() {
        return new MemoryInputStream();
    }

    /**
     * Creates a new OutputStream for writing to the memory file.
     *
     @return OutputStream
     */
     public OutputStream getOutputStream() {
        return new MemoryOutputStream();
    }

    /**
     * Reads bytes from the memory file.
     * Will throw an IOException if the file has been purged.
     *
     * @param buffer byte array to read bytes into.
     * @param srcOffset offset into the memory file to read from.
     * @param destOffset offset into the byte array buffer to read into.
     * @param count number of bytes to read.
     * @return number of bytes read.
     * @throws java.io.IOException if the memory file has been purged or deactivated.
     */
    public int readBytes(byte[] buffer, int srcOffset, int destOffset, int count)
            throws IOException {
        if (isDeactivated()) {
            throw new IOException("Can't read from deactivated memory file.");
        }
        if (destOffset < 0 || destOffset > buffer.length || count < 0
                || count > buffer.length - destOffset
                || srcOffset < 0 || srcOffset > mLength
                || count > mLength - srcOffset) {
            throw new IndexOutOfBoundsException();
        }
        return native_read(mFD, mAddress, buffer, srcOffset, destOffset, count, mAllowPurging);
    }

    /**
     * Write bytes to the memory file.
     * Will throw an IOException if the file has been purged.
     *
     * @param buffer byte array to write bytes from.
     * @param srcOffset offset into the byte array buffer to write from.
     * @param destOffset offset  into the memory file to write to.
     * @param count number of bytes to write.
     * @throws java.io.IOException if the memory file has been purged or deactivated.
     */
    public void writeBytes(byte[] buffer, int srcOffset, int destOffset, int count)
            throws IOException {
        if (isDeactivated()) {
            throw new IOException("Can't write to deactivated memory file.");
        }
        if (srcOffset < 0 || srcOffset > buffer.length || count < 0
                || count > buffer.length - srcOffset
                || destOffset < 0 || destOffset > mLength
                || count > mLength - destOffset) {
            throw new IndexOutOfBoundsException();
        }
        native_write(mFD, mAddress, buffer, srcOffset, destOffset, count, mAllowPurging);
    }

    /**
     * Gets a FileDescriptor for the memory file.
     *
     * The returned file descriptor is not duplicated.
     *
     * @throws java.io.IOException If the memory file has been closed.
     *
     * @hide
     */
    public FileDescriptor getFileDescriptor() throws IOException {
        return mFD;
    }

    /**
     * Returns the size of the memory file that the file descriptor refers to,
     * or -1 if the file descriptor does not refer to a memory file.
     *
     * @throws java.io.IOException If <code>fd</code> is not a valid file descriptor.
     *
     * @hide
     */
    public static int getSize(FileDescriptor fd) throws IOException {
        return native_get_size(fd);
    }

    private class MemoryInputStream extends InputStream {

        private int mMark = 0;
        private int mOffset = 0;
        private byte[] mSingleByte;

        @Override
        public int available() throws IOException {
            if (mOffset >= mLength) {
                return 0;
            }
            return mLength - mOffset;
        }

        @Override
        public boolean markSupported() {
            return true;
        }

        @Override
        public void mark(int readlimit) {
            mMark = mOffset;
        }

        @Override
        public void reset() throws IOException {
            mOffset = mMark;
        }

        @Override
        public int read() throws IOException {
            if (mSingleByte == null) {
                mSingleByte = new byte[1];
            }
            int result = read(mSingleByte, 0, 1);
            if (result != 1) {
                return -1;
            }
            return mSingleByte[0];
        }

        @Override
        public int read(byte buffer[], int offset, int count) throws IOException {
            if (offset < 0 || count < 0 || offset + count > buffer.length) {
                // readBytes() also does this check, but we need to do it before
                // changing count.
                throw new IndexOutOfBoundsException();
            }
            count = Math.min(count, available());
            if (count < 1) {
                return -1;
            }
            int result = readBytes(buffer, mOffset, offset, count);
            if (result > 0) {
                mOffset += result;
            }
            return result;
        }

        @Override
        public long skip(long n) throws IOException {
            if (mOffset + n > mLength) {
                n = mLength - mOffset;
            }
            mOffset += n;
            return n;
        }
    }

    private class MemoryOutputStream extends OutputStream {

        private int mOffset = 0;
        private byte[] mSingleByte;

        @Override
        public void write(byte buffer[], int offset, int count) throws IOException {
            writeBytes(buffer, offset, mOffset, count);
            mOffset += count;
        }

        @Override
        public void write(int oneByte) throws IOException {
            if (mSingleByte == null) {
                mSingleByte = new byte[1];
            }
            mSingleByte[0] = (byte)oneByte;
            write(mSingleByte, 0, 1);
        }
    }

    public long address() { return mAddress;}
	//------------------------- reflection
	private static Method get(String name, Class<?>[] args) {
		try {
			Method m = android.os.MemoryFile.class.getDeclaredMethod(name, args);
			m.setAccessible(true);
			return m;
		} catch (NoSuchMethodException e) {
            MLog.v(e.toString());
			return null;
		}
	}
	
	private static FileDescriptor getFileDescriptor(int fd) {
		
		try {
			FileDescriptor jfd = new FileDescriptor();
			Field f = FileDescriptor.class.getDeclaredField("descriptor");
			f.setAccessible(true);
			f.setInt(jfd, fd);
			return jfd;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	private static FileDescriptor native_open(String name, int length)
			throws IOException {
		Method m = get("native_open", new Class[] { String.class, int.class }); // fname,length
		try {
			return (FileDescriptor) m.invoke(null, name, length);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	// returns memory address for ashmem region
	private static long native_mmap(FileDescriptor fd, int length, int mode)
			throws IOException {
		Method m = get("native_mmap", new Class[] { FileDescriptor.class, int.class, int.class }); // length,mode
		try {
			if (Build.VERSION.SDK_INT >= 20)
				return (Long)m.invoke(null, fd, length, mode);
			return (Integer)m.invoke(null, fd, length, mode);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return -1;
	}

	private static void native_munmap(long addr, int length) throws IOException {
		
		try {
			if (Build.VERSION.SDK_INT >= 20) {
				get("native_munmap", new Class[] { long.class, int.class }).invoke(null, addr, length);
			}
			else {
				get("native_munmap", new Class[] { int.class, int.class }).invoke(null, (int)addr, length);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void native_close(FileDescriptor fd) {
		Method m = get("native_close", new Class[] { FileDescriptor.class });
		try {
			m.invoke(null, fd);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void native_pin(FileDescriptor fd, boolean pin)
			throws IOException {
		Method m = get("native_pin", new Class[] { FileDescriptor.class,
				boolean.class });
		try {
			m.invoke(null, fd, pin);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static int native_get_size(FileDescriptor fd) throws IOException {
		Method m = get("native_get_size", new Class[] { FileDescriptor.class });
		try {
			return (Integer) m.invoke(null, fd);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return -1;
	}
	
	private static native int native_read(FileDescriptor fd, long address, byte[] buffer,
            int srcOffset, int destOffset, int count, boolean isUnpinned) throws IOException;
    private static native void native_write(FileDescriptor fd, long address, byte[] buffer,
            int srcOffset, int destOffset, int count, boolean isUnpinned) throws IOException;
    
	/*
	final private static Method _native_read, _native_write;
	static {
		_native_read = get("native_read", new Class[] { FileDescriptor.class,
				int.class, byte[].class, int.class, int.class, int.class,
				boolean.class });
		_native_write = get("native_write", new Class[] {
				FileDescriptor.class, int.class, byte[].class, int.class,
				int.class, int.class, boolean.class });
	}

	private static int native_read(FileDescriptor fd, long address,
			byte[] buffer, int srcOffset, int destOffset, int count,
			boolean isUnpinned) throws IOException {
		try {
			return (Integer) _native_read.invoke(null, fd, address, buffer,
					srcOffset, destOffset, count, isUnpinned);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return -1;
	}

	private static void native_write(FileDescriptor fd, long address,
			byte[] buffer, int srcOffset, int destOffset, int count,
			boolean isUnpinned) throws IOException {
		try {
			_native_write.invoke(null, fd, address, buffer, srcOffset,
					destOffset, count, isUnpinned);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	*/

//  // fill range [from, to) with val 
//	public void fill(int from, int to, byte val) throws IOException {
//      if (isDeactivated()) {
//          throw new IOException("Can't write to deactivated memory file.");
//      }
//      if ( !(0 <= from && from < to && to <= mLength) )
//          throw new IndexOutOfBoundsException();
//		native_fill(mFD, mAddress, from, to, val);
//	}
//  private static native void native_fill(FileDescriptor fd, int address,
//  		int from, int to, byte val) throws IOException;

}
