package com.rsupport.util;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.Binder;
import android.os.Build;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

/**
 * Created by objects on 5/8/15.
 */
public class Config {
	
    public static final boolean SKT = false;
    public final static boolean DBG = false;
    
//    real x509 hashcode
//    HASHCODE_RSUPPORT = "134d9ae1a341d02583804ffb935a8602fed6f51f549cbbbae63d7ea0718d14bf6ab5b3dd34fb3b47e80379d83c8197b519a8e4ad96fc4dd2253a336eab";
//    HASHCODE_NTT      = "172191bdb8b618d23fca39655a919dc3229ceefa597dacff78845b4e6de1c1c206ae2118bec9037b3e2f2bb675460b05878146f13522854b337f46f1d16d";
//    HASHCODE_NTT_TEST = "db673211a4dcef165e8a1dcb48712d6b725111653f93539c1fd4f49493809f452cfee6c8867f374438485aed87960fb43e791db4c9735bb29e4d7b4317ce";
    
//	private static String[] mAllowedSig = {
//		"9cbbbae63d7ea0718d14bf6ab5b3dd34fb3b47e80379d83c8197b519a8e4ad96fc4dd2253a336eab134d9ae1a341d02583804ffb935a8602fed6f51f54", // RSUP_SIGNITURE
//	};

	private static int CheckedUID = -1;
	static public void clearPkgSignature() {
		CheckedUID = -1;
	}

	/*
	static public boolean checkPkgSignature()
	{
//		if (true) {
//			for (int i=0; i<3; ++i) log.w("*** test : skip signature checking(%s) ***", pkgName);
//			return true;
//		}
		int callerUid = Binder.getCallingUid();

		if (callerUid != CheckedUID) {
			PackageManager pm = RsupApplication.context.getPackageManager();
			if (!checkPkgSignature(pm.getNameForUid(callerUid))
				return false;
			CheckedUID = callerUid;
		}
		return true;
	}
	*/

	static public void checkPkgSignatureEx()
	{
//		if (true) {
//			log.e("==== Developer mode ====");
//			return;
//		}
//		if (true) {
//			for (int i=0; i<3; ++i) log.w("*** test : skip signature checking(%s) ***", pkgName);
//			return true;
//		}
//		int callerUid = Binder.getCallingUid();
//		if (callerUid != CheckedUID) {
//			PackageManager pm = RsupApplication.context.getPackageManager();
//			if (!checkPkgSignature(pm.getNameForUid(callerUid)))
//				throw new SecurityException("Invalid signature!");
//			CheckedUID = callerUid;
//		}
	}

	static private boolean checkPkgSignature(String pkgName) {
//		try {
//			int flags = PackageManager.GET_SIGNATURES;
//			//flags |= PackageManager.GET_META_DATA;
//			PackageManager pm = RsupApplication.context.getPackageManager();
//			PackageInfo piRemote = pm.getPackageInfo(pkgName, flags);
//			Signature sigRemote[] = piRemote.signatures;
//
//			boolean isValidSignature = false;
//			for (int i = 0; i < sigRemote.length && !isValidSignature; ++i) {
//				String hash = confuseText(getX509Hashcode(sigRemote[i]));
//				for (String s : mAllowedSig) {
//					if (s.equals(hash)) {
//						return true;
//					}
//				}
//			}
//
//		} catch (Throwable e) {
//			log.e("sig check: " + e.toString());
//		}
//		return false;
		return true;
	}

	static void fixAllowedSignature(String pkgName) {
//		if (SKT) return ;
//
//		if (pkgName.contains(".rsperm.ntt")) {
//			if (Build.MANUFACTURER.toLowerCase().contains("sony")) {
//				if (!Build.BRAND.equals("docomo")) {
//					throw new SecurityException("Only Docomo!");
//				}
//			}
//			mAllowedSig = new String[]{
//				"93539c1fd4f49493809f452cfee6c8867f374438485aed87960fb43e791db4c9735bb29e4d7b4317cedb673211a4dcef165e8a1dcb48712d6b725111653f", // RSUP_FOR_NTT (ntt test signiture)
//				"7dacff78845b4e6de1c1c206ae2118bec9037b3e2f2bb675460b05878146f13522854b337f46f1d16d172191bdb8b618d23fca39655a919dc3229ceefa59", // NTT_SIGNITURE
//			};
//		} else if (pkgName.contains(".rsperm.ba") || //sony
//				   pkgName.contains(".rsperm.bn") || //fujitsu
//				   pkgName.contains(".rsperm.bm") || //sharp
//				   pkgName.contains(".rsperm.bo") || //panasonic
//				   pkgName.contains(".rsperm.bp") || //nec
//				   pkgName.contains(".rsperm.bq")) { //toshiba
//
//			mAllowedSig = new String[]{
//				"9cbbbae63d7ea0718d14bf6ab5b3dd34fb3b47e80379d83c8197b519a8e4ad96fc4dd2253a336eab134d9ae1a341d02583804ffb935a8602fed6f51f54",  // RSUP_SIGNITURE, 2014.11.04, In order to use both docomo-mobizen and rsupport-mobizen
//				"7dacff78845b4e6de1c1c206ae2118bec9037b3e2f2bb675460b05878146f13522854b337f46f1d16d172191bdb8b618d23fca39655a919dc3229ceefa59",// NTT_SIGNITURE
//			};
//		}
	}
	
//	 private static String getX509Hashcode(Signature signature) throws CertificateException, NoSuchAlgorithmException {
//	    	InputStream iStream = null;
//	    	try {
//				iStream = new ByteArrayInputStream(signature.toByteArray());
//			    X509Certificate certificate = (X509Certificate) CertificateFactory.getInstance("X509").generateCertificate(iStream);
//			    String hexString = bytesToHexString(MessageDigest.getInstance("SHA-512").digest(certificate.getEncoded()));
//			    return hexString;
//
//	    	} finally {
//				if (iStream != null) {
//					try {iStream.close();} catch (IOException e) {}
//					iStream = null;
//				}
//			}
//		}
//
//	 private static String bytesToHexString(byte[] data) {
//		 StringBuilder sb = new StringBuilder();
//		 for (byte n : data) {
//			 sb.append(Integer.toHexString(n & 0xff));
//		 }
//		 return sb.toString();
//	 }
//
//	 private static String confuseText(String text) {
//		 if (text == null || text.length() < 94) {
//			 throw new RuntimeException();
//		 }
//		 return text.substring(42, 81) + text.substring(81, text.length()) + text.substring(0, 42);
//	 }
	
}
