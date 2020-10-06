package com.rsupport.mobile.agent.modules.ftp;

import android.os.Environment;
import android.os.StatFs;

import com.rsupport.mobile.agent.modules.net.model.MsgPacket;
import com.rsupport.mobile.agent.service.HxdecThread;
import com.rsupport.mobile.agent.utils.compress.rcpZipHeader;
import com.rsupport.mobile.agent.utils.compress.unzip;
import com.rsupport.mobile.agent.modules.net.protocol.MessageID;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Iterator;

import control.Converter;
import control.DeflaterEx;

import com.rsupport.mobile.agent.constant.Global;
import com.rsupport.mobile.agent.constant.GlobalStatic;
import com.rsupport.util.log.RLog;

public class RsFTPTrans {
    private static final String className = "RsFTPTrans";

    static {
        System.loadLibrary("jzlib");
    }

    private static DeflaterEx m_compresser = new DeflaterEx(DeflaterEx.DEFAULT_COMPRESSION, true);
    private static final int SFTPDATA_SIZE = 1024 * 100;
    private static final int MAX_SFTP_ZIPBUFFER = SFTPDATA_SIZE * 2;
    private static byte zip_buffer[] = new byte[MAX_SFTP_ZIPBUFFER];
    private static byte send_buffer[] = new byte[MAX_SFTP_ZIPBUFFER];
    private static boolean m_isCompress = true;

    public static final int FILE_ATTRIBUTE_READ = 0x00000001;
    public static final int FILE_ATTRIBUTE_WRITE = 0x00000002;
    public static final int FILE_ATTRIBUTE_HIDDEN = 0x00000004;
    public static final int FILE_ATTRIBUTE_FILE = 0x00000008;
    public static final int FILE_ATTRIBUTE_DIRECTORY = 0x00000010;

    public static FtpFileInfo[] m_fileInfo;
    public static boolean isRunningDownload = false;
    public static boolean isRunningUpload = false;


    public static void reset() {
        m_compresser = new DeflaterEx(DeflaterEx.DEFAULT_COMPRESSION, true);
        m_unzip = new unzip();
        m_isClose = false;
    }

    public static boolean setFilelist(String filepath) {
        boolean ret = true;
        FtpFileInfo[] fileInfos = null;
        File[] files = null;
        try {
            File f = new File(filepath);
            files = f.listFiles();
        } catch (Exception e) {
            e.printStackTrace();
        }
        fileInfos = assignFileInfos(files);
        if (fileInfos == null) ret = false;
        return false;
    }

