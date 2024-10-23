package com.lht.lhtfs;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringEscapeUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.DigestUtils;
import org.springframework.util.FileCopyUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;

import static com.lht.lhtfs.HttpSyncer.XFILE_NAME;
import static com.lht.lhtfs.HttpSyncer.XORIGIN_FILE_NAME;

/**
 * @author Leo
 * @date 2024/07/19
 */
@RestController
public class FileController {

    @Value("${lhtfs.path}")
    private String uploadPath;

    @Value("${lhtfs.autoMd5}")
    private boolean autoMd5;

    @Value("${lhtfs.syncBackup}")
    private boolean syncBackup;

    @Value("${lhtfs.backupUrl}")
    private String backupUrl;

    @Value("${lhtfs.downloadUrl}")
    private String downloadUrl;

    @Autowired
    private HttpSyncer httpSyncer;

    @Autowired
    private MQSyncer mqSyncer;

    @SneakyThrows
    @PostMapping("/upload")
    public String uploadFile(@RequestParam("file") MultipartFile file, HttpServletRequest request) {
        //1. 处理文件
        String fileName = request.getHeader(XFILE_NAME);
        boolean needSync = false;
        String originalFilename = file.getOriginalFilename();
        if (!StringUtils.hasLength(fileName)) {// upload上传文件
            needSync = true;
            fileName = FileUtils.getUUIDFile(originalFilename);
            fileName = StringEscapeUtils.unescapeHtml4(fileName);
        } else {// 同步文件
            String oriFileName = request.getHeader(XORIGIN_FILE_NAME);
            if (StringUtils.hasLength(oriFileName)) {
                originalFilename = oriFileName;
            }
        }
        String subDir=FileUtils.getSubDir(fileName);
        File dest = new File(uploadPath + "/" + subDir + "/" + fileName);
        file.transferTo(dest);

        //2. 处理meta
        FileMeta meta = new FileMeta();
        meta.setName(fileName);
        meta.setOriginalFilename(originalFilename);
        meta.setSize(file.getSize());
        meta.setDownloadUrl(downloadUrl);
        if (autoMd5) {
            meta.getTags().put("md5", DigestUtils.md5DigestAsHex(new FileInputStream(dest)));
        }

        //2.1 存放到本地文件
        //2.2 存放到数据库
        //2.3 存放到配置中心或者注册中心,比如zk
        String metaName = fileName + ".meta";
        File metaFile = new File(uploadPath + "/" + subDir + "/" + metaName);
        FileUtils.writeMeta(metaFile, meta);


        //3. 同步到backup
        //同步文件到backup
        //既可以同步处理文件复制也可以异步处理文件复制。
        if (needSync) {
            if (syncBackup) {
                try {
                    httpSyncer.sync(dest, backupUrl, originalFilename);
                }catch (Exception e){
                    e.printStackTrace();
//                    MQSyncer.sync(dest, backupUrl, originalFilename);
                }
            } else {
                mqSyncer.sync(meta);
            }
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


    @RequestMapping("/meta")
    public String meta(String name){
        String subDir = FileUtils.getSubDir(name);
        String path = uploadPath + "/" + subDir + "/" + name + ".meta";
        File file=new File(path);
        try {
            String meta = FileCopyUtils.copyToString(new FileReader(file));
            return meta;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


}
