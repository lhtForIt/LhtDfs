package com.lht.lhtfs;

import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.net.FileNameMap;
import java.net.URLConnection;
import java.util.UUID;

/**
 * @author Leo
 * @date 2024/07/20
 */
public class FileUtils {

    static final String DEFAULT_MIME_TYPE = "application/octet-stream";

    public static String getMimeType(String fileName) {

        FileNameMap fileNameMap = URLConnection.getFileNameMap();
        String contentType = fileNameMap.getContentTypeFor(fileName);
        return contentType == null ? DEFAULT_MIME_TYPE : contentType;

    }

    public static void init(String uploadPath){
        File dir=new File(uploadPath);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        for (int i = 0; i < 256; i++) {
            String subDir=String.format("%02x", i);
            File file = new File(uploadPath + "/" + subDir);
            if (!file.exists()) {
                file.mkdirs();
            }
        }
    }


    public static String getUUIDFile(String file) {
        String fileName;
        fileName = UUID.randomUUID().toString() + getExt(file);
        return fileName;
    }

    public static String getSubDir(String file){
        return file.substring(0, 2);
    }

    public static String getExt(String originalFilename) {

        return originalFilename.substring(originalFilename.lastIndexOf("."));

    }


}
