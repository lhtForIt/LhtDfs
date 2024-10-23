package com.lht.lhtfs;

import com.alibaba.fastjson.JSON;
import lombok.SneakyThrows;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.io.*;
import java.net.FileNameMap;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
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


    @SneakyThrows
    public static void writeMeta(File metaFile, FileMeta meta) {
        String json = JSON.toJSONString(meta);
        Files.writeString(Paths.get(metaFile.getAbsolutePath()), json, StandardOpenOption.CREATE, StandardOpenOption.WRITE);
    }

    @SneakyThrows
    public static void writeString(File metaFile, String content) {
        Files.writeString(Paths.get(metaFile.getAbsolutePath()), content, StandardOpenOption.CREATE, StandardOpenOption.WRITE);
    }

    @SneakyThrows
    public static void download(String download, File file) {
        System.out.println(" ======> download file: " + file.getAbsolutePath());
        RestTemplate restTemplate = new RestTemplate();
        HttpEntity<?> entity = new HttpEntity<>(new HttpHeaders());
        ResponseEntity<Resource> exchange = restTemplate
                .exchange(download, HttpMethod.GET, entity, Resource.class);
        InputStream fis = new BufferedInputStream(exchange.getBody().getInputStream());
        byte[] buffer = new byte[16 * 1024];
        OutputStream ops = new FileOutputStream(file);
        while (fis.read(buffer) != -1) {
            ops.write(buffer);
        }
        ops.flush();
        ops.close();
        fis.close();
    }
}
