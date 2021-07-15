/**
 * Copyright 2014-2021  the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package com.webank.webase.node.mgr.contract;

import com.webank.webase.node.mgr.appintegration.contractstore.ContractStoreService;
import com.webank.webase.node.mgr.appintegration.contractstore.entity.ContractStoreParam;
import com.webank.webase.node.mgr.appintegration.contractstore.entity.ReqContractAddressSave;
import com.webank.webase.node.mgr.appintegration.contractstore.entity.TbContractStore;
import com.webank.webase.node.mgr.base.annotation.entity.CurrentAccountInfo;
import com.webank.webase.node.mgr.base.code.ConstantCode;
import com.webank.webase.node.mgr.base.entity.BasePageResponse;
import com.webank.webase.node.mgr.base.enums.ContractStatus;
import com.webank.webase.node.mgr.base.enums.ContractType;
import com.webank.webase.node.mgr.base.enums.RoleType;
import com.webank.webase.node.mgr.base.exception.NodeMgrException;
import com.webank.webase.node.mgr.base.properties.ConstantProperties;
import com.webank.webase.node.mgr.base.tools.JsonTools;
import com.webank.webase.node.mgr.base.tools.Web3Tools;
import com.webank.webase.node.mgr.contract.abi.AbiService;
import com.webank.webase.node.mgr.contract.abi.entity.AbiInfo;
import com.webank.webase.node.mgr.contract.entity.*;
import com.webank.webase.node.mgr.front.entity.TransactionParam;
import com.webank.webase.node.mgr.frontinterface.FrontInterfaceService;
import com.webank.webase.node.mgr.frontinterface.FrontRestTools;
import com.webank.webase.node.mgr.frontinterface.entity.PostAbiInfo;
import com.webank.webase.node.mgr.group.GroupService;
import com.webank.webase.node.mgr.method.MethodService;
import com.webank.webase.node.mgr.method.entity.NewMethodInputParam;
import com.webank.webase.node.mgr.monitor.MonitorService;
import com.webank.webase.node.mgr.precompiled.permission.PermissionManageService;
import com.webank.webase.node.mgr.user.UserService;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.fisco.bcos.sdk.abi.datatypes.Address;
import org.fisco.bcos.sdk.abi.wrapper.ABIDefinition;
import org.fisco.bcos.sdk.contract.precompiled.permission.PermissionInfo;
import org.fisco.bcos.sdk.crypto.CryptoSuite;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * services for contract data.
 */
@Log4j2
@Service
public class  ContractService {

    private static final int CONTRACT_ADDRESS_LENGTH = 42;
    private static final String PERMISSION_TYPE_DEPLOY_AND_CREATE = "deployAndCreate";
    public static final String STATE_MUTABILITY_VIEW = "view";
    public static final String STATE_MUTABILITY_PURE = "pure";

    @Autowired
    private ContractMapper contractMapper;
    @Autowired
    private FrontRestTools frontRestTools;
    @Autowired
    @Lazy
    private MonitorService monitorService;
    @Autowired
    private FrontInterfaceService frontInterface;
    @Autowired
    private UserService userService;
    @Autowired
    private AbiService abiService;
    @Autowired
    private PermissionManageService permissionManageService;
    @Autowired
    private ContractPathService contractPathService;
    @Autowired
    private ContractStoreService contractStoreService;
    @Autowired
    private MethodService methodService;
    @Autowired
    private ConstantProperties constantProperties;
    @Autowired
    private CryptoSuite cryptoSuite;
    @Autowired
    private GroupService groupService;

    /**
     * add new contract data.
     */
    public TbContract saveContract(Contract contract) throws NodeMgrException {
        log.info("start saveContract Contract:{}", JsonTools.toJSONString(contract));
        TbContract tbContract;
        if (contract.getContractId() == null) {
            //new
            tbContract = newContract(contract);
        } else {
            //update
            tbContract = updateContract(contract);
        }

        if (StringUtils.isNotBlank(tbContract.getContractBin())) {
            // update monitor unusual deployInputParam's info
            monitorService.updateUnusualContract(tbContract.getGroupId(),
                tbContract.getContractName(), tbContract.getContractBin());
        }

        return tbContract;
    }


