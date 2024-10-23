package com.lht.lhtfs;

import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.io.File;

/**
 * @author Leo
 * @date 2024/07/22
 */
@Component
@Slf4j
public class MQSyncer {

    @Value("${lhtfs.group}")
    private String group;

    @Value("${lhtfs.path}")
    private String uploadPath;

    @Value("${lhtfs.downloadUrl}")
    private String localUrl;

    @Autowired
    RocketMQTemplate template;

    private String topic = "lhtfs";

    public void sync(FileMeta meta) {
        Message<String> message = MessageBuilder.withPayload(JSON.toJSONString(meta)).build();
        template.send(topic, message);
        log.info(" =======> send message:  " + message);
    }


    @Service
    @RocketMQMessageListener(topic = "lhtfs", consumerGroup = "${lhtfs.group}")
    public class FileMQSyncer implements RocketMQListener<MessageExt>{

        @Override
        public void onMessage(MessageExt message) {

            // 1. 从消息里拿到meta数据
            log.info(" =======> onMessage ID = " + message.getMsgId());
            String json = new String(message.getBody());
            log.info(" =======> message json = " + json);
            FileMeta meta = JSON.parseObject(json, FileMeta.class);
            String downloadUrl = meta.getDownloadUrl();
            if (StringUtils.isEmpty(downloadUrl)) {
                log.info(" =====> downloadUrl is empty");
                return;
            }

            //本机也会去消费，所以要去重本地操作
            if (localUrl.equals(downloadUrl)) {
                log.info(" #@$@#%$##=> the same file server , ignore mq sync task .");
                return;
            }

            log.info(" #@$@#%$##=> the other file server , process mq sync task .");

            // 2. 写meta文件
            String dir = uploadPath + "/" + meta.getName().substring(0, 2);
            File metaFile = new File(dir, meta.getName() + ".meta");
            if (metaFile.exists()) {
                log.info(" ====> meta file exists and ignore save : " + metaFile.getAbsolutePath());
            } else {
                log.info(" ====> meta file save : " + metaFile.getAbsolutePath());
                FileUtils.writeString(metaFile, json);
            }

            // 3.下载文件
            File file = new File(dir, meta.getName());
            if (file.exists() && file.length() == meta.getSize()) {
                log.info(" ===> file exists and ignore download :" + file.getAbsolutePath());
                return;
            }

            String download = downloadUrl + "?name=" + file.getName();
            FileUtils.download(download, file);
        }
    }


}
