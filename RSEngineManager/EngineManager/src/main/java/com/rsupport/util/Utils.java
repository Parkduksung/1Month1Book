package com.rsupport.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;

import org.json.JSONObject;

import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.util.Log;

import com.rsupport.common.jni.ETC;
import com.rsupport.util.rslog.MLog;

public class Utils {
	
	public static boolean checkP2P(){
		try {
			// DataChannel : 0
			final int DATA_CHANNEL = 0;
			// 중계서버 접속 여부 판단.
			String acceptJSon = Net10.jniRelayGetChannelInfo(DATA_CHANNEL);
			JSONObject jsonObject = new JSONObject(acceptJSon);
			String flag = (String)jsonObject.get("flag");
			// 중계서버 접속 정보를 요청하였으나 error 이므로 p2p 접속
			return "error".equals(flag);
		} catch (Exception e) {
			MLog.e(Log.getStackTraceString(e));
		}
		return true;
	}

	public static boolean rgbaToJpg(String imgPath) {
		final int HEADER_SIZE = 4*4;
		FileInputStream fis = null;
		try {
			File tmpFile = new File(imgPath);
			if(tmpFile.exists() == false){
				MLog.e("not found file");
				return false;
			}
			
			long fileSize = tmpFile.length();
			
			byte[] buffer = new byte[(int)fileSize];
			
			fis = new FileInputStream(tmpFile);
			
			int readSize = fis.read(buffer);
			
			int offset = 0;
			
			int width = Converter.byte4Toint(buffer, offset);
			offset += 4;
			
			int height = Converter.byte4Toint(buffer, offset);
			offset += 4;
			
			int bytePerLine = Converter.byte4Toint(buffer, offset);
			offset += 4;
			
			int colorFormat = Converter.byte4Toint(buffer, offset);
			offset += 4;
			
			int stride = bytePerLine/4;

			MLog.i("width.%d, height.%d, bytePerLine.%d, stride.%d, colorFormat.%d", width, height, bytePerLine, stride, colorFormat);
			
			File fileCacheItem = new File(imgPath);
			fileCacheItem.delete();
			fileCacheItem.createNewFile();
			FileOutputStream fileOutputStream = new FileOutputStream(fileCacheItem);
			
			try{
				return ARGB2JPG(buffer, HEADER_SIZE, readSize-HEADER_SIZE, width, height, stride, fileOutputStream);
			}finally{
				if(fileOutputStream != null){
					fileOutputStream.close();
					fileOutputStream = null;
				}
			}
		} catch (Exception e) {
			MLog.e(Log.getStackTraceString(e));
		} finally{
			if(fis != null){
				try {
					fis.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
				fis = null;
			}
		}
		return false;
	}
	
	public static boolean ARGB2JPG(byte[] argbBuffer, int offset, int argbLength, int width, int height, int stride, OutputStream outputStream){
		Bitmap bitmap = Bitmap.createBitmap(width, height, Config.ARGB_8888);
		int[] colors = new int[stride*height];

		int i = 0; 
		int size = offset + argbLength;
		for(i = offset; i < size; i+= 4){
			colors[(i-offset) != 0?((i-offset)>>2):0] = (argbBuffer[i+3]<<24 & 0xFF000000 |
					argbBuffer[i]<<16 & 0x00FF0000 |
					argbBuffer[i+1]<<8 & 0x0000FF00 |
					argbBuffer[i+2] & 0x000000FF);
		}
		bitmap.setPixels(colors, 0, stride, 0, 0, width, height);
		try{
			return bitmap.compress(CompressFormat.JPEG, 100, outputStream);
		}catch (Exception e){
			e.printStackTrace();
		}
		finally{
			if(bitmap != null){
				bitmap.recycle();
			}
		}
		return false;
	}

	public static boolean makeJPEG(byte[] byteArray, int offset, int argbLength, int width, int height, int stride, OutputStream outputStream){
		Bitmap bitmap = Bitmap.createBitmap(width, height, Config.ARGB_8888);
		int[] colors = new int[stride*height];

		int i = 0;
		int size = offset + argbLength;
		for(i = offset; i < size; i+= 4){
			colors[(i-offset) != 0?((i-offset)>>2):0] = (byteArray[i]<<24 & 0xFF000000 |
					byteArray[i+1]<<16 & 0x00FF0000 |
					byteArray[i+2]<<8 & 0x0000FF00 |
					byteArray[i+3] & 0x000000FF);
		}

		bitmap.setPixels(colors, 0, stride, 0, 0, width, height);

		try{
			return bitmap.compress(CompressFormat.JPEG, 100, outputStream);
		}catch (Exception e){
			e.printStackTrace();
		}
		finally{
			if(bitmap != null){
				bitmap.recycle();
			}
		}
		return false;
	}

	public static ByteBuffer cloneByteBuffer(ByteBuffer original) {
		// Create clone with same capacity as original.
		ByteBuffer clone = (original.isDirect()) ? ByteBuffer.allocateDirect(original.capacity()) : ByteBuffer.allocate(original.capacity());

		// Create a read-only copy of the original.
		// This allows reading from the original without modifying it.
		ByteBuffer readOnlyCopy = original.asReadOnlyBuffer();

		// Flip and read from the original.
		readOnlyCopy.flip();
		clone.put(readOnlyCopy);
		return clone;
	}


    public static boolean check64bit() {

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

                if(Build.SUPPORTED_64_BIT_ABIS.length > 0){
                    return true;
                }
        }

        return false;
    }

