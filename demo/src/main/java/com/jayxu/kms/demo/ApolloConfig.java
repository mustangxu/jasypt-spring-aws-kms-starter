/**
 * Authored by jayxu @2022
 */
package com.jayxu.kms.demo;

import java.util.Date;
import java.util.List;

import lombok.Value;

/**
 * @author xujiajing
 */
@Value
public class ApolloConfig {
    private String appId;
    private String clusterName;
    private String namespaceName;
    private String comment;
    private String format;
    private boolean isPublic;
    private List<ApolloConfigItem> items;
    private String dataChangeCreatedBy;
    private String dataChangeLastModifiedBy;
    private Date dataChangeCreatedTime;
    private Date dataChangeLastModifiedTime;

    @Value
    public static class ApolloConfigItem {
        private String key;
        private String value;
        private String dataChangeCreatedBy;
        private String dataChangeLastModifiedBy;
        private Date dataChangeCreatedTime;
        private Date dataChangeLastModifiedTime;
    }
}