    private static int getFileHeaderSize(FtpFileInfo fileinfo) {
        RLog.i("getFileHeaderSize");
        int ret = 0, len = 0;
        try {
            ret += 16;
            len = fileinfo.name.getBytes("UTF-16LE").length;
            ret += len;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ret;
    }

    private static int getFilelistDataSize(String filepath) {
        RLog.i("getFilelistDataSize");
        int ret = 0, len = 0;
        try {
            if (m_fileInfo == null) return ret;
            ret += 4;
            ret += 4;
            ret += filepath.getBytes("UTF-16LE").length;
            ret += 2;
            for (FtpFileInfo fileinfo : m_fileInfo) {
                ret += 4; // blocksize
                ret += fileinfo.sizePacket();
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return ret;
    }

    static class CommonTime {
        int low;
        int high;
    }

    private static long getCompatableDateFormat(long time) {
        long ret = 0;
        time *= 10000;
        time = time + (0x19db1ded53e8000L);
        return ret;
    }

    private static CommonTime m_bufCT = new CommonTime();

    private static CommonTime getCommonDateFormat(long time) {
        time *= 10000;
        time = time + (0x19db1ded53e8000L);
        m_bufCT.low = (int) time;
        m_bufCT.high = (int) (time >> 32);
        return m_bufCT;
    }

    private static FTPHeader m_recvFtpHeader;
    private static FTPHeader m_sendFTPHeader;
    private static Hashtable m_mapSendFileInfo = new Hashtable<String, SendFileInfo>();

    public static byte[] getFTPHeader(String filepath) {
        RLog.i("RsFTPTrans getFTPHeader : " + filepath);
        byte[] ret = null;

        if (m_sendFTPHeader == null) m_sendFTPHeader = new FTPHeader();

        m_sendFTPHeader.totalcount = 0;
        m_sendFTPHeader.totalsize = 0;

        m_mapSendFileInfo.clear();
        m_sendFTPHeader.savepath = filepath;
        File f = new File(filepath);
        if (f == null) return ret;
        File[] arrFiles = f.listFiles();

        RLog.i("getAbsolutePath() : " + f.getAbsolutePath());
        if (f.isDirectory()) {
            m_mapSendFileInfo.put(String.valueOf(m_sendFTPHeader.totalcount), new SendFileInfo(filepath, f.getName()));
        } else {
            m_mapSendFileInfo.put(String.valueOf(m_sendFTPHeader.totalcount), new SendFileInfo(filepath, ""));
        }
        m_sendFTPHeader.totalcount++;
        m_sendFTPHeader.totalsize += f.length();

        String foldername = f.getName();
        if (arrFiles != null) {
            for (File file : arrFiles) {
                if (file.isDirectory()) {
                    procHeaderSubFolder(file.getAbsolutePath(), foldername);
                } else {
                    m_mapSendFileInfo.put(String.valueOf(m_sendFTPHeader.totalcount), new SendFileInfo(file.getAbsolutePath(), foldername));
                    m_sendFTPHeader.totalcount++;
                    m_sendFTPHeader.totalsize += file.length();
                }
            }
        }

        SendFileInfo fileInfo;
        Collection<SendFileInfo> coll = m_mapSendFileInfo.values();
        Iterator it = coll.iterator();
        while (it.hasNext()) {
            fileInfo = (SendFileInfo) it.next();
        }

        ret = new byte[4 + 8];
        int index = 0;
        int sizeLow = (int) m_sendFTPHeader.totalsize;
        int sizeHigh = (int) (m_sendFTPHeader.totalsize >> 32);
        System.arraycopy(Converter.getBytesFromIntLE(m_sendFTPHeader.totalcount), 0, ret, index, 4);
        index += 4;
        System.arraycopy(Converter.getBytesFromIntLE(sizeLow), 0, ret, index, 4);
        index += 4;
        System.arraycopy(Converter.getBytesFromIntLE(sizeHigh), 0, ret, index, 4);
        index += 4;

        RLog.i("RsFTPTrans getFTPHeader m_sendFTPHeader.totalcount : " + m_sendFTPHeader.totalcount);
        RLog.i("RsFTPTrans getFTPHeader m_sendFTPHeader.totalsize : " + m_sendFTPHeader.totalsize);

        return ret;
    }

    private static void procHeaderSubFolder(String filepath, String foldername) {
        RLog.i(filepath + " : " + foldername);
        File f = new File(filepath);
        File[] arrFiles = f.listFiles();

        foldername += "/";
        foldername += f.getName();

        m_mapSendFileInfo.put(String.valueOf(m_sendFTPHeader.totalcount), new SendFileInfo(filepath, foldername));
        m_sendFTPHeader.totalcount++;
        m_sendFTPHeader.totalsize += f.length();

        if (arrFiles == null) return;

        for (File file : arrFiles) {
            if (file.isDirectory()) {
                procHeaderSubFolder(file.getAbsolutePath(), foldername);
            } else {
                m_mapSendFileInfo.put(String.valueOf(m_sendFTPHeader.totalcount), new SendFileInfo(file.getAbsolutePath(), foldername));
                m_sendFTPHeader.totalcount++;
                m_sendFTPHeader.totalsize += file.length();
            }
        }
    }

    private static void assignFileInfo(FtpFileInfo fInfo, File f, String foldername) {
        RLog.i("assignFileInfo");
        fInfo.name = f.getName();
        fInfo.fullname = f.getAbsolutePath();
        if (f.canRead()) {
            fInfo.attribute |= FILE_ATTRIBUTE_READ;
        }
        if (f.canWrite()) {
            fInfo.attribute |= FILE_ATTRIBUTE_WRITE;
        }
        if (f.isHidden()) {
            fInfo.attribute |= FILE_ATTRIBUTE_HIDDEN;
        }
        if (f.isFile()) {
            fInfo.attribute |= FILE_ATTRIBUTE_FILE;
        }
        if (f.isDirectory()) {
            fInfo.attribute |= FILE_ATTRIBUTE_DIRECTORY;
        }
        long size = f.length();
        fInfo.totalsize = size;
        if (f.isDirectory()) fInfo.totalsize = 0;
        fInfo.sizeLow = (int) size;
        fInfo.sizeHigh = (int) (size >> 32);
        m_bufCT = getCommonDateFormat(f.lastModified());
        fInfo.modifydateLow = m_bufCT.low;
        fInfo.modifydateHigh = m_bufCT.high;
        fInfo.folderpath = foldername;
    }

    public static byte[] getFileHeaderData(String filepath, String foldername) {
        RLog.i(filepath);
        byte[] ret = null;
        File file = new File(filepath);
        if (file == null) return ret;

        FtpFileInfo fileinfo = null;
        fileinfo = new FtpFileInfo();

        assignFileInfo(fileinfo, file, foldername);

        m_sendFileInfo = fileinfo;

        int packetsize = fileinfo.sizePacket();
        ret = new byte[packetsize];

        makeFileHeader(ret, fileinfo);

        return ret;
    }

    public static byte[] getExistFileHeaderData(String filepath, String foldername) {
        RLog.i(filepath + " : " + foldername);
        byte[] ret = null;
        File file = new File(filepath);
        if (file == null) return ret;

        if (!file.exists()) {
            ret = new byte[1];
            ret[0] = (byte) 1;
            return ret;
        }

        FtpFileInfo fileinfo = null;
        fileinfo = new FtpFileInfo();

        assignFileInfo(fileinfo, file, foldername);

        int packetsize = fileinfo.sizePacket();
        ret = new byte[packetsize + 1];

        makeExistFileHeader(ret, fileinfo);

        return ret;
    }

    private static void makeFileHeader(byte[] byteArray, FtpFileInfo fileinfo) {
        int index = 0, len = 0;
        try {
            len = fileinfo.name.getBytes("UTF-16LE").length;

            int folderpathSize = fileinfo.folderpath.getBytes("UTF-16LE").length;
            System.arraycopy(Converter.getBytesFromIntLE(folderpathSize), 0, byteArray, index, 4);
            index += 4;
            if (folderpathSize > 0) {
                System.arraycopy(fileinfo.folderpath.getBytes("UTF-16LE"), 0, byteArray, index, folderpathSize);
                index += folderpathSize;
            }
            System.arraycopy(Converter.getBytesFromIntLE(fileinfo.sizeLow), 0, byteArray, index, 4);
            index += 4;
            System.arraycopy(Converter.getBytesFromIntLE(fileinfo.sizeHigh), 0, byteArray, index, 4);
            index += 4;
            System.arraycopy(Converter.getBytesFromIntLE((int) fileinfo.modifydateLow), 0, byteArray, index, 4);
            index += 4;
            System.arraycopy(Converter.getBytesFromIntLE((int) fileinfo.modifydateHigh), 0, byteArray, index, 4);
            index += 4;
            System.arraycopy(Converter.getBytesFromIntLE(fileinfo.attribute), 0, byteArray, index, 4);
            index += 4;
            System.arraycopy(fileinfo.name.getBytes("UTF-16LE"), 0, byteArray, index, len);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void makePriviewFileHeader(byte[] byteArray, FtpFileInfo fileinfo) {
        int index = 0, len = 0;
        try {
            len = fileinfo.name.getBytes("UTF-16LE").length;
            System.arraycopy(Converter.getBytesFromIntLE(fileinfo.sizeLow), 0, byteArray, index, 4);
            index += 4;
            System.arraycopy(Converter.getBytesFromIntLE(fileinfo.sizeHigh), 0, byteArray, index, 4);
            index += 4;
            System.arraycopy(Converter.getBytesFromIntLE((int) fileinfo.modifydateLow), 0, byteArray, index, 4);
            index += 4;
            System.arraycopy(Converter.getBytesFromIntLE((int) fileinfo.modifydateHigh), 0, byteArray, index, 4);
            index += 4;
            System.arraycopy(Converter.getBytesFromIntLE(fileinfo.attribute), 0, byteArray, index, 4);
            index += 4;
            System.arraycopy(fileinfo.name.getBytes("UTF-16LE"), 0, byteArray, index, len);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void makeExistFileHeader(byte[] byteArray, FtpFileInfo fileinfo) {
        RLog.i("makeExistFileHeader");
        int index = 0, len = 0;
        try {
            len = fileinfo.name.getBytes("UTF-16LE").length;

            byte[] byteFlag = new byte[1];
            byteFlag[0] = (byte) 0;
            System.arraycopy(byteFlag, 0, byteArray, index, 1);
            index++;
            int folderpathSize = fileinfo.folderpath.getBytes("UTF-16LE").length;
            System.arraycopy(Converter.getBytesFromIntLE(folderpathSize), 0, byteArray, index, 4);
            index += 4;
            if (folderpathSize > 0) {
                System.arraycopy(fileinfo.folderpath.getBytes("UTF-16LE"), 0, byteArray, index, folderpathSize);
                index += folderpathSize;
            }
            System.arraycopy(Converter.getBytesFromIntLE(fileinfo.sizeLow), 0, byteArray, index, 4);
            index += 4;
            System.arraycopy(Converter.getBytesFromIntLE(fileinfo.sizeHigh), 0, byteArray, index, 4);
            index += 4;
            System.arraycopy(Converter.getBytesFromIntLE((int) fileinfo.modifydateLow), 0, byteArray, index, 4);
            index += 4;
            System.arraycopy(Converter.getBytesFromIntLE((int) fileinfo.modifydateHigh), 0, byteArray, index, 4);
            index += 4;
            System.arraycopy(Converter.getBytesFromIntLE(fileinfo.attribute), 0, byteArray, index, 4);
            index += 4;
            System.arraycopy(fileinfo.name.getBytes("UTF-16LE"), 0, byteArray, index, len);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static int getFileInfoByteSize(FtpFileInfo fileinfo) {
        int ret = 0, len = 0;
        try {
            ret += 20;
            len = fileinfo.name.getBytes("UTF-16LE").length;
            ret += len;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ret;
    }

    public static boolean fileSubDeleteFolder(File file) {
        RLog.i("fileSubDeleteFolder");
        boolean ret = true;
        File[] files = file.listFiles();
        if (files == null) {
            file.delete();
            return ret;
        }
        int size = files.length;
        for (int i = 0; i < size; i++) {
            File subFile = files[i];
            if (subFile.isFile()) subFile.delete();
            else {
                ret = fileSubDeleteFolder(subFile);
                if (!ret) return ret;
            }
        }
        file.delete();
        return ret;
    }

    public static boolean procDeleteFiles(String arrfiles) {
        RLog.i("procDeleteFiles");
        boolean ret = true;
        int token = arrfiles.indexOf(";");
        String filename;
        int filelen = arrfiles.length();
        int index = 0;
        if (token <= 0) {
            ret = fileDelete(arrfiles);
        } else {
            while (token > 0) {
                filename = arrfiles.substring(index, token);
                index = token + 1;
                ret = fileDelete(filename);
                if (!ret) return ret;
                token = arrfiles.indexOf(";", token + 1);
            }
            if (filelen > index) {
                filename = arrfiles.substring(index, arrfiles.length());
                ret = fileDelete(filename);
            }
        }
        return ret;
    }

    private static boolean fileDelete(String filename) {
        RLog.i(filename);
        boolean ret = true;

        File f = new File(filename);

        File[] files;

        if (f.isFile()) {
            f.delete();
        } else {
            files = f.listFiles();
            if (files == null) {
                f.delete();
                return ret;
            }
            int size = files.length;
            for (int i = 0; i < size; i++) {
                File subFile = files[i];
                if (subFile.isFile()) subFile.delete();
                else {
                    ret = fileSubDeleteFolder(subFile);
                }
            }
            f.delete();
        }
        return ret;
    }

    public static boolean fileExecute(String filename) {
        RLog.i(filename);
        File f = new File(filename);
        if (f == null) return false;
        return FileOperator.perform_file_operation(new File(filename), Global.getInstance().getAppContext());
    }

    private static int getFileAttribute(String filepath) {
        int ret = 0;
        File file = new File(filepath);
        if (file == null) return ret;

        if (file.canRead()) {
            ret |= FILE_ATTRIBUTE_READ;
        }
        if (file.canWrite()) {
            ret |= FILE_ATTRIBUTE_WRITE;
        }
        if (file.isHidden()) {
            ret |= FILE_ATTRIBUTE_HIDDEN;
        }
        if (file.isFile()) {
            ret |= FILE_ATTRIBUTE_FILE;
        }
        if (file.isDirectory()) {
            ret |= FILE_ATTRIBUTE_DIRECTORY;
        }
        return ret;
    }

    public static byte[] getFlielistData(String filepath) {
        RLog.i(filepath);
        byte[] ret = null;
        int size = getFilelistDataSize(filepath);

        if (size <= 0) return ret;
        int index = 0, len = 0;
        ret = new byte[size];
        try {
            len = filepath.getBytes("UTF-16LE").length;
            System.arraycopy(Converter.getBytesFromIntLE(getFileAttribute(filepath)), 0, ret, index, 4);
            index += 4;
            System.arraycopy(Converter.getBytesFromIntLE(len), 0, ret, index, 4);
            index += 4;
            System.arraycopy(filepath.getBytes("UTF-16LE"), 0, ret, index, len);
            index += len;
            System.arraycopy(Converter.getBytesFromShortLE((short) m_fileInfo.length), 0, ret, index, 2);
            index += 2;
            int i = 0;
            for (FtpFileInfo fileinfo : m_fileInfo) {
                len = fileinfo.name.getBytes("UTF-16LE").length;
                System.arraycopy(Converter.getBytesFromIntLE(getFileInfoByteSize(fileinfo)), 0, ret, index, 4);
                index += 4;
                System.arraycopy(Converter.getBytesFromIntLE(fileinfo.sizeLow), 0, ret, index, 4);
                index += 4;
                System.arraycopy(Converter.getBytesFromIntLE(fileinfo.sizeHigh), 0, ret, index, 4);
                index += 4;
                System.arraycopy(Converter.getBytesFromIntLE((int) fileinfo.modifydateLow), 0, ret, index, 4);
                index += 4;
                System.arraycopy(Converter.getBytesFromIntLE((int) fileinfo.modifydateHigh), 0, ret, index, 4);
                index += 4;
                System.arraycopy(Converter.getBytesFromIntLE(fileinfo.attribute), 0, ret, index, 4);
                index += 4;
                System.arraycopy(fileinfo.name.getBytes("UTF-16LE"), 0, ret, index, len);
                index += len;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ret;
    }

    private static void initSendProc() {
        isRunningUpload = true;
    }

    public static boolean sendProcFTPHeader(String filepath) {
        RLog.i("RsFTPTrans sendProcFTPHeader : " + filepath);
        initSendProc();
        byte[] data = RsFTPTrans.getFTPHeader(filepath);
        return getAgentThread().getChannelAgentFTP().getRcmpFTPChannel().sendPacket(MessageID.rcpSFTP, MessageID.rcpExpFTPStart, data, data.length);
    }

    private static HxdecThread getAgentThread() {
        return Global.getInstance().getAgentThread();
    }

    private static SendFileInfo getSendFileInfoByIndex() {
        return (SendFileInfo) m_mapSendFileInfo.get(String.valueOf(indexSentFile));
    }

    private static boolean sendFileHeader() {
        boolean ret = false;

        isCanceledFile = false;
        SendFileInfo fileInfo = getSendFileInfoByIndex();
        if (fileInfo == null) return ret;

        RLog.i("RsFTPTrans sendFileHeader : " + fileInfo.fullfilename);
        procSendFileEx(fileInfo.fullfilename, fileInfo.foldername);

        return ret;
    }

    private static boolean sendProcFiles() {
        RLog.i("sendProcFiles");
        boolean ret = false;
        if (m_sendFTPHeader.savepath.length() <= 0) return ret;
        File f = new File(m_sendFTPHeader.savepath);
        File[] arrFiles = f.listFiles();

        ret = procSendFile(f.getAbsolutePath(), "");
        if (!ret) return ret;
        long totalsize = 0;

        String foldername = f.getName();
        if (arrFiles != null) {
            for (File file : arrFiles) {
                if (file.isDirectory()) {
                    ret = procSendSubFolder(file.getAbsolutePath(), foldername);
                    if (!ret) break;
                } else {
                    ret = procSendFile(file.getAbsolutePath(), foldername);
                    if (!ret) break;
                }
            }
        }
        return ret;
    }

    private static boolean procSendSubFolder(String filepath, String foldername) {
        RLog.i(filepath);
        boolean ret = false;
        File f = new File(filepath);
        if (f == null) return ret;
        File[] arrFiles = f.listFiles();

        ret = procSendFile(f.getAbsolutePath(), foldername);
        if (!ret) return ret;

        foldername += "/";
        foldername += f.getName();
        if (arrFiles != null) {
            for (File file : arrFiles) {
                if (file.isDirectory()) {
                    ret = procSendSubFolder(file.getAbsolutePath(), foldername);
                    if (!ret) break;
                } else {
                    ret = procSendFile(file.getAbsolutePath(), foldername);
                    if (!ret) break;
                }
            }
        }
        return ret;
    }

    private static int indexSentFile;

    private static boolean procSendFile(String filepath, String foldername) {
        RLog.i(filepath);
        boolean ret = false;
        m_isReceivedFilePos = false;
        ret = sendProcFileHeader(filepath, foldername);
        return ret;
    }

    private static boolean procSendFileEx(String filepath, String foldername) {
        RLog.i(filepath);
        boolean ret = false;
        ret = sendProcFileHeader(filepath, foldername);
        indexSentFile++;
        return ret;
    }

    private static boolean sendProcFileHeader(String filepath, String foldername) {
        byte[] data = RsFTPTrans.getFileHeaderData(filepath, foldername);
        uploadedSize = 0;
        RLog.i("RsFTPTrans sendProcFileHeader rcpExpFTPReceiveFileHeader : " + filepath);
        return getAgentThread().getChannelAgentFTP().getRcmpFTPChannel().sendPacket(MessageID.rcpSFTP, MessageID.rcpExpFTPReceiveFileHeader, data, data.length);
    }

    private static boolean sendProcExistFileHeader(String filepath, String foldername) {
        RLog.i(filepath);
        byte[] data = RsFTPTrans.getExistFileHeaderData(filepath, foldername);
        return getAgentThread().getChannelAgentFTP().getRcmpFTPChannel().sendPacket(MessageID.rcpSFTP, MessageID.rcpExpFTPSendExistFileHeader, data, data.length);
    }

    public static boolean isCanceledFile = false;

    public static boolean sendProcFileBody(String filepath) {
        RLog.i("RsFTP sendProcFileBody");
        boolean ret = false;

        try {
            File f = new File(filepath);
            RLog.i("filepath   :   " + filepath);
            if (m_upFilePos < 0 || m_upFilePos > f.length()) return ret;

            RLog.e("f.length()" + f.length());
            RLog.i("m_upFilePos :" + m_upFilePos);
            if (m_upFilePos == f.length() || f.isDirectory()) {
                RLog.i("RsFTP sendProcFileBody already exist");
                ret = true;
                return ret;
            }

            RandomAccessFile rf = new RandomAccessFile(f, "r");

            byte[] data = new byte[SFTPDATA_SIZE];
            int compressed = 0, packetsize = 0, index = 0;

            rcpZipHeader zipHeaderEx = new rcpZipHeader();

            rf.seek(m_upFilePos);
            uploadedSize = m_upFilePos;

            while (true) {
                if (m_isClose) {
                    RLog.i("RsFTP sendProcFileBody closed");
                    return false;
                }
                int read = rf.read(data, 0, SFTPDATA_SIZE);
                if (read == -1) break;

                Arrays.fill(zip_buffer, (byte) 0);
                Arrays.fill(send_buffer, (byte) 0);

                if (read <= 0) continue;

                index = 0;
                if (m_isCompress) {
                    m_compresser.setInput(data, 0, read);
                    compressed = m_compresser.deflate(zip_buffer);

                    if (compressed <= 0) break;

                    zipHeaderEx.originalsize = read;
                    zipHeaderEx.compresssize = compressed;
                    zipHeaderEx.push(send_buffer, index);
                    index += zipHeaderEx.size();
                    System.arraycopy(zip_buffer, 0, send_buffer, index, compressed);
                    packetsize = zipHeaderEx.size() + compressed;
                } else {
                    packetsize = zipHeaderEx.size() + read;
                    zipHeaderEx.originalsize = read;
                    zipHeaderEx.compresssize = 0;
                    zipHeaderEx.push(send_buffer, index);
                    index += zipHeaderEx.size();
                    System.arraycopy(data, 0, send_buffer, index, read);
                }
                if (isCanceledFile) {
                    RLog.i("RsFTPTrans sendProcFileBody canceled send file : " + filepath);
                    return true;
                }
                ret = getAgentThread().getChannelAgentFTP().getRcmpFTPChannel().sendPacket(MessageID.rcpSFTP, MessageID.rcpExpFTPData, send_buffer, packetsize);
                if (!ret) break;
                uploadedSize += zipHeaderEx.originalsize;
            }
            rf.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ret;
    }

    private static int calcUpProgress(long sentsize) {
        RLog.i(String.valueOf(sentsize));
        int ret = 0;
        float f = (float) ((float) sentsize / (float) m_sendFileInfo.totalsize) * 100;
        ret = (int) f;
        return ret;
    }

    public static boolean isFinish() {
        return m_isClose;
    }

    private static boolean m_isClose = false;

    public static void procClose() {
        RLog.i("RsFTPTrans procClose");
        m_isClose = true;
    }

    private static boolean sendProcFileDataEnd(String filepath) {
        RLog.i("RsFTP sendProcFileDataEnd : " + filepath);
        if (m_isClose) return false;
        isRunningUpload = false;
        return getAgentThread().getChannelAgentFTP().getRcmpFTPChannel().sendPacket(MessageID.rcpSFTP, MessageID.rcpExpFTPFileDataEnd);
    }


    public static boolean FTPGetEnd() {
        RLog.i("FTPGetEnd");
//		CoreVariables.isAgreeFtpDownConfirm = false;
//		CoreVariables.isCancleFtpDownConfirm = false;
        isRunningUpload = false;
        return true;
    }

    public static boolean sendProtocolFTPEnd() {
        if (m_isClose) return false;
        RLog.i("sendProtocolFTPEnd");
        isRunningUpload = false;
        return getAgentThread().getChannelAgentFTP().getRcmpFTPChannel().sendPacket(MessageID.rcpSFTP, MessageID.rcpExpFTPGetEnd);
    }

    private static String getSaveFullname(FtpFileInfo fileinfo) {
        RLog.i("getSaveFullname");
        String ret = "";
        try {
            String saveFullpath = "", foldername = "";
            if (fileinfo.folderpath.length() > 0) {
                saveFullpath = m_recvFtpHeader.savepath + "/" + fileinfo.folderpath + "/" + fileinfo.name;
                foldername = m_recvFtpHeader.savepath + "/" + fileinfo.folderpath;
            } else {
                saveFullpath = m_recvFtpHeader.savepath + "/" + fileinfo.name;
                foldername = m_recvFtpHeader.savepath;
            }
            File file = new File(foldername);
            file.mkdirs();
            ret = saveFullpath;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ret;
    }

    private static FtpFileInfo m_recvFileInfo;
    private static FtpFileInfo m_sendFileInfo;

    private static boolean readFTPHeader(byte[] data, int datasize) {
        RLog.i("readFTPHeader");
        boolean ret = false;
        try {
            if (data == null) return ret;
            if (datasize <= 0) return ret;

            m_recvFtpHeader = new FTPHeader();
            m_recvFtpHeader.save(data, 0, datasize);
            isRunningDownload = true;
            ret = sendProcDiskFreesize();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ret;
    }


    private static boolean readFileHeader(byte[] data, int datasize) {
        isCanceledFile = false;
        boolean ret = false;
        try {
            if (data == null) return ret;
            if (datasize <= 0) return ret;

            m_recvFileInfo = new FtpFileInfo();
            m_recvFileInfo.save(data, 0, datasize);

            String fullfilename = getSaveFullname(m_recvFileInfo);
            File file = new File(fullfilename);

            if ((m_recvFileInfo.totalsize <= 0) && ((m_recvFileInfo.attribute & FILE_ATTRIBUTE_DIRECTORY) > 0)) {
                if (file == null) return ret;
                file.mkdirs();
                ret = true;
            } else {
                if (file == null) return ret;

                sendProcExistFileHeader(fullfilename, m_recvFtpHeader.savepath);
                if (file.exists()) return true;

                long ltime = Converter.timeToLong(m_recvFileInfo.modifydateHigh, m_recvFileInfo.modifydateLow);
                if (file.exists()) file.delete();
                if (file.exists() && file.lastModified() == ltime) {
                    m_downFilePos = file.length();
                } else {
                    m_downFilePos = 0;
                    file.createNewFile();
                }
                m_recvFileInfo.modifydateLong = ltime;
                g_receiveFile = new RandomAccessFile(file, "rw");
                g_receiveFile.seek(m_downFilePos);
                if (g_receiveFile == null) return ret;
                downloadedSize = m_downFilePos;
                sendProcFilePos(m_downFilePos);
                ret = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ret;
    }

    private static boolean sendProcExistFilePos(int opt) {
        RLog.i("RsFTPTrans sendProcExistFilePos : " + String.valueOf(opt));
        boolean ret = false;
        try {
            String fullfilename = getSaveFullname(m_recvFileInfo);
            File file = new File(fullfilename);

            if ((m_recvFileInfo.totalsize <= 0) && ((m_recvFileInfo.attribute & FILE_ATTRIBUTE_DIRECTORY) > 0)) {
                if (file == null) return ret;
                file.mkdirs();
                ret = true;
            } else {
                if (file == null) return ret;
                long ltime = Converter.timeToLong(m_recvFileInfo.modifydateHigh, m_recvFileInfo.modifydateLow);
                if (opt == TRANSOPT_NEW) {
                    if (file.exists()) file.delete();
                }
                if (file.exists()) {
                    m_downFilePos = file.length();
                } else {
                    m_downFilePos = 0;
                    file.createNewFile();
                }
                m_recvFileInfo.modifydateLong = ltime;
                g_receiveFile = new RandomAccessFile(file, "rw");
                g_receiveFile.seek(m_downFilePos);
                if (g_receiveFile == null) return ret;
                downloadedSize = m_downFilePos;
                sendProcFilePos(m_downFilePos);
                ret = true;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return ret;
    }

    private static boolean sendProcDiskFreesize() {
        long free = getAvailableExternalSize();
        RLog.i("sendProcDiskFreesize : " + free);
        byte[] data = new byte[8];
        int low = (int) free;
        int high = (int) (free >> 32);
        int index = 0;
        System.arraycopy(Converter.getBytesFromIntLE(low), 0, data, index, 4);
        index += 4;
        System.arraycopy(Converter.getBytesFromIntLE(high), 0, data, index, 4);
        index += 4;
        return getAgentThread().getChannelAgentFTP().getRcmpFTPChannel().sendPacket(MessageID.rcpSFTP, MessageID.rcpExpFTPDiskFreeSpace, data, data.length);
    }

    private static long getAvailableExternalSize() {
        RLog.i("getAvailableExternalSize");
        long free = 0;
        File externalPath = Environment.getExternalStorageDirectory();
        if (externalPath != null) {
            try {
                StatFs stat = new StatFs(externalPath.getAbsolutePath());
                long blockSize = stat.getBlockSize();
                long availBlocks = stat.getAvailableBlocks();

                free = availBlocks * blockSize;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return free;
    }

    private static boolean isAvailableRecvSize(long recvFilesize) {
        boolean ret = true;
        long free = 0;
        File externalPath = Environment.getExternalStorageDirectory();
        if (externalPath != null) {
            try {
                StatFs stat = new StatFs(externalPath.getAbsolutePath());
                long blockSize = stat.getBlockSize();
                long availBlocks = stat.getAvailableBlocks();

                free = availBlocks * blockSize;
                if (free < recvFilesize) ret = false;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return ret;
    }

    private static long getDiskFreesize() {
        RLog.i("getDiskFreesize");
        long ret = 0;
        long free = 0;
        File externalPath = Environment.getExternalStorageDirectory();
        if (externalPath != null) {
            try {
                StatFs stat = new StatFs(externalPath.getAbsolutePath());
                long blockSize = stat.getBlockSize();
                long availBlocks = stat.getAvailableBlocks();

                ret = availBlocks * blockSize;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return ret;
    }

    private static boolean sendProcFilePos(long filePos) {
        byte[] data = new byte[8];
        int low = (int) filePos;
        int high = (int) (filePos >> 32);
        int index = 0;
        System.arraycopy(Converter.getBytesFromIntLE(low), 0, data, index, 4);
        index += 4;
        System.arraycopy(Converter.getBytesFromIntLE(high), 0, data, index, 4);
        index += 4;
        RLog.i("RsFTPTrans sendPosFilePos : " + filePos);
        return getAgentThread().getChannelAgentFTP().getRcmpFTPChannel().sendPacket(MessageID.rcpSFTP, MessageID.rcpExpFTPFilePos, data, data.length);
    }

    private static long getMergeSize(int high, int low) {
        long ret = 0;
        ret = ((long) high) << 32 | (low & 0xffffffffL);
        return ret;
    }

    private static final int TRANSOPT_NEW = 0;
    private static final int TRANSOPT_KEEP = 1;

    private static boolean readFileTransOpt(byte[] data, int datasize) {
        RLog.i("readFileTransOpt");
        boolean ret = false;
        try {
            int opt = ((int) data[0] & 0xff);
            ret = sendProcExistFilePos(opt);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ret;
    }

    private static boolean m_isReceivedFilePos;
    private static long m_upFilePos;
    private static long m_downFilePos;

    private static boolean readFilePos(byte[] data, int datasize) {
        RLog.i("RsFTP readFilePos");
        boolean ret = false;
        try {
            int index = 0;
            int sizeLow = Converter.readIntLittleEndian(data, index);
            index += 4;
            int sizeHigh = Converter.readIntLittleEndian(data, index);
            index += 4;
            m_upFilePos = getMergeSize(sizeHigh, sizeLow);
            if (m_upFilePos < 0) return ret;
            if (m_sendFileInfo == null) return ret;

            ret = sendProcFileBody(m_sendFileInfo.fullname);
            if (!ret) return ret;
            if (!isCanceledFile) {
                ret = sendProcFileDataEnd(m_sendFileInfo.fullname);
                if (!ret) return ret;
            }
            if (indexSentFile < m_sendFTPHeader.totalcount) {
                if (GlobalStatic.androidProtocolVersion >= 4) {
                    if (isCanceledFile) {
                        indexSentFile = m_sendFTPHeader.totalcount;
                        return true;
                    }
                }
                sendFileHeader();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ret;
    }

    private static boolean isDirectory(int attribute) {
        boolean ret = false;
        if ((m_sendFileInfo.attribute & FILE_ATTRIBUTE_DIRECTORY) > 0) {
            ret = true;
        }
        return ret;
    }

    private static boolean isHaveToSend(String filepath) {
        boolean ret = true;
        File f = new File(filepath);
        if (f == null) return ret;
        if (m_upFilePos == f.length()) ret = false;
        return ret;
    }

    private static boolean readDiskFreeSpace(byte[] data, int datasize) {
        RLog.i("RsFTPTrans readDiskFreeSpace start");
        boolean ret = false;
        try {
            int index = 0;
            int freeLow = Converter.readIntLittleEndian(data, index);
            index += 4;
            int freeHigh = Converter.readIntLittleEndian(data, index);
            index += 4;
            long freesize = getMergeSize(freeHigh, freeLow);

            RLog.i("RsFTPTrans readDiskFreeSpace freesize : " + freesize);

            indexSentFile = 0;
            if (m_sendFTPHeader.totalsize > freesize) {
                RLog.e("RsFTPTrans readDiskFreeSpace over file size");
                FTPGetEnd();
            } else {
                sendFileHeader();
            }
            ret = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ret;
    }

    private static long downloadedSize;
    private static long uploadedSize;
    private static rcpZipHeader m_bufZipHeader;

    private static boolean readFileData(MsgPacket msg) {
        RLog.i("readFileData");
        boolean ret = false;
        try {
            if (msg == null) return ret;
            if (msg.getData() == null) return ret;
            if (msg.getDataSize() <= 0) return ret;

            if (m_bufZipHeader == null) m_bufZipHeader = new rcpZipHeader();
            m_bufZipHeader.save(msg.getData(), 0);
            if (g_receiveFile == null) return ret;
            if (m_bufZipHeader.compresssize > 0) {
                int decompressSize = readCompressData(msg.getData(), m_bufZipHeader.size(), zip_buffer, m_bufZipHeader);
                if (decompressSize <= 0) return ret;
                g_receiveFile.seek(g_receiveFile.length());
                g_receiveFile.write(zip_buffer, 0, m_bufZipHeader.originalsize);
            } else {
                g_receiveFile.write(msg.getData(), m_bufZipHeader.size(), msg.getData().length - m_bufZipHeader.size());
            }
            setLastModifyOnFile();
            downloadedSize += m_bufZipHeader.originalsize;
            ret = true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return ret;
    }

    private static void setLastModifyOnFile() {
        File f = new File(getSaveFullname(m_recvFileInfo));
        if (f == null || !f.exists()) return;
        f.setLastModified(m_recvFileInfo.modifydateLong);
    }

    private static int calcDownProgress(int receivedsize) {
        int ret = 0;
        float f = (float) ((float) receivedsize / (float) m_recvFileInfo.totalsize) * 100;
        ret = (int) f;
        RLog.i(String.valueOf(ret));
        return ret;
    }

    public static boolean recvFileDataClose() {
        RLog.i("recvFileDataClose");
        boolean ret = true;
        try {
            if (g_receiveFile == null) return ret;
            if (m_recvFileInfo == null) return ret;

            g_receiveFile.close();
            File f = new File(getSaveFullname(m_recvFileInfo));
            if (f == null || !f.exists()) return ret;
            f.setLastModified(m_recvFileInfo.modifydateLong);
            if (RsFTPTrans.isCanceledFile) return ret;
            ret = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ret;
    }

    public static boolean isRunningDownload() {
        return isRunningDownload;
    }

    public static boolean FTPSendEnd() {
        RLog.i("FTPSendEnd");
        boolean ret = false;
        try {
            if (m_recvFtpHeader != null) m_recvFtpHeader.clearData();
            isRunningDownload = false;
            ret = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ret;
    }

    public static void recvForceClose() {
        isRunningDownload = false;
    }

    private static unzip m_unzip = new unzip();

    private static int readCompressData(byte[] recvdata, byte[] databuf, rcpZipHeader zipHeader) {
        int decompressSize = 0;
        try {
            m_unzip.set_outbuf(databuf, 0, zipHeader.originalsize);
            m_unzip.set_inbuf(recvdata, 0, recvdata.length);
            decompressSize = m_unzip.decompress();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return decompressSize;
    }

    private static int readCompressData(byte[] recvdata, int offset, byte[] databuf, rcpZipHeader zipHeader) {
        int decompressSize = 0;
        try {
            m_unzip.set_outbuf(databuf, 0, zipHeader.originalsize);
            m_unzip.set_inbuf(recvdata, offset, zipHeader.compresssize);
            decompressSize = m_unzip.decompress();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return decompressSize;
    }

    public static boolean recvFTPHeader(byte[] data, int datasize) {
        boolean ret = false;
        try {
            ret = readFTPHeader(data, datasize);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ret;
    }

    private static RandomAccessFile g_receiveFile;

    public static boolean recvFileHeader(byte[] data, int datasize) {
        boolean ret = false;
        try {
            ret = readFileHeader(data, datasize);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ret;
    }

    public static boolean recvFileTransOpt(byte[] data, int datasize) {
        boolean ret = false;
        try {
            ret = readFileTransOpt(data, datasize);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ret;
    }

    public static boolean recvFilePos(byte[] data, int datasize) {
        boolean ret = false;
        try {
            ret = readFilePos(data, datasize);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ret;
    }

    public static boolean recvDiskFreeSpace(byte[] data, int datasize) {
        boolean ret = false;
        try {
            ret = readDiskFreeSpace(data, datasize);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ret;
    }

    public static boolean recvFileData(MsgPacket msg) {
        boolean ret = false;
        try {
            ret = readFileData(msg);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ret;
    }

    private static FtpFileInfo[] assignFileInfos(File[] files) {
        RLog.i("assignFileInfos");
        if (files == null) return null;
        int listsize = files.length;
        m_fileInfo = new FtpFileInfo[listsize];
        for (int i = 0; i < listsize; i++) {
            m_fileInfo[i] = new FtpFileInfo();
            File file = files[i];
            if (file == null) continue;
            m_fileInfo[i].name = file.getName();
            if (file.canRead()) {
                m_fileInfo[i].attribute |= FILE_ATTRIBUTE_READ;
            }
            if (file.canWrite()) {
                m_fileInfo[i].attribute |= FILE_ATTRIBUTE_WRITE;
            }
            if (file.isHidden()) {
                m_fileInfo[i].attribute |= FILE_ATTRIBUTE_HIDDEN;
            }
            if (file.isFile()) {
                m_fileInfo[i].attribute |= FILE_ATTRIBUTE_FILE;
            }
            if (file.isDirectory()) {
                m_fileInfo[i].attribute |= FILE_ATTRIBUTE_DIRECTORY;
            }
            long size = file.length();
            m_fileInfo[i].sizeLow = (int) size;
            m_fileInfo[i].sizeHigh = (int) (size >> 32);
            m_bufCT = getCommonDateFormat(file.lastModified());
            m_fileInfo[i].modifydateLong = file.lastModified();
            m_fileInfo[i].modifydateLow = m_bufCT.low;
            m_fileInfo[i].modifydateHigh = m_bufCT.high;
        }
        return m_fileInfo;
    }


}