    /**
     * save new contract.
     */
    private TbContract newContract(Contract contract) {
        if (contract.getAccount() == null) {
            throw new NodeMgrException(ConstantCode.ACCOUNT_CANNOT_BE_EMPTY);
        }
        //check contract not exist.
        verifyContractNotExist(contract.getGroupId(), contract.getContractName(),
            contract.getContractPath(), contract.getAccount());

        //add to database.
        TbContract tbContract = new TbContract();
        BeanUtils.copyProperties(contract, tbContract);
        log.debug("newContract save contract");
        contractMapper.add(tbContract);
        // save contract path
        log.debug("newContract save contract path");
        // if exist, auto not save (ignore)
        contractPathService.save(contract.getGroupId(), contract.getContractPath(), contract.getAccount(), true);
        return tbContract;
    }

    /**
     * save application's contract.
     *
     * @param appKey
     * @param reqContractAddressSave
     */
    @Transactional
    public void appContractSave(String appKey, ReqContractAddressSave reqContractAddressSave)
        throws IOException {
        log.info("appContractSave appKey:{},reqContractAddressSave:{}", appKey, reqContractAddressSave);
        Integer groupId = reqContractAddressSave.getGroupId();
        // check group id
        groupService.checkGroupId(groupId);
        // get runtimeBin
        String runtimeBin = abiService.getAddressRuntimeBin(groupId,
            reqContractAddressSave.getContractAddress());
        String contractName = reqContractAddressSave.getContractName();
        String contractVersion = reqContractAddressSave.getContractVersion();
        String contractPath = reqContractAddressSave.getContractPath();
        // get contract store
        ContractStoreParam contractStoreParam = new ContractStoreParam();
        contractStoreParam.setAppKey(appKey);
        contractStoreParam.setContractVersion(contractVersion);
        contractStoreParam.setContractName(contractName);
        List<TbContractStore> listOfContractStore =
            contractStoreService.listOfContractStore(contractStoreParam);
        if (CollectionUtils.isEmpty(listOfContractStore)) {
            throw new NodeMgrException(ConstantCode.CONTRACT_SOURCE_NOT_EXIST);
        }
        boolean pathExist = contractPathService.checkPathExist(groupId, contractPath,
                listOfContractStore.get(0).getAccount());
        for (TbContractStore tbContractStore : listOfContractStore) {
            // check if tbContractStore has been saved
            if (pathExist && !tbContractStore.getContractName().equals(contractName)) {
                continue;
            }
            ContractParam param = new ContractParam(groupId, contractPath,
                tbContractStore.getContractName(), tbContractStore.getAccount());
            TbContract localContract = queryContract(param);
            // check if deployed contract saved
            if (Objects.nonNull(localContract)
                && localContract.getContractStatus() == ContractStatus.DEPLOYED.getValue()
                && !tbContractStore.getContractName().equals(contractName)) {
                continue;
            }
            TbContract tbContract = new TbContract();
            BeanUtils.copyProperties(tbContractStore, tbContract);
            tbContract.setGroupId(groupId);
            tbContract.setContractStatus(ContractStatus.NOTDEPLOYED.getValue());
            tbContract.setContractPath(contractPath);
            tbContract.setContractType(ContractType.APPIMPORT.getValue());
            if (tbContractStore.getContractName().equals(contractName)) {
                tbContract.setContractAddress(reqContractAddressSave.getContractAddress());
                tbContract.setContractBin(runtimeBin);
                tbContract.setContractStatus(ContractStatus.DEPLOYED.getValue());
                // save abi
                abiService.saveAbiFromAppContract(tbContract);
            }
            // save and update contract
            contractMapper.saveAndUpdate(tbContract);
            // save and update method
            NewMethodInputParam newMethodInputParam = new NewMethodInputParam();
            newMethodInputParam.setGroupId(groupId);
            newMethodInputParam.setMethodList(
                Web3Tools.getMethodFromAbi(tbContractStore.getContractAbi(), cryptoSuite));
            methodService.saveMethod(newMethodInputParam, ContractType.APPIMPORT.getValue());
        }
        // if exist, auto not save (ignore)
        contractPathService.save(groupId, contractPath, listOfContractStore.get(0).getAccount(), true);
    }

    /**
     * update contract.
     */
    private TbContract updateContract(Contract contract) {
        // check contract id
        TbContract tbContract =
            verifyContractIdExist(contract.getContractId(), contract.getGroupId());
        if (tbContract.getContractStatus() == ContractStatus.DEPLOYED.getValue()
            && !constantProperties.isDeployedModifyEnable()) {
            log.info("fail updateContract. deployed contract cannot be modified");
            throw new NodeMgrException(ConstantCode.DEPLOYED_CANNOT_MODIFIED);
        }
        // check contractName
        verifyContractNameNotExist(contract.getGroupId(), contract.getContractPath(),
            contract.getContractName(), contract.getAccount(), contract.getContractId());
        BeanUtils.copyProperties(contract, tbContract);
        // bind contract address
        String address = contract.getContractAddress();
        if (address != null) {
            if (address.length() != CONTRACT_ADDRESS_LENGTH) {
                log.warn("fail updateContract address. inputAddress:{}", address);
                throw new NodeMgrException(ConstantCode.CONTRACT_ADDRESS_INVALID);
            }
            // check address on chain
            abiService.getAddressRuntimeBin(contract.getGroupId(), address);
            log.info("updateContract contract address:{} and deployed status", address);
            tbContract.setContractAddress(address);
            tbContract.setContractStatus(ContractStatus.DEPLOYED.getValue());
            // deploy success, old contract save in tb_abi
            abiService.saveAbiFromContractId(contract.getContractId(), address);
        }
        contractMapper.update(tbContract);
        return tbContract;
    }

    private void handleUpdateAddressAbi(Contract contract, TbContract tbContract) {
        String address = contract.getContractAddress();
        if (address == null) {
            return;
        }
        if (address.length() != CONTRACT_ADDRESS_LENGTH) {
            log.warn("fail sendAbi. inputAddress:{}", address);
            throw new NodeMgrException(ConstantCode.CONTRACT_ADDRESS_INVALID);
        }
        // check address on chain
        abiService.getAddressRuntimeBin(contract.getGroupId(), address);
        log.info("updateContract contract address:{} and deployed status", address);
        tbContract.setContractAddress(address);
        tbContract.setContractStatus(ContractStatus.DEPLOYED.getValue());

    }

    /**
     * delete contract by contractId.
     */
    public void deleteContract(Integer contractId, int groupId) throws NodeMgrException {
        log.debug("start deleteContract contractId:{} groupId:{}", contractId, groupId);
        // check if contract deployed
        if (!constantProperties.isDeployedModifyEnable()) {
             verifyContractNotDeploy(contractId, groupId);
        }
        //remove
        contractMapper.remove(contractId);
        log.debug("end deleteContract");
    }

    /**
     * query contract list.
     */
    public List<TbContract> queryContractList(ContractParam param) throws NodeMgrException {
        log.debug("start queryContractList ContractListParam:{}", JsonTools.toJSONString(param));

        // query contract list
        List<TbContract> listOfContract = contractMapper.listOfContract(param);

        log.debug("end queryContractList listOfContract:{}", JsonTools.toJSONString(listOfContract));
        return listOfContract;
    }

    /**
     * query contract list.
     */
    public List<RspContractNoAbi> queryContractListNoAbi(ContractParam param) throws NodeMgrException {
        log.debug("start queryContractListNoAbi ContractListParam:{}", JsonTools.toJSONString(param));

        // query contract list
        List<TbContract> listOfContract = contractMapper.listOfContract(param);
        List<RspContractNoAbi> resultList = new ArrayList<>();
        listOfContract.forEach(c -> {
            RspContractNoAbi rsp = new RspContractNoAbi();
            BeanUtils.copyProperties(c, rsp);
            resultList.add(rsp);
        });
        log.debug("end queryContractListNoAbi listOfContract:{}", JsonTools.toJSONString(listOfContract));
        return resultList;
    }



    /**
     * query count of contract.
     */
    public int countOfContract(ContractParam param) throws NodeMgrException {
        log.debug("start countOfContract ContractListParam:{}", JsonTools.toJSONString(param));
        try {
            return contractMapper.countOfContract(param);
        } catch (RuntimeException ex) {
            log.error("fail countOfContract", ex);
            throw new NodeMgrException(ConstantCode.DB_EXCEPTION);
        }
    }

    /**
     * query contract by contract id.
     */
    public TbContract queryByContractId(Integer contractId) throws NodeMgrException {
        log.debug("start queryContract contractId:{}", contractId);
        try {
            TbContract contractRow = contractMapper.queryByContractId(contractId);
            log.debug("start queryContract contractId:{} contractRow:{}", contractId,
                JsonTools.toJSONString(contractRow));
            return contractRow;
        } catch (RuntimeException ex) {
            log.error("fail countOfContract", ex);
            throw new NodeMgrException(ConstantCode.DB_EXCEPTION);
        }

    }


    /**
     * query DeployInputParam By Address.
     */
    public List<TbContract> queryContractByBin(Integer groupId, String contractBin)
        throws NodeMgrException {
        try {
            if (StringUtils.isEmpty(contractBin)) {
                return null;
            }
            List<TbContract> contractRow = contractMapper.queryContractByBin(groupId, contractBin);
            log.debug("start queryContractByBin:{}", contractBin, JsonTools.toJSONString(contractRow));
            return contractRow;
        } catch (RuntimeException ex) {
            log.error("fail queryContractByBin", ex);
            throw new NodeMgrException(ConstantCode.DB_EXCEPTION);
        }
    }

    /**
     * deploy contract.
     * v1.5.0 import abi when re-deploy same contract
     */
    public TbContract deployContract(DeployInputParam inputParam) throws NodeMgrException {
        log.info("start deployContract. inputParam:{}", JsonTools.toJSONString(inputParam));
        int groupId = inputParam.getGroupId();

        // check deploy permission
        checkDeployPermission(groupId, inputParam.getUser());

        String contractName = inputParam.getContractName();
        // check contract
        TbContract contractRecord =
            verifyContractIdExist(inputParam.getContractId(), inputParam.getGroupId());
        if (contractRecord.getContractStatus() == ContractStatus.DEPLOYED.getValue()
            && !constantProperties.isDeployedModifyEnable()) {
            log.info("fail deployContract. deployed contract cannot be modified");
            throw new NodeMgrException(ConstantCode.DEPLOYED_CANNOT_MODIFIED);
        }
        //check contractName
        verifyContractNameNotExist(inputParam.getGroupId(), inputParam.getContractPath(),
            inputParam.getContractName(), inputParam.getAccount(), inputParam.getContractId());

//        List<ABIDefinition> abiArray = JsonTools.toJavaObjectList(inputParam.getContractAbi(), ABIDefinition.class);
        List<Object> abiArray = JsonTools.toJavaObjectList(inputParam.getContractAbi(), Object.class);
        if (abiArray == null || abiArray.isEmpty()) {
            log.info("fail deployContract. abi is empty");
            throw new NodeMgrException(ConstantCode.CONTRACT_ABI_EMPTY);
        }

        // deploy param
        // get signUserId
        String signUserId = userService.getSignUserIdByAddress(groupId, inputParam.getUser());

        Map<String, Object> params = new HashMap<>();
        params.put("groupId", groupId);
        params.put("signUserId", signUserId);
        params.put("contractName", contractName);
        // params.put("version", version);
        params.put("abiInfo", abiArray);
        params.put("bytecodeBin", inputParam.getBytecodeBin());
        params.put("funcParam", inputParam.getConstructorParams());

        //deploy
        String contractAddress = frontRestTools.postForEntity(groupId,
            FrontRestTools.URI_CONTRACT_DEPLOY_WITH_SIGN, params, String.class);
        if (StringUtils.isBlank(contractAddress) || Address.DEFAULT.getValue().equals(contractAddress)) {
            log.error("fail deploy, contractAddress is empty");
            throw new NodeMgrException(ConstantCode.CONTRACT_DEPLOY_FAIL);
        }
        // deploy success, old contract save in tb_abi
        abiService.saveAbiFromContractId(inputParam.getContractId(), contractAddress);

        // get deploy user name
        String userName = userService.getUserNameByAddress(groupId, inputParam.getUser());
        //save contract
        TbContract tbContract = new TbContract();
        BeanUtils.copyProperties(inputParam, tbContract);
        tbContract.setDeployAddress(inputParam.getUser());
        tbContract.setDeployUserName(userName);
        tbContract.setContractAddress(contractAddress);
        tbContract.setContractStatus(ContractStatus.DEPLOYED.getValue());
        //tbContract.setContractVersion(version);
        tbContract.setDeployTime(LocalDateTime.now());
        contractMapper.update(tbContract);

        log.debug("end deployContract. contractId:{} groupId:{} contractAddress:{}",
            tbContract.getContractId(), groupId, contractAddress);
        return tbContract;
    }

