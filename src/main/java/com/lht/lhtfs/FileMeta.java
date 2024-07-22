package com.lht.lhtfs;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Leo
 * @date 2024/07/22
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class FileMeta {

    private String name;
    private String originalFilename;
    private long size;
    private boolean autoMd5;
    private Map<String, String> tags = new HashMap<>();


}
