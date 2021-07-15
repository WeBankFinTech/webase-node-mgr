package com.webank.webase.node.mgr.precompiled.entity;

import javax.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ContractStatusHandle {
    private int groupId;
    @NotBlank
    private String contractAddress;
    // 4 types: freeze, unfreeze, getStatus
    // deprecated:  grantManager, listManager
    private String handleType;
    @NotBlank
    private String fromAddress;
    private String signUserId;
    private String grantAddress;
}