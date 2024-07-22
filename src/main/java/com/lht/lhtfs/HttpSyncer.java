package com.lht.lhtfs;

import lombok.SneakyThrows;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.net.URLEncoder;

/**
 * @author Leo
 * @date 2024/07/19
 */
@Component
public class HttpSyncer {

    public static final String XFINAL_NAME = "X-Filename";

    @SneakyThrows
    public String sync(File file, String url) {

        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        headers.add(XFINAL_NAME, file.getName());

        MultipartBodyBuilder builder = new MultipartBodyBuilder();
        builder.part("file", new FileSystemResource(file));

        HttpEntity<MultiValueMap<String, HttpEntity<?>>> httpEntity
                = new HttpEntity<>(builder.build(), headers);

        ResponseEntity<String> stringResponseEntity = restTemplate.postForEntity(url, httpEntity, String.class);
        String result = stringResponseEntity.getBody();
        System.out.println(" sync result = " + result);

        return result;

    }




}
