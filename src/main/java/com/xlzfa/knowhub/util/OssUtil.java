package com.xlzfa.knowhub.util;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.xlzfa.knowhub.config.OssProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@Component
public class OssUtil {

    @Autowired
    private OssProperties ossProperties;

    public String upload(MultipartFile file, String dir) {
        try {
            String endpoint = ossProperties.getEndpoint();
            String bucketName = ossProperties.getBucketName();

            String originalFilename = file.getOriginalFilename();
            String suffix = originalFilename.substring(originalFilename.lastIndexOf("."));
            String fileName = dir + "/" + UUID.randomUUID() + suffix;

            OSS oss = new OSSClientBuilder().build(
                    endpoint,
                    ossProperties.getAccessKeyId(),
                    ossProperties.getAccessKeySecret()
            );

            oss.putObject(bucketName, fileName, file.getInputStream());
            oss.shutdown();

            return "https://" + bucketName + "." + endpoint + "/" + fileName;

        } catch (Exception e) {
            throw new RuntimeException("OSS 上传失败", e);
        }
    }
}