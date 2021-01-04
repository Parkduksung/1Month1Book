package com.rsupport.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;

import android.content.Context;

import com.rsupport.util.rslog.MLog;

public class FileUtil {

	public static String getUniqueFileName(String orgFileName){
		String resultFileName = orgFileName;

		File orgFile = new File(resultFileName);
		if(orgFile.exists() == true){
			int nSearchCount=0;
			int lastDotIndex = orgFileName.lastIndexOf('.');
			String title = resultFileName;
			String ext = "";
			if(lastDotIndex > 0){
				title = resultFileName.substring(0, lastDotIndex);
				ext = resultFileName.substring(lastDotIndex);
			}

			do {
				resultFileName = String.format(Locale.ENGLISH, "%s(%d)%s", title, ++nSearchCount, ext);
				File checkFile = new File(resultFileName);
				if(checkFile.exists() == false){
					break;
				}
			}while(true);
		}
		return resultFileName;
	}

	public static String getSoFile(Context cxt, String srcPath, String dstFile) throws Exception {
		File tmpDir = cxt.getDir("tmp", Context.MODE_PRIVATE);
		File outFile = new File(tmpDir,dstFile);
		FileOutputStream out = new FileOutputStream(outFile);
		InputStream input = new FileInputStream(srcPath);
		
		byte[] buffer = new byte[4096];
		while (true) {
			int n = input.read(buffer);
			if (n == -1) break;
			out.write(buffer, 0, n);
		}
		input.close();
		out.close();
		MLog.i("copy [%s] -> [%s]", srcPath, outFile.getAbsolutePath());
		outFile.setExecutable(true);
		return outFile.getAbsolutePath();
	}

	public static boolean createNewFile(String fileName){
		File file = new File(fileName);
		if(file.exists() == true){
			file.delete();
		}
		String folder = fileName.substring(0, fileName.lastIndexOf(File.separator));
		if(new File(folder).exists() == false){
			if(new File(folder).mkdirs() == false){
				return false;
			}
		}
		try {
			return file.createNewFile();
		} catch (IOException e) {
			return false;
		}
	}
}
