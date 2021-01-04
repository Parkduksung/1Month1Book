package com.rsupport.util;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.net.LocalSocket;
import android.net.LocalSocketAddress;
import android.os.Build;
import android.util.Log;

import com.rsupport.android.engine.BuildConfig;
import com.rsupport.jarinput.shell.FinPacket;
import com.rsupport.jarinput.shell.IShellPacket;
import com.rsupport.jarinput.shell.ShellChannel;
import com.rsupport.jarinput.shell.ShellPacketHandler;
import com.rsupport.jarinput.shell.SyncPacket;
import com.rsupport.util.rslog.MLog;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.Charset;
import java.util.Locale;

/**
 * Created by kwcho on 3/23/16.
 */
public class LauncherUtilForInject implements ILauncherUtils{
    /**
     * launcher 를 종료 시킨다.
     * thread 에서 동작한다.
     * @param context
     */
    public void killLauncher(Context context) {
        new Thread(new LauncherKiller(context)).start();
    }

    @Override
    public boolean hasInjector() {
        return true;
    }

    /**
     * Liblauncher 의 PID 를 반환한다.
     * @param context
     * @return
     */
    public int getLauncherPID(final Context context, final boolean isFinShellChannel){
        try {
            final int[] port = new int[]{LauncherUtils.INVALID_LAUNCHER};
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    try{
                        int myPID = detectProcessID(context.getPackageName());
                        if(myPID < 0){
                            MLog.w("detectProcessID : " + myPID);
                            return;
                        }

                        ShellPacketHandler shellPacketHandler = ShellPacketHandler.createClient(
                                "localhost", myPID,
                                new ShellPacketHandler.OnShellPacketHandler() {
                                    @Override
                                    public void handle(ShellChannel shellChannel, IShellPacket shellPacket) {
                                        ByteBuffer bodyBuffer = shellPacket.getBodyBytes();
                                        bodyBuffer.get(); // dummy
                                        port[0] = bodyBuffer.getInt(); // port
                                        if(isFinShellChannel == true){
                                            try {
                                                shellChannel.write(new FinPacket());
                                            } catch (IOException e) {
                                                e.printStackTrace();
                                            }
                                        }
                                        else{
                                            try {
                                                shellChannel.write(new SyncPacket());
                                            } catch (IOException e) {
                                                e.printStackTrace();
                                            }
                                        }

                                    }
                                }
                        );
                        shellPacketHandler.run();
                        MLog.d("getLauncherPort : " + port[0]);
                    }
                    catch (Exception e){
                    }
                }
            });
            thread.start();
            thread.join(3000);
            return port[0];
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return LauncherUtils.INVALID_LAUNCHER;
    }

    private int detectProcessID(String packageName) {
        File file = new File("/data/local/tmp/" + packageName + ".process");
        if(file.exists() == false){
            MLog.d("not found process file.");
            return -1;
        }

        DataInputStream inputStream = null;
        try {
            inputStream = new DataInputStream(new FileInputStream(file));
            int port = Integer.parseInt(inputStream.readUTF());
            MLog.v("pid : " + port + ", packageName : " + packageName);
            return port;
        } catch (Exception e) {
            MLog.d(e);
            return -1;
        }
        finally {
            if(inputStream != null){
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Liblauncher 의 PID 를 반환한다.
     * @param context
     * @return
     */
    public int getLauncherPID(Context context){
        return getLauncherPID(context, false);
    };

    /**
     * launcher 이 살아 있는지 확인한다.
     * @param context
     * @return
     */
    public boolean isAliveLauncher(Context context){
        if(getLauncherPID(context) != LauncherUtils.INVALID_LAUNCHER){
            return true;
        }
        return false;
    }

    /**
     * Launcher 를 실행한다.(rooting 폰일 경우에만 동작한다)
     * @param context
     * @return
     */
    public boolean executeLauncher(Context context, boolean enableInput){
        BufferedReader in = null;
        try {
            if(isAliveLauncher(context) == true){
                MLog.w("already available");
                return true;
            }

            ApplicationInfo ai = context.getPackageManager().getApplicationInfo(context.getPackageName(), 0);
            String dataDir = ai.dataDir;

            String pkgName = context.getPackageName();
            String launcherFile = String.format(Locale.ENGLISH, dataDir + "/lib/liblauncher%d%s.so",
                    getLauncherSDKVersion(context),
                    BuildConfig.IS_MIRRORING ? "_m" : "");
            String apkFilePath = ai.sourceDir;

//            boolean enableVDisp = (Build.VERSION_CODES.KITKAT <= Build.VERSION.SDK_INT);

            String suPath[] = {
                    "/system/xbin/su",
                    "/sbin/su",
            };

            File su = null;
            for (String path : suPath) {
                File f = new File(path);
                if (f.canExecute()) { su = f; break;}
            }
            if (su == null) return false;

            Process process = null;
            process = Runtime.getRuntime().exec(su.getAbsolutePath());
            StringBuilder sb = new StringBuilder();
            sb.append(launcherFile);
            sb.append(" -pkgname ").append(pkgName);
            if (enableInput){
                sb.append(" -jarinput ");
                sb.append(apkFilePath);
            }
//            if(enableVDisp) sb.append(" -vdisp");
            sb.append("\n");

            process.getOutputStream().write(sb.toString().getBytes(Charset.defaultCharset()));
            process.getOutputStream().flush();

            MLog.v("%s", sb.toString());

            in = new BufferedReader(new InputStreamReader(process.getInputStream(), Charset.defaultCharset()));

            for (String line = in.readLine(); line != null; line = in.readLine()) {
                MLog.v("read from exe: " + line);
                if (line.contains("[ERRO]")) {
                    MLog.e(line);
                    process.destroy();
                    throw new RuntimeException(line);
                }
                if (line.contains("[INFO]")) {
                    MLog.i(line);
                    break;
                }
            }
        }catch (Exception e){
            MLog.e(Log.getStackTraceString(e));
        }finally {
            if(in != null){
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return false;
    }

    private int getLauncherSDKVersion(Context context){
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP ?
                Build.VERSION_CODES.LOLLIPOP:
                Build.VERSION_CODES.ICE_CREAM_SANDWICH
                ;
    }

    private class LauncherKiller implements Runnable{
        private Context context = null;

        LauncherKiller(Context context){
            this.context = context;
        }

        @Override
        public void run() {
            LocalSocket localSocket = null;
            Socket socket = null;
            InputStream is = null;
            OutputStream os = null;
            UDSConnection connection = null;

            int launcherPid = getLauncherPID(context, true);
            if(launcherPid == LauncherUtils.INVALID_LAUNCHER){
                MLog.e("INVALID_LAUNCHER");
                return;
            }

            try {
                // lillipop socket 사용.
                if(Build.VERSION_CODES.LOLLIPOP <= Build.VERSION.SDK_INT){
                    socket = new Socket("localhost", launcherPid);
                    is = socket.getInputStream();
                    os = socket.getOutputStream();
                }
                // lollipop 이하 uds 사용.
                else{
                    localSocket = new LocalSocket();
                    localSocket.connect(new LocalSocketAddress(context.getPackageName() + ".udsbinder." + launcherPid));;
                    if(localSocket != null && localSocket.isConnected()){
                        is = localSocket.getInputStream();
                        os = localSocket.getOutputStream();
                    }
                }

                if(is != null){
                    MLog.w("killLiblauncher");
                    connection = new UDSConnection("udsbinder");
                    connection.setInOutStream(is, os);
                    try {
                        final int DISCONNECT_EVENT = 28;
                        ByteBuffer bb = ByteBuffer.allocate(15).order(ByteOrder.LITTLE_ENDIAN);
                        bb.position(4);
                        bb.put((byte)DISCONNECT_EVENT);
                        int size = bb.position();
                        bb.putInt(0, size-4);
                        connection.write(bb.array(), 0, bb.position());
                    } catch (Exception e) {
                        MLog.e(Log.getStackTraceString(e));
                    }
                }

            } catch (Exception e) {
                MLog.e(Log.getStackTraceString(e));
            } finally{
                if(connection != null){
                    connection.close();
                    connection = null;
                }
                safetyCloseSocket(socket);
                safetyCloseLocalSocket(localSocket);
            }

            context = null;
        }

        private void safetyCloseLocalSocket(LocalSocket target){
            if(target != null){
                try {
                    target.close();
                } catch (IOException e) {
                    MLog.e(Log.getStackTraceString(e));
                }
            }
        }

        protected void safetyCloseSocket(Socket target){
            if(target != null){
                try {
                    target.close();
                } catch (IOException e) {
                    MLog.e(Log.getStackTraceString(e));
                }
            }
        }
        protected void safetyClose(Closeable target){
            if(target != null){
                try {
                    target.close();
                } catch (IOException e) {
                    MLog.e(Log.getStackTraceString(e));
                }
            }
        }

        private class UDSConnection{
            private InputStream inputStream = null;
            private OutputStream outputStream = null;

            private String name = null;

            public UDSConnection(String name){
                this.name = name;
            }

            public void setInOutStream(InputStream inputStream, OutputStream outputStream){
                this.inputStream = inputStream;
                this.outputStream = outputStream;
            }

            public synchronized boolean write(byte[] buffer, int offset, int count) throws IOException{
                if(outputStream == null){
                    return false;
                }
                outputStream.write(buffer, offset, count);
                return true;
            }

            public synchronized boolean write(int data) throws IOException{
                if(outputStream == null){
                    return false;
                }
                outputStream.write(data);
                return true;
            }

            public synchronized int read(byte[] buffer, int offset, int length) throws IOException{
                if(inputStream == null){
                    return -1;
                }
                return inputStream.read(buffer, offset, length);
            }

            public synchronized int read() throws IOException{
                if(inputStream == null){
                    return -1;
                }
                return inputStream.read();
            }

            public synchronized int available() throws IOException{
                if(inputStream == null){
                    return -1;
                }
                int available = inputStream.available();
                return available==0?-1:available;
            }

            public void close(){
                MLog.d("close.%s", name);
                safetyClose(inputStream);
                safetyClose(outputStream);
                inputStream = null;
                outputStream = null;
            }

            protected void safetyClose(LocalSocket target){
                if(target != null){
                    try {
                        target.close();
                    } catch (IOException e) {
                        MLog.e(Log.getStackTraceString(e));
                    }
                }
            }
            protected void safetyClose(Closeable target){
                if(target != null){
                    try {
                        target.close();
                    } catch (IOException e) {
                        MLog.e(Log.getStackTraceString(e));
                    }
                }
            }
        }
    }
}
