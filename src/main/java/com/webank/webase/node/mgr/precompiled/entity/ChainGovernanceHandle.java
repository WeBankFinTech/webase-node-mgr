package com.webank.webase.node.mgr.precompiled.entity;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ChainGovernanceHandle {
    @NotNull
    private Integer groupId;
    @NotBlank
    private String fromAddress;
    private String address;
    private Integer weight;
    private Integer threshold;
    // use to replace fromAddress when request front
    private String signUserId;
}