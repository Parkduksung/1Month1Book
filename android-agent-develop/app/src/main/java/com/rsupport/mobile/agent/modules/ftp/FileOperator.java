package com.rsupport.mobile.agent.modules.ftp;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import java.io.File;

public class FileOperator {

    public static boolean perform_file_operation(File target, Context parent) {
        boolean ret = false;
        try {
            if (!target.isFile()) return ret;
            String name = target.getName().toLowerCase();
            name = name.substring(name.lastIndexOf('.') + 1, name.length());

            if (name.toLowerCase().compareTo("apk") == 0) {
                return install_apk(target, parent);
            } else if (name.toLowerCase().compareTo("mp4") == 0) {
                return open_video(target, parent);
            } else if (name.toLowerCase().compareTo("3gp") == 0) {
                return open_video(target, parent);
            } else if (name.toLowerCase().compareTo("wmv") == 0) {
                return open_video(target, parent);
            } else if (name.toLowerCase().compareTo("avi") == 0) {
                return open_video(target, parent);
            } else if (name.toLowerCase().compareTo("rm") == 0) {
                return open_video(target, parent);
            } else if (name.toLowerCase().compareTo("rmvb") == 0) {
                return open_video(target, parent);
            } else if (name.toLowerCase().compareTo("wav") == 0) {
                return open_audio(target, parent);
            } else if (name.toLowerCase().compareTo("aac") == 0) {
                return open_audio(target, parent);
            } else if (name.toLowerCase().compareTo("m4a") == 0) {
                return open_audio(target, parent);
            } else if (name.toLowerCase().compareTo("mid") == 0) {
                return open_audio(target, parent);
            } else if (name.toLowerCase().compareTo("mp3") == 0) {
                return open_audio(target, parent);
            } else if (name.toLowerCase().compareTo("wma") == 0) {
                return open_audio(target, parent);
            } else if (name.toLowerCase().compareTo("amr") == 0) {
                return open_audio(target, parent);
            } else if (name.toLowerCase().compareTo("pdf") == 0) {
                return open_pdf(target, parent);
            } else if (name.toLowerCase().compareTo("doc") == 0) {
                return open_msword(target, parent);
            } else if (name.toLowerCase().compareTo("docx") == 0) {
                return open_msword(target, parent);
            } else if (name.toLowerCase().compareTo("txt") == 0) {
                return open_txt(target, parent);
            } else if (name.toLowerCase().compareTo("ini") == 0) {
                return open_txt(target, parent);
            } else if (name.toLowerCase().compareTo("jpg") == 0) {
                return open_image_jpeg_jpg(target, parent);
            } else if (name.toLowerCase().compareTo("jpeg") == 0) {
                return open_image_jpeg_jpg(target, parent);
            } else if (name.toLowerCase().compareTo("png") == 0) {
                return open_image_png(target, parent);
            } else if (name.toLowerCase().compareTo("gif") == 0) {
                return open_image_gif(target, parent);
            } else if (name.toLowerCase().compareTo("bmp") == 0) {
                return open_image_bmp(target, parent);
            } else if (name.toLowerCase().compareTo("html") == 0) {
                return open_html(target, parent);
            } else if (name.toLowerCase().compareTo("htm") == 0) {
                return open_html(target, parent);
            } else if (name.toLowerCase().compareTo("xml") == 0) {
                return open_xml(target, parent);
            } else if (name.toLowerCase().compareTo("odt") == 0) {
                return open_odt_file(target, parent);
            } else if (name.toLowerCase().compareTo("xlsx") == 0) {
                return open_xlsx(target, parent);
            } else if (name.toLowerCase().compareTo("docx") == 0) {
                return open_docx(target, parent);
            } else if (name.toLowerCase().compareTo("ppt") == 0) {
                return open_pptx(target, parent);
            } else if (name.toLowerCase().compareTo("pptx") == 0) {
                return open_pptx(target, parent);
            } else {
                return open_file_with_default(target, parent);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ret;
    }

    public static boolean install_apk(File apk, Context parent) {
        return open_file_with_type(apk, "application/vnd.android.package-archive", parent);
    }

    private static boolean open_video(File video_file, Context parent) {
        return open_file_with_type(video_file, "video/*", parent);
    }

    private static boolean open_audio(File audio_file, Context parent) {
        return open_file_with_type(audio_file, "audio/*", parent);
    }

    private static boolean open_image_jpeg_jpg(File img_file, Context parent) {
        return open_file_with_type(img_file, "image/jpeg", parent);
    }

    private static boolean open_image_png(File img_file, Context parent) {
        return open_file_with_type(img_file, "image/png", parent);
    }

    private static boolean open_image_bmp(File img_file, Context parent) {
        return open_file_with_type(img_file, "image/bmp", parent);
    }

    private static boolean open_image_gif(File img_file, Context parent) {
        return open_file_with_type(img_file, "image/gif", parent);
    }

    private static boolean open_odt_file(File odt_file, Context parent) {
        return open_file_with_type(odt_file, "application/vnd.oasis.opendocument.text", parent);
    }

    private static boolean open_msword(File word_file, Context parent) {
        return open_file_with_type(word_file, "application/msword", parent);
    }

    private static boolean open_xlsx(File word_file, Context parent) {
        return open_file_with_type(word_file, "application/vnd.ms-excel", parent);
    }

    private static boolean open_docx(File word_file, Context parent) {
        return open_file_with_type(word_file, "application/vnd.openxmlformats-officedocument.wordprocessingml.document", parent);
    }


    private static boolean open_pptx(File word_file, Context parent) {
        return open_file_with_type(word_file, "application/vnd.openxmlformats-officedocument.presentationml.presentation", parent);
    }


    private static boolean open_pdf(File pdf, Context parent) {
        return open_file_with_type(pdf, "application/pdf", parent);
    }

    private static boolean open_txt(File txt_file, Context parent) {
        return open_file_with_type(txt_file, "text/plain", parent);
    }

    private static boolean open_html(File html_file, Context parent) {
        return open_file_with_type(html_file, "text/html", parent);
    }

    private static boolean open_xml(File xml_file, Context parent) {
        return open_file_with_type(xml_file, "text/xml", parent);
    }

    private static boolean open_file_with_type(File target, String type, Context parent) {
        boolean ret = false;
        if (parent == null || target == null) return ret;
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setDataAndType(Uri.fromFile(target.getAbsoluteFile()), type);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        parent.startActivity(i);
        ret = true;
        return ret;
    }

    private static boolean open_file_with_default(File target, Context parent) {
        boolean ret = false;
        if (parent == null || target == null) return ret;
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.fromFile(target.getAbsoluteFile()));
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        i.addCategory(Intent.CATEGORY_DEFAULT);
        parent.startActivity(i);
        return ret;
    }

}
