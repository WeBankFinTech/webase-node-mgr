/**
 * Copyright 2014-2021 the original author or authors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package com.webank.webase.node.mgr.external;

import com.webank.webase.node.mgr.base.code.ConstantCode;
import com.webank.webase.node.mgr.base.enums.HasPk;
import com.webank.webase.node.mgr.base.exception.NodeMgrException;
import com.webank.webase.node.mgr.external.entity.RspAllExtAccount;
import com.webank.webase.node.mgr.external.entity.TbExternalAccount;
import com.webank.webase.node.mgr.external.mapper.TbExternalAccountMapper;
import com.webank.webase.node.mgr.user.UserService;
import com.webank.webase.node.mgr.user.entity.TbUser;
import com.webank.webase.node.mgr.user.entity.UserParam;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Log4j2
@Service
public class ExtAccountService {
    @Autowired
    private TbExternalAccountMapper extAccountMapper;
    @Autowired
    private UserService userService;


    /**
     * save block's
     * @param groupId
     * @param userAddress
     * @return
     */
    public int saveAccountOnChain(int groupId, String userAddress) {
        if (checkAddressExist(groupId, userAddress)) {
            return 0;
        }
        TbExternalAccount tbAccount = new TbExternalAccount();
        // check tb_user's address
        TbUser addressRow = userService.queryUser(null, groupId, null, userAddress, null);
        if (Objects.nonNull(addressRow)) {
            log.debug("saveAccountOnChain exists tb_user groupId:{} address:{}", groupId, userAddress);
            tbAccount.setUserName(tbAccount.getUserName());
            tbAccount.setSignUserId(tbAccount.getSignUserId());
            tbAccount.setHasPk(tbAccount.getHasPk());
        }
        tbAccount.setGroupId(groupId);
        tbAccount.setAddress(userAddress);
        tbAccount.setHasPk(HasPk.NONE.getValue());
        Date now = new Date();
        tbAccount.setCreateTime(now);
        tbAccount.setModifyTime(now);
        int insertRes = extAccountMapper.insertSelective(tbAccount);
        log.info("saveAccountOnChain groupId:{} address:{}, insertRes:{}",
            groupId, userAddress, insertRes);
        return insertRes;
    }

    private boolean checkAddressExist(int groupId, String userAddress) {
        int count = extAccountMapper.countOfExtAccount(groupId, userAddress);
        if (count > 0) {
            log.debug("checkAddressExist is true groupId:{} address:{}", groupId, userAddress);
            return true;
        }
        return false;
    }

    public List<TbExternalAccount> listExtAccount(UserParam param) {
        log.debug("start listExtAccount param:{}", param);
        return extAccountMapper.listExtAccount(param);
    }

    public int countExtAccount(UserParam param) {
        return extAccountMapper.countExtAccount(param);
    }

    public int updateAccountInfo(int accountId, String signUserId, String userName, String description) {
        TbExternalAccount update = extAccountMapper.selectByPrimaryKey(accountId);
        if (update == null) {
            log.error("updateAccountInfo id not exist!");
            throw new NodeMgrException(ConstantCode.ACCOUNT_NOT_EXISTS);
        }
        update.setUserName(userName);
        update.setSignUserId(signUserId);
        update.setDescription(description);
        update.setHasPk(HasPk.HAS.getValue());
        return extAccountMapper.updateByPrimaryKeySelective(update);
    }

    public void deleteByGroupId(int groupId) {
        int affected = extAccountMapper.deleteByGroupId(groupId);
        log.warn("deleteByGroupId:{} affected:{}", groupId, affected);
    }

    public List<RspAllExtAccount> getAllExtAccountLeftJoinUser(UserParam param) {
        log.info("getAllExtAccountLeftJoinUser param:{}", param);
        return extAccountMapper.listAccountJoinTbUser(param);
    }
}
