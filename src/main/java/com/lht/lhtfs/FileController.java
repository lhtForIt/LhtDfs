package com.lht.lhtfs;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringEscapeUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.net.URLEncoder;
import java.util.UUID;

import static com.lht.lhtfs.HttpSyncer.XFINAL_NAME;

/**
 * @author Leo
 * @date 2024/07/19
 */
@RestController
public class FileController {

    @Value("${lhtfs.path}")
    private String uploadPath;

    @Value("${lhtfs.backupUrl}")
    private String backupUrl;

    @Autowired
    private HttpSyncer httpSyncer;

    @SneakyThrows
    @PostMapping("/upload")
    public String uploadFile(@RequestParam("file") MultipartFile file, HttpServletRequest request) {
        String fileName = request.getHeader(XFINAL_NAME);
        boolean needSync = false;
        if (!StringUtils.hasLength(fileName)) {
            needSync = true;
//            fileName = file.getOriginalFilename();
            fileName = FileUtils.getUUIDFile(file.getOriginalFilename());
            fileName = StringEscapeUtils.unescapeHtml4(fileName);
        }
        String subDir=FileUtils.getSubDir(fileName);
        File dest = new File(uploadPath + "/" + subDir + "/" + fileName);
        file.transferTo(dest);

        //同步文件到backup
        if (needSync) {
            httpSyncer.sync(dest, backupUrl);
        }

        return fileName;
    }




    @RequestMapping("/download")
    public void download(String name, HttpServletResponse response){
        String subDir = FileUtils.getSubDir(name);
        String path = uploadPath + "/" + subDir + "/" + name;
        File file=new File(path);

        try{
            FileInputStream inputStream = new FileInputStream(file);
            InputStream fis = new BufferedInputStream(inputStream);
            byte[] buffer = new byte[16 * 1024];
            //加一些response的头
            response.setCharacterEncoding("UTF-8");
//            response.setContentType("application/octet-stream");
            response.setContentType("image/png");
//            response.addHeader("Content-Disposition", "attachment;filename=" + name);
            response.setHeader("Content-Length", String.valueOf(file.length()));
            //读取文件信息并逐段输出
            OutputStream ops = response.getOutputStream();
            while (fis.read(buffer) != -1) {
                ops.write(buffer);
            }
            ops.flush();
            fis.close();
        }catch (Exception e){
            e.printStackTrace();
        }

    }


}