    /**
     * query contract info.
     */
    public TbContract queryContract(ContractParam queryParam) {
        log.debug("start queryContract. queryParam:{}", JsonTools.toJSONString(queryParam));
        TbContract tbContract = contractMapper.queryContract(queryParam);
        log.debug("end queryContract. queryParam:{} tbContract:{}", JsonTools.toJSONString(queryParam),
            JsonTools.toJSONString(tbContract));
        return tbContract;
    }


    /**
     * send transaction.
     */
    public Object sendTransaction(TransactionInputParam param) throws NodeMgrException {
        log.debug("start sendTransaction. param:{}", JsonTools.toJSONString(param));

        if (Objects.isNull(param)) {
            log.info("fail sendTransaction. request param is null");
            throw new NodeMgrException(ConstantCode.INVALID_PARAM_INFO);
        }

        // check contractId
        String contractAbiStr = "";
        if (param.getContractId() != null) {
            // get abi by contract
            TbContract contract = verifyContractIdExist(param.getContractId(), param.getGroupId());
            contractAbiStr = contract.getContractAbi();
            //send abi to front
            sendAbi(param.getGroupId(), param.getContractId(), param.getContractAddress());
            //check contract deploy
            verifyContractDeploy(param.getContractId(), param.getGroupId());
        } else {
            // send tx by TABLE abi
            // get from db and it's deployed
            AbiInfo abiInfo = abiService.getAbiByGroupIdAndAddress(param.getGroupId(), param.getContractAddress());
            contractAbiStr = abiInfo.getContractAbi();
        }

        // if constant, signUserId is useless
        ABIDefinition funcAbi = Web3Tools.getAbiDefinition(param.getFuncName(), contractAbiStr);
        String signUserId = "empty";
        // func is not constant or stateMutability is not equal to 'view' or 'pure'
        // fit in solidity 0.6
        boolean isConstant = (STATE_MUTABILITY_VIEW.equals(funcAbi.getStateMutability()) ||
            STATE_MUTABILITY_PURE.equals(funcAbi.getStateMutability()));
        if (!isConstant) {
            // !funcAbi.isConstant()
            signUserId = userService.getSignUserIdByAddress(param.getGroupId(), param.getUser());
        }

        //send transaction
        TransactionParam transParam = new TransactionParam();
        BeanUtils.copyProperties(param, transParam);
        transParam.setSignUserId(signUserId);
        Object frontRsp = frontRestTools
            .postForEntity(param.getGroupId(), FrontRestTools.URI_SEND_TRANSACTION_WITH_SIGN, transParam,
                Object.class);
        log.debug("end sendTransaction. frontRsp:{}", JsonTools.toJSONString(frontRsp));
        return frontRsp;
    }


    /**
     * verify that the contract does not exist.
     */
    private void verifyContractNotExist(int groupId, String name, String path, String account) {
        ContractParam param = new ContractParam(groupId, path, name, account);
        TbContract contract = queryContract(param);
        if (Objects.nonNull(contract)) {
            log.warn("contract is exist. groupId:{} name:{} path:{}", groupId, name, path);
            throw new NodeMgrException(ConstantCode.CONTRACT_EXISTS);
        }
    }

    /**
     * verify that the contract had not deployed.
     */
    private TbContract verifyContractNotDeploy(int contractId, int groupId) {
        TbContract contract = verifyContractIdExist(contractId, groupId);
        if (ContractStatus.DEPLOYED.getValue() == contract.getContractStatus()) {
            log.info("contract had bean deployed contractId:{}", contractId);
            throw new NodeMgrException(ConstantCode.CONTRACT_HAS_BEAN_DEPLOYED);
        }
        return contract;
    }

