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

package com.webank.webase.node.mgr.appintegration.contractstore;

import com.webank.webase.node.mgr.account.AccountService;
import com.webank.webase.node.mgr.appintegration.contractstore.entity.ContractSource;
import com.webank.webase.node.mgr.appintegration.contractstore.entity.ContractStoreParam;
import com.webank.webase.node.mgr.appintegration.contractstore.entity.ReqContractSourceSave;
import com.webank.webase.node.mgr.appintegration.contractstore.entity.TbContractStore;
import java.util.List;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * ContractStoreService.
 */
@Log4j2
@Service
public class ContractStoreService {

    @Autowired
    private AccountService accountService;
    @Autowired
    private ContractStoreMapper contractStoreMapper;

    /**
     * save source info.
     * 
     * @param appKey
     * @param reqContractSourceSave
     */
    public void saveContractSource(String appKey, ReqContractSourceSave reqContractSourceSave) {
        log.debug("saveContractSource");
        // check account
        String account = reqContractSourceSave.getAccount();
        accountService.accountExist(account);
        for (ContractSource contractSource : reqContractSourceSave.getContractList()) {
            TbContractStore tbContractStore = new TbContractStore();
            BeanUtils.copyProperties(contractSource, tbContractStore);
            tbContractStore.setAppKey(appKey);
            tbContractStore.setContractVersion(reqContractSourceSave.getContractVersion());
            tbContractStore.setAccount(account);
            contractStoreMapper.saveContractStore(tbContractStore);
        }
    }

    /**
     * get count.
     * 
     * @param contractStoreParam
     * @return
     */
    public int countOfContractStore(ContractStoreParam contractStoreParam) {
        return contractStoreMapper.countOfContractStore(contractStoreParam);
    }

    /**
     * get List.
     * 
     * @param contractStoreParam
     * @return
     */
    public List<TbContractStore> listOfContractStore(ContractStoreParam contractStoreParam) {
        return contractStoreMapper.listOfContractStore(contractStoreParam);
    }

    /**
     * queryContractStore by id.
     * 
     * @return
     */
    public TbContractStore queryContractStoreById(Integer id) {
        ContractStoreParam contractStoreParam = new ContractStoreParam();
        contractStoreParam.setId(id);
        return queryContractStore(contractStoreParam);
    }

    /**
     * queryContractStore.
     * 
     * @param contractStoreParam
     * @return
     */
    private TbContractStore queryContractStore(ContractStoreParam contractStoreParam) {
        return contractStoreMapper.queryContractStore(contractStoreParam);
    }
}