	public static boolean isNeonCPU(){
//		String cpuInfo = readCPUinfo();
//		if(cpuInfo != null && cpuInfo.toLowerCase().contains("neon") == true){
//			return true;
//		}
//		return false;
		int isNeon = new ETC().isNeonCPU();
		MLog.d("isNeonCPU() : " + isNeon);
		return isNeon == ETC.CPU_NEON;
	}

	private static String readCPUinfo(){
		ProcessBuilder cmd;
		String result="";

		try{
			String[] args = {"/system/bin/cat", "/proc/cpuinfo"};
			cmd = new ProcessBuilder(args);

			Process process = cmd.start();
			InputStream in = process.getInputStream();
			byte[] re = new byte[1024];
			while(in.read(re) != -1){
				result = result + new String(re, Charset.defaultCharset());
			}
			in.close();
		} catch(IOException ex){
			ex.printStackTrace();
		}
		return result;
	}
	
	public static void saveToRow(ByteBuffer imageBuffer, int width, int height, int pixelStride, int rowStride, int rowPadding, String fileName){
		try {
			
			File file = new File(fileName);
			if(file.exists() == true){
				file.delete();
			}
			String folder = fileName.substring(0, fileName.lastIndexOf(File.separator));
			if(new File(folder).exists() == false){
				new File(folder).mkdirs();
			}
			
			FileOutputStream fos = null;
			try {
				if(file.createNewFile() == true){
					ByteBuffer buffer = ByteBuffer.allocate(4 * 5 + imageBuffer.capacity());
					buffer.putInt(width);
					buffer.putInt(height);
					buffer.putInt(pixelStride);
					buffer.putInt(rowStride);
					buffer.putInt(rowPadding);
					buffer.put(imageBuffer);
					buffer.flip();
					fos = new FileOutputStream(fileName);
					FileChannel c = fos.getChannel();
					c.write(buffer);
				}
			} catch (Exception e) {
			} finally{
				if(fos != null){
					try {
						fos.close();
						fos = null;
					} catch (Exception e2) {
					}
				}
			}
		} catch (Exception e) {
			MLog.e(Log.getStackTraceString(e));
		}
	}

	public static void ARGBtoJPEFile(ByteBuffer argbBuffer, int i, int length,
			int width, int height, int stride, String fileName) {
		argbBuffer.position(0);
		byte[] buffer = new byte[argbBuffer.capacity()];
		argbBuffer.get(buffer);
		argbBuffer.position(0);
		ARGBtoJPEFile(buffer, 0, length, width, height, stride, fileName);
	}
	
	public static void ARGBtoJPEFile(byte[] argbBuffer, int i, int length,
			int width, int height, int stride, String fileName) {
		File file = new File(fileName);
		if(file.exists() == true){
			file.delete();
		}
		String folder = fileName.substring(0, fileName.lastIndexOf(File.separator));
		if(new File(folder).exists() == false){
			new File(folder).mkdirs();
		}
		
		FileOutputStream fos = null;
		try {
			if(file.createNewFile() == true){
				fos = new FileOutputStream(file);
				ARGB2JPG(argbBuffer, i, length, width, height, stride, fos);
			}
		} catch (Exception e) {
		} finally{
			if(fos != null){
				try {
					fos.close();
					fos = null;
				} catch (Exception e2) {
				}
			}
		}
	}

	public static boolean makeJPEGFile(byte[] byteArray, int i, int length,
									 int width, int height, int stride, String fileName) {
		boolean isFileMaked = false;

		FileOutputStream fos = null;
		File file = new File(fileName);

		if(file.exists() == true){
			file.delete();
		}

		String folder = fileName.substring(0, fileName.lastIndexOf(File.separator));
		if(new File(folder).exists() == false){
			new File(folder).mkdirs();
		}

		try {
			if(file.createNewFile() == true){
				fos = new FileOutputStream(file);
				isFileMaked = makeJPEG(byteArray, i, length, width, height, stride, fos);
			}
		} catch (Exception e) {
			isFileMaked = false;
		} finally{
			if(fos != null){
				try {
					fos.close();
					fos = null;
				} catch (Exception e2) {
					isFileMaked = false;
				}
			}
		}

		return isFileMaked;
	}

	public static int getPid(String procName, String[] filter) {
		if (procName == null){
            return -1;
        }
		try{
			String[] args = {"/system/bin/ps"};
			ProcessBuilder cmd = new ProcessBuilder(args);

			Process process = cmd.start();
			InputStream in = process.getInputStream();
			BufferedReader bin = new BufferedReader(new InputStreamReader(in, Charset.defaultCharset()));
			boolean filterContinue;

			for (String l = bin.readLine(); l != null; l = bin.readLine())
			{
				if(filter != null){
					filterContinue = false;
					for(String filterName : filter){
						if(l.contains(filterName) == true){
							filterContinue = true;
							break;
						}
					}
					if(filterContinue == true) continue;
				}

				if (l.contains(procName)) {
					String tokens[] = l.split("[ ]+");
					if (tokens.length >= 2)
						return Integer.parseInt(tokens[1]);
				}
			}
			in.close();
		} catch(IOException ex){
			ex.printStackTrace();
		}
		return -1;
	}
}