    /**
     * verify that the contract had bean deployed.
     */
    private TbContract verifyContractDeploy(int contractId, int groupId) {
        TbContract contract = verifyContractIdExist(contractId, groupId);
        if (ContractStatus.DEPLOYED.getValue() != contract.getContractStatus()) {
            log.info("contract had bean deployed contractId:{}", contractId);
            throw new NodeMgrException(ConstantCode.CONTRACT_NOT_DEPLOY);
        }
        return contract;
    }

    /**
     * verify that the contractId is exist.
     */
    private TbContract verifyContractIdExist(int contractId, int groupId) {
        ContractParam param = new ContractParam(contractId, groupId);
        TbContract contract = queryContract(param);
        if (Objects.isNull(contract)) {
            log.info("contractId is invalid. contractId:{}", contractId);
            throw new NodeMgrException(ConstantCode.INVALID_CONTRACT_ID);
        }
        return contract;
    }

    /**
     * contract name can not be repeated.
     */
    private void verifyContractNameNotExist(int groupId, String path, String name, String account, int contractId) {
        ContractParam param = new ContractParam(groupId, path, name, account);
        TbContract localContract = queryContract(param);
        if (Objects.isNull(localContract)) {
            return;
        }
        if (contractId != localContract.getContractId()) {
            throw new NodeMgrException(ConstantCode.CONTRACT_NAME_REPEAT);
        }
    }


    /**
     * delete by groupId
     */
    public void deleteByGroupId(int groupId) {
        if (groupId == 0) {
            return;
        }
        log.info("delete contract by groupId");
        contractMapper.removeByGroupId(groupId);
        log.info("delete contract path by groupId");
        contractPathService.removeByGroupId(groupId);
    }


    /**
     * send abi.
     */
    public void sendAbi(int groupId, int contractId, String address) {
        log.info("start sendAbi, groupId:{} contractId:{} address:{}", groupId, contractId,
            address);
        TbContract contract = verifyContractIdExist(contractId, groupId);
        String localAddress = contract.getContractAddress();
        String abiInfo = contract.getContractAbi();
        if (StringUtils.isBlank(address)) {
            log.warn("ignore sendAbi. inputAddress is empty");
            return;
        }
        if (StringUtils.isBlank(abiInfo)) {
            log.warn("ignore sendAbi. abiInfo is empty");
            return;
        }
        if (address.equals(localAddress)) {
            log.info("ignore sendAbi. inputAddress:{} localAddress:{}", address, localAddress);
            return;
        }
        if (address.length() != CONTRACT_ADDRESS_LENGTH) {
            log.warn("fail sendAbi. inputAddress:{}", address);
            throw new NodeMgrException(ConstantCode.CONTRACT_ADDRESS_INVALID);
        }
        //send abi
        PostAbiInfo param = new PostAbiInfo();
        param.setGroupId(groupId);
        param.setContractName(contract.getContractName());
        param.setAddress(address);
        param.setAbiInfo(JsonTools.toJavaObjectList(abiInfo, ABIDefinition.class));
        param.setContractBin(contract.getContractBin());

        frontInterface.sendAbi(groupId, param);

        //save address
        if (StringUtils.isBlank(contract.getContractAddress())) {
            contract.setContractAddress(address);
            contract.setContractStatus(ContractStatus.DEPLOYED.getValue());
        }

        contract.setDeployTime(LocalDateTime.now());
        contract.setDescription("address add by sendAbi");
        contractMapper.update(contract);
    }

    /**
     * check user deploy permission
     */
    private void checkDeployPermission(int groupId, String userAddress) {
        // get deploy permission list
        List<PermissionInfo> deployUserList = new ArrayList<>();
        BasePageResponse response = permissionManageService.listPermissionFull(groupId, PERMISSION_TYPE_DEPLOY_AND_CREATE, null);
        if (response.getCode() != 0) {
            log.error("checkDeployPermission get permission list error");
            return;
        } else {
            List listData = (List) response.getData();
            deployUserList = JsonTools.toJavaObjectList(JsonTools.toJSONString(listData), PermissionInfo.class);
        }

        // check user in the list
        if (deployUserList == null || deployUserList.isEmpty()) {
            return;
        } else {
            long count = 0;
            count = deployUserList.stream().filter( admin -> userAddress.equals(admin.getAddress())).count();
            // if not in the list, permission denied
            if (count == 0) {
                log.error("checkDeployPermission permission denied for user:{}", userAddress);
                throw new NodeMgrException(ConstantCode.PERMISSION_DENIED_ON_CHAIN);
            }
        }
    }

