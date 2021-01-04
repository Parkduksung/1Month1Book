package com.rsupport.util;

import com.rsupport.util.rslog.MLog;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.IntBuffer;
import java.nio.charset.Charset;
import java.util.ArrayList;

/**
 * Created by kwcho on 6/2/15.
 */
public class DurationMarker {
    private final int BUFFER_SIZE = 4096;
    private long startTime = 0;
    private String tag = null;
    private String filePath = null;
    private IntBuffer intBuffer = null;
    private ArrayList<IntBuffer> bufferArrayList = null;


    private DurationMarker(String filePath, String tag){
        this.tag = tag;
        this.filePath = filePath;
        bufferArrayList = new ArrayList<>();
    };

    public static DurationMarker create(String tag){
        return new DurationMarker("/sdcard/marker", tag);
    }

    public static DurationMarker create(String filePath, String tag){
        return new DurationMarker(filePath, tag);
    }

    public void reset(){
        startTime = System.currentTimeMillis();
    }

    public void mark(){
        if(intBuffer == null || intBuffer.hasRemaining() == false){
            intBuffer = IntBuffer.allocate(BUFFER_SIZE);
            bufferArrayList.add(intBuffer);
        }
        long currentTime = System.currentTimeMillis();
        intBuffer.put((int) (currentTime - startTime));
        startTime = currentTime;
    }

    public void output(){
        String filePath = this.filePath + File.separator + tag + ".txt";
        MLog.w("output : " + filePath);

        if(FileUtil.createNewFile(filePath) == false){
            MLog.e("can't create file : " + filePath);
            return;
        }

        Writer fw = null;
        try {
            File file = new File(filePath);
            fw = new OutputStreamWriter(new FileOutputStream(file), Charset.defaultCharset());
            for(IntBuffer intBuffer : bufferArrayList){
                intBuffer.flip();
                while(intBuffer.hasRemaining()){
                    fw.write(String.valueOf(intBuffer.get()));
                    fw.write("\n");
                }
            }

            fw.write("\n");
            fw.write("## AVG 30 ##");
            fw.write("\n");

            int count = 0;
            int sumDuration = 0;
            for(IntBuffer intBuffer : bufferArrayList){
                intBuffer.flip();
                while(intBuffer.hasRemaining()){
                    count++;
                    sumDuration += intBuffer.get();
                    if(count > 30){
                        fw.write(String.valueOf(sumDuration/count));
                        fw.write("\n");
                        count = 0;
                        sumDuration = 0;
                    }
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if(fw != null){
                try {
                    fw.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            bufferArrayList.clear();
        }
    }
}