    /**
     * get contract path list
     */
    public List<TbContractPath> queryContractPathList(Integer groupId, String account) {
        List<TbContractPath> pathList = contractPathService.listContractPath(groupId, account);
        // not return null, but return empty list
        List<TbContractPath> resultList = new ArrayList<>();
        if (pathList != null) {
            resultList.addAll(pathList);
        }
        return resultList;
    }

    public void deleteByContractPath(ContractPathParam param, CurrentAccountInfo currentAccountInfo) {
        log.debug("start deleteByContractPath ContractPathParam:{}", JsonTools.toJSONString(param));
        // check developer
        if (RoleType.DEVELOPER.getValue().intValue() == currentAccountInfo.getRoleId().intValue()
                && !contractPathService.checkPathExist(param.getGroupId(), param.getContractPath(),
                        currentAccountInfo.getAccount())) {
            log.error("end deleteByContractPath. contract path not exists.");
            throw new NodeMgrException(ConstantCode.CONTRACT_PATH_NOT_EXISTS);
        }
        
        ContractParam listParam = new ContractParam();
        BeanUtils.copyProperties(param, listParam);
        List<TbContract> contractList = contractMapper.listOfContract(listParam);
        if (contractList == null || contractList.isEmpty()) {
            log.debug("deleteByContractPath contract list empty, direct delete path");
            contractPathService.removeByPathName(param);
            return;
        }
        // batch delete contract by path
        if (constantProperties.isDeployedModifyEnable()) {
            contractList.forEach( c -> deleteContract(c.getContractId(), c.getGroupId()));
        } else {
            Collection<TbContract> unDeployedList = contractList.stream()
                    .filter( contract -> ContractStatus.DEPLOYED.getValue() != contract.getContractStatus())
                    .collect(Collectors.toList());
            // unDeployed's size == list's size, list is all unDeployed
            if (unDeployedList.size() == contractList.size()) {
                log.debug("deleteByContractPath delete contract in path");
                unDeployedList.forEach( c -> deleteContract(c.getContractId(), c.getGroupId()));
            } else {
                log.error("end deleteByContractPath for contain deployed contract");
                throw new NodeMgrException(ConstantCode.CONTRACT_PATH_CONTAIN_DEPLOYED);
            }
        }
        log.debug("deleteByContractPath delete path");
        contractPathService.removeByPathName(param);
        log.debug("end deleteByContractPath. ");

    }

    /**
     * query contract list by multi path
     */
    public List<TbContract> queryContractListMultiPath(ReqListContract param) throws NodeMgrException {
        log.debug("start queryContractListMultiPath ReqListContract:{}", JsonTools.toJSONString(param));
        int groupId = param.getGroupId();
        String account = param.getAccount();
        List<String> pathList = param.getContractPathList();

        List<TbContract> resultList = new ArrayList<>();
        for (String path: pathList) {
            // query contract list
            ContractParam listParam = new ContractParam(groupId, account, path);
            List<TbContract> listOfContract = contractMapper.listOfContract(listParam);
            resultList.addAll(listOfContract);
        }

        log.debug("end queryContractListMultiPath listOfContract size:{}", resultList.size());
        return resultList;
    }

    /**
     * copy contracts source from contract warehouse
     * @param reqCopyContracts
     */
    public void copyContracts(ReqCopyContracts reqCopyContracts) {
        log.debug("start saveContractBatch ReqContractList:{}",
            JsonTools.toJSONString(reqCopyContracts));
        if ("".equals(reqCopyContracts.getContractPath())) {
            reqCopyContracts.setContractPath("/");
        }
        reqCopyContracts.getContractItems().forEach(c -> {
            Contract reqContractSave = new Contract(reqCopyContracts.getGroupId(), "",
                reqCopyContracts.getContractPath(), reqCopyContracts.getAccount());
            reqContractSave.setContractName(c.getContractName());
            reqContractSave.setContractSource(c.getContractSource());
            this.newContract(reqContractSave);
        });
    }

}
