/**
 * Copyright 2014-2021 the original author or authors.
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
package com.webank.webase.node.mgr.user;

import com.webank.webase.node.mgr.account.AccountService;
import com.webank.webase.node.mgr.account.entity.TbAccountInfo;
import com.webank.webase.node.mgr.base.code.ConstantCode;
import com.webank.webase.node.mgr.base.enums.CheckUserExist;
import com.webank.webase.node.mgr.base.enums.HasPk;
import com.webank.webase.node.mgr.base.enums.ReturnPrivateKey;
import com.webank.webase.node.mgr.base.enums.RoleType;
import com.webank.webase.node.mgr.base.enums.UserType;
import com.webank.webase.node.mgr.base.exception.NodeMgrException;
import com.webank.webase.node.mgr.base.properties.ConstantProperties;
import com.webank.webase.node.mgr.base.tools.HttpRequestTools;
import com.webank.webase.node.mgr.base.tools.JsonTools;
import com.webank.webase.node.mgr.base.tools.NodeMgrTools;
import com.webank.webase.node.mgr.cert.entity.FileContentHandle;
import com.webank.webase.node.mgr.frontinterface.FrontRestTools;
import com.webank.webase.node.mgr.group.GroupService;
import com.webank.webase.node.mgr.monitor.MonitorService;
import com.webank.webase.node.mgr.user.entity.BindUserInputParam;
import com.webank.webase.node.mgr.user.entity.KeyPair;
import com.webank.webase.node.mgr.user.entity.ReqImportPem;
import com.webank.webase.node.mgr.user.entity.TbUser;
import com.webank.webase.node.mgr.user.entity.UpdateUserInputParam;
import com.webank.webase.node.mgr.user.entity.UserParam;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.fisco.bcos.sdk.crypto.CryptoSuite;
import org.fisco.bcos.sdk.crypto.exceptions.LoadKeyStoreException;
import org.fisco.bcos.sdk.crypto.keypair.CryptoKeyPair;
import org.fisco.bcos.sdk.crypto.keystore.KeyTool;
import org.fisco.bcos.sdk.crypto.keystore.P12KeyStore;
import org.fisco.bcos.sdk.crypto.keystore.PEMKeyStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

/**
 * services for user data.
 */
@Log4j2
@Service
public class UserService {

    @Autowired
    private AccountService accountService;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private GroupService groupService;
    @Autowired
    private FrontRestTools frontRestTools;
    @Lazy
    @Autowired
    private MonitorService monitorService;
    @Autowired
    private CryptoSuite cryptoSuite;
    private final static String PEM_FILE_FORMAT = ".pem";
    private final static String P12_FILE_FORMAT = ".p12";

    /**
     * save key pair locally and not in integrated app
     * @param privateKeyEncoded in base64
     * @return
     * @throws NodeMgrException
     */
    @Transactional
    public TbUser addUserInfoLocal(Integer groupId, String userName, String account, String description,
        Integer userType, String privateKeyEncoded) throws NodeMgrException {
        return addUserInfo(groupId, userName, account, description, userType,
            privateKeyEncoded, ReturnPrivateKey.FALSE.getValue(), CheckUserExist.TURE.getValue());
    }

    /**
     * add new user data.
     */
    @Transactional
    public TbUser addUserInfo(Integer groupId, String userName, String account, String description,
            Integer userType, String privateKeyEncoded, boolean returnPrivateKey, 
            boolean isCheckExist) throws NodeMgrException {
        log.debug("start addUserInfo groupId:{},userName:{},description:{},userType:{},", groupId,
                userName, description, userType);
        // check group id
        groupService.checkGroupId(groupId);
        // check account
        accountService.accountExist(account);

        // check userName and account
        TbUser checkUserNameRow;
        TbAccountInfo accountRow = accountService.queryByAccount(account);
        if (RoleType.ADMIN.getValue().equals(accountRow.getRoleId())) {
            checkUserNameRow = queryByName(groupId, userName, null);
        } else {
            userName = account + "_" + userName;
            checkUserNameRow = queryByName(groupId, userName, account);
        }
        if (checkUserNameRow != null) {
            if (!isCheckExist) {
                return checkUserNameRow;
            }
            log.warn("fail addUserInfo. user info already exists");
            throw new NodeMgrException(ConstantCode.USER_EXISTS);
        }


        // add user by webase-front->webase-sign
        String signUserId = UUID.randomUUID().toString().replaceAll("-", "");
        // group id as appId
        String appId = groupId.toString();

        // request sign or not
        KeyPair keyPair;
        // import key
        if (StringUtils.isNotBlank(privateKeyEncoded)) {
            // check user address if import private key
            TbUser checkAddressRow = queryUser(null, groupId, null,
                getAddressFromPrivateKeyEncoded(privateKeyEncoded), null);
            if (Objects.nonNull(checkAddressRow)) {
                if (!isCheckExist) {
                    return checkAddressRow;
                }
                log.warn("fail addUserInfo. address is already exists");
                throw new NodeMgrException(ConstantCode.USER_EXISTS);
            }
            // import key pair
            Map<String, Object> param = new HashMap<>();
            param.put("signUserId", signUserId);
            param.put("appId", appId);
            // already encoded privateKey
            param.put("privateKey", privateKeyEncoded);
            keyPair = frontRestTools.postForEntity(groupId,
                    FrontRestTools.URI_KEY_PAIR_IMPORT_WITH_SIGN, param, KeyPair.class);
        } else {
            // not import, but new key pair
            Map<String, String> param = new HashMap<>();
            // for front, its type is 2-external account
            param.put("type", "2");
            param.put("userName", userName);
            param.put("signUserId", signUserId);
            param.put("appId", appId);
            param.put("returnPrivateKey", String.valueOf(returnPrivateKey));
            String uri = HttpRequestTools.getQueryUri(FrontRestTools.URI_KEY_PAIR, param);
            keyPair = frontRestTools.getForEntity(groupId, uri, KeyPair.class);
            privateKeyEncoded = keyPair.getPrivateKey();
        }

        String publicKey = Optional.ofNullable(keyPair).map(KeyPair::getPublicKey).orElse(null);
        String address = Optional.ofNullable(keyPair).map(KeyPair::getAddress).orElse(null);

        if (StringUtils.isAnyBlank(publicKey, address)) {
            log.warn("get key pair fail. publicKey:{} address:{}", publicKey, address);
            throw new NodeMgrException(ConstantCode.SYSTEM_EXCEPTION_GET_PRIVATE_KEY_FAIL);
        }

        // check address after sign return
        TbUser checkAddressRow = queryUser(null, groupId, null, address, null);
        if (Objects.nonNull(checkAddressRow)) {
            if (!isCheckExist) {
                return checkAddressRow;
            }
            log.warn("fail addUserInfo. address is already exists");
            throw new NodeMgrException(ConstantCode.USER_EXISTS);
        }
        // add row
        TbUser newUserRow = new TbUser(HasPk.HAS.getValue(), userType, userName, account, groupId,
                address, publicKey, description);
        newUserRow.setSignUserId(signUserId);
        newUserRow.setAppId(appId);
        Integer affectRow = userMapper.addUserRow(newUserRow);
        if (affectRow == 0) {
            log.warn("affect 0 rows of tb_user");
            throw new NodeMgrException(ConstantCode.DB_EXCEPTION);
        }

        // update monitor unusual user's info
        monitorService.updateUnusualUser(groupId, userName, address);

        checkUserNameRow = queryByUserId(newUserRow.getUserId());
        // if return privateKey
        if (returnPrivateKey) {
            checkUserNameRow.setPrivateKey(privateKeyEncoded);
        }
        return checkUserNameRow;
    }

    /**
     * bind user info.
     */
    @Transactional
    public TbUser bindUserInfo(BindUserInputParam user, String account, boolean isCheckExist) throws NodeMgrException {
        log.debug("start bindUserInfo User:{}", JsonTools.toJSONString(user));

        String publicKey = user.getPublicKey();
        if (StringUtils.isBlank(publicKey)) {
            log.info("fail bindUserInfo. publicKey cannot be empty");
            throw new NodeMgrException(ConstantCode.PUBLICKEY_NULL);
        }

        if (publicKey.length() != ConstantProperties.PUBLICKEY_LENGTH
                && publicKey.length() != ConstantProperties.ADDRESS_LENGTH) {
            log.info("fail bindUserInfo. publicKey length error");
            throw new NodeMgrException(ConstantCode.PUBLICKEY_LENGTH_ERROR);
        }

        // check group id
        groupService.checkGroupId(user.getGroupId());
        // check account
        accountService.accountExist(account);

        // check userName
        TbUser userRow = queryByName(user.getGroupId(), user.getUserName(), account);
        if (Objects.nonNull(userRow)) {
            if (!isCheckExist) {
                return userRow;
            }
            log.warn("fail bindUserInfo. userName is already exists");
            throw new NodeMgrException(ConstantCode.USER_EXISTS);
        }
        String address = publicKey;
        if (publicKey.length() == ConstantProperties.PUBLICKEY_LENGTH) {
            address = cryptoSuite.createKeyPair().getAddress(publicKey);
        }

        // check address
        TbUser addressRow = queryUser(null, user.getGroupId(), null, address, account);
        if (Objects.nonNull(addressRow)) {
            if (!isCheckExist) {
                return addressRow;
            }
            log.warn("fail bindUserInfo. address is already exists");
            throw new NodeMgrException(ConstantCode.USER_EXISTS);
        }

        // add row
        TbUser newUserRow = new TbUser(HasPk.NONE.getValue(), user.getUserType(),
                user.getUserName(), account, user.getGroupId(), address, publicKey,
                user.getDescription());
        Integer affectRow = userMapper.addUserRow(newUserRow);
        if (affectRow == 0) {
            log.warn("bindUserInfo affect 0 rows of tb_user");
            throw new NodeMgrException(ConstantCode.DB_EXCEPTION);
        }

        Integer userId = newUserRow.getUserId();

        // update monitor unusual user's info
        monitorService.updateUnusualUser(user.getGroupId(), user.getUserName(), address);

        log.debug("end bindUserInfo userId:{}", userId);
        return queryByUserId(userId);
    }

    /**
     * query count of user.
     */
    public Integer countOfUser(UserParam userParam) throws NodeMgrException {
        log.debug("start countOfUser. userParam:{}", JsonTools.toJSONString(userParam));

        try {
            Integer count = userMapper.countOfUser(userParam);
            log.debug("end countOfUser userParam:{} count:{}", JsonTools.toJSONString(userParam),
                    count);
            return count;
        } catch (RuntimeException ex) {
            log.error("fail countOfUser userParam:{}", JsonTools.toJSONString(userParam), ex);
            throw new NodeMgrException(ConstantCode.DB_EXCEPTION);
        }
    }

    /**
     * query user list by page.
     */
    public List<TbUser> queryUserList(UserParam userParam) throws NodeMgrException {
        log.debug("start queryUserList userParam:{}", JsonTools.toJSONString(userParam));
        // query user list
        List<TbUser> listOfUser = userMapper.listOfUser(userParam);
        log.debug("end queryUserList listOfUser:{}", JsonTools.toJSONString(listOfUser));
        return listOfUser;
    }
    
    /**
     * query user detail(private key from sign).
     */
    public TbUser queryUserDetail(Integer userId) throws NodeMgrException {
        // query user info
        TbUser user = queryByUserId(userId);
        if (user == null) {
            throw new NodeMgrException(ConstantCode.USER_NOT_EXIST);
        }
        KeyPair keyPair = this.getUserKeyPairFromSign(user.getGroupId(), user.getSignUserId());
        // encode privateKey
        user.setPrivateKey(keyPair.getPrivateKey());
        return user;
    }

    public String queryUserDetail(int groupId, String userAddress) throws NodeMgrException {
        // query sign user id
        String signUserId = getSignUserIdByAddress(groupId, userAddress);
        // get key from sign
        KeyPair keyPair = this.getUserKeyPairFromSign(groupId, signUserId);
        // decode key
        String privateKeyRaw = new String(Base64.getDecoder().decode(keyPair.getPrivateKey()));
        return privateKeyRaw;
    }

    /**
     * return key pair with private key encoded in base64
     * @param groupId
     * @param signUserId
     * @return the private key of KeyPair is encoded in base64
     */
    private KeyPair getUserKeyPairFromSign(int groupId, String signUserId) {
        Map<String, String> param = new HashMap<>();
        param.put("signUserId", signUserId);
        param.put("returnPrivateKey", "true");
        String uri = HttpRequestTools.getQueryUri(FrontRestTools.URI_KEY_PAIR_USERINFO_WITH_SIGN, param);
        KeyPair keyPair = frontRestTools.getForEntity(groupId, uri, KeyPair.class);
        return keyPair;
    }

    /**
     * query user row.
     */
    public TbUser queryUser(Integer userId, Integer groupId, String userName, String address,
            String account) throws NodeMgrException {
        log.debug("start queryUser userId:{} groupId:{} userName:{} address:{}", userId, groupId,
                userName, address);
        try {
            TbUser userRow = userMapper.queryUser(userId, groupId, userName, address, account);
            log.debug("end queryUser userId:{} groupId:{} userName:{}  address:{} TbUser:{}",
                    userId, groupId, userName, address, JsonTools.toJSONString(userRow));
            return userRow;
        } catch (RuntimeException ex) {
            log.error("fail queryUser userId:{} groupId:{} userName:{}  address:{}", userId,
                    groupId, userName, address, ex);
            throw new NodeMgrException(ConstantCode.DB_EXCEPTION);
        }
    }

    /**
     * query by groupId、userName.
     */
    public TbUser queryUser(Integer groupId, String userName) throws NodeMgrException {
        return queryUser(null, groupId, userName, null, null);
    }

    /**
     * query by userName.
     */
    public TbUser queryByName(int groupId, String userName, String account)
            throws NodeMgrException {
        return queryUser(null, groupId, userName, null, account);
    }


    /**
     * query by userId.
     */
    public TbUser queryByUserId(Integer userId) throws NodeMgrException {
        return queryUser(userId, null, null, null, null);
    }

    /**
     * query by group id and address.
     */
    public String getSignUserIdByAddress(int groupId, String address) throws NodeMgrException {
        TbUser user = queryUser(null, groupId, null, address, null);
        if (user == null) {
            throw new NodeMgrException(ConstantCode.USER_SIGN_USER_ID_NOT_EXIST);
        }
        if (StringUtils.isBlank(user.getSignUserId())) {
            log.error("getSignUserIdByAddress userAddress's signUserId not exist, address:{}",
                address);
            throw new NodeMgrException(ConstantCode.USER_SIGN_USER_ID_NOT_EXIST);
        }
        return user.getSignUserId();
    }

    public String getUserNameByAddress(int groupId, String address) throws NodeMgrException {
        TbUser user = queryUser(null, groupId, null, address, null);
        if (user == null) {
            throw new NodeMgrException(ConstantCode.USER_NOT_EXIST);
        }
        return user.getUserName();
    }

    /**
     * update user info.
     */
    public void updateUser(UpdateUserInputParam user) throws NodeMgrException {
        TbUser tbUser = queryByUserId(user.getUserId());
        tbUser.setDescription(user.getDescription());
        updateUser(tbUser);
    }

    /**
     * update user info.
     */
    public void updateUser(TbUser user) throws NodeMgrException {
        log.debug("start updateUser user", JsonTools.toJSONString(user));
        Integer userId = Optional.ofNullable(user).map(u -> u.getUserId()).orElse(null);
        String description = Optional.ofNullable(user).map(u -> u.getDescription()).orElse(null);
        if (userId == null) {
            log.warn("fail updateUser. user id is null");
            throw new NodeMgrException(ConstantCode.USER_ID_NULL);
        }
        TbUser tbUser = queryByUserId(userId);
        tbUser.setDescription(description);

        try {
            Integer affectRow = userMapper.updateUser(tbUser);
            if (affectRow == 0) {
                log.warn("affect 0 rows of tb_user");
                throw new NodeMgrException(ConstantCode.DB_EXCEPTION);
            }
        } catch (RuntimeException ex) {
            log.error("fail updateUser  userId:{} description:{}", userId, description, ex);
            throw new NodeMgrException(ConstantCode.DB_EXCEPTION);
        }

        log.debug("end updateOrtanization");
    }


    /**
     * get user name by address.
     */
    public String queryUserNameByAddress(Integer groupId, String address) throws NodeMgrException {
        log.debug("queryUserNameByAddress address:{} ", address);
        String userName = userMapper.queryUserNameByAddress(groupId, address);
        log.debug("end queryUserNameByAddress");
        return userName;
    }

    public void deleteByAddress(String address) throws NodeMgrException {
        log.debug("deleteByAddress address:{} ", address);
        userMapper.deleteByAddress(address);
        log.debug("end deleteByAddress");
    }

    /**
     * import pem file to import privateKey
     * 
     * @param reqImportPem
     * @return userId
     */
    @Transactional
    public TbUser importPem(ReqImportPem reqImportPem, boolean isCheckExist) {
        PEMKeyStore pemManager = new PEMKeyStore(new ByteArrayInputStream(reqImportPem.getPemContent().getBytes()));
        String privateKey = KeyTool.getHexedPrivateKey(pemManager.getKeyPair().getPrivate());
        // pem's privateKey encoded here
        String privateKeyEncoded = NodeMgrTools.encodedBase64Str(privateKey);

        // store local and save in sign
        TbUser tbUser = addUserInfo(reqImportPem.getGroupId(), reqImportPem.getUserName(),
                reqImportPem.getAccount(), reqImportPem.getDescription(),
                reqImportPem.getUserType(), privateKeyEncoded, ReturnPrivateKey.FALSE.getValue(), isCheckExist);
        return tbUser;
    }

    /**
     * import keystore info from p12 file input stream and its password
     * 
     * @param p12File
     * @param p12PasswordEncoded
     * @param userName
     * @return KeyStoreInfo
     */
    @Transactional
    public TbUser importKeyStoreFromP12(MultipartFile p12File, String p12PasswordEncoded, Integer groupId,
            String userName, String account, String description, boolean isCheckExist) {
        // decode p12 password
        String p12Password;
        try{
            p12Password = new String(Base64.getDecoder().decode(p12PasswordEncoded));
        } catch (Exception e) {
            log.error("decode pwd error:[]", e);
            throw new NodeMgrException(ConstantCode.PRIVATE_KEY_DECODE_FAIL);
        }
        String privateKey;
        try {
            // manually set password and load
            P12KeyStore p12Manager = new P12KeyStore(p12File.getInputStream(), p12Password);
            privateKey = KeyTool.getHexedPrivateKey(p12Manager.getKeyPair().getPrivate());
        }  catch (IOException e) {
            log.error("importKeyStoreFromP12 file not found error:[]", e);
            throw new NodeMgrException(ConstantCode.P12_FILE_ERROR);
        } catch (LoadKeyStoreException e) {
            log.error("importKeyStoreFromP12 error:[]", e);
            if (e.getMessage().contains("password")) {
                throw new NodeMgrException(ConstantCode.P12_PASSWORD_ERROR);
            }
            throw new NodeMgrException(ConstantCode.P12_FILE_ERROR);
        }

        // pem's privateKey encoded here
        String privateKeyEncoded = NodeMgrTools.encodedBase64Str(privateKey);

        // store local and save in sign
        TbUser tbUser = addUserInfo(groupId, userName, account, description,
                UserType.GENERALUSER.getValue(), privateKeyEncoded, 
                ReturnPrivateKey.FALSE.getValue(), isCheckExist);

        return tbUser;
    }

    /**
     * get pem file exported from sign from front api
     * @return ResponseEntity<InputStreamResource>
     */
    public FileContentHandle exportPemFromSign(int groupId, String signUserId, String account,Integer roleId) {
        log.debug("start getExportPemFromSign signUserId:{}", signUserId);
        TbUser user = queryUser(groupId,null,null,null,account);
        if(roleId == RoleType.DEVELOPER.getValue()){
            if(user.getSignUserId() != signUserId){
                throw new NodeMgrException(ConstantCode.PRIVATE_KEY_NOT_EXISTS);
            }
        }
        KeyPair keyPair = getUserKeyPairFromSign(groupId, signUserId);
        String decodedPrivateKey = new String(Base64.getDecoder().decode(keyPair.getPrivateKey()));
        keyPair.setPrivateKey(decodedPrivateKey);
        String filePath = NodeMgrTools.writePrivateKeyPem(keyPair.getPrivateKey(),
            keyPair.getAddress(), keyPair.getUserName(), cryptoSuite);
        try {
            log.debug("end getExportPemFromSign, filePath:{}", filePath);
            return new FileContentHandle(keyPair.getAddress() + PEM_FILE_FORMAT,
                new FileInputStream(filePath));
        } catch (IOException e) {
            log.error("exportPrivateKeyPem fail:[]", e);
            throw new NodeMgrException(ConstantCode.WRITE_PRIVATE_KEY_CRT_KEY_FILE_FAIL);
        }

    }

    /**
     * get p12 file exported from sign from front api
     * @param p12PasswordEncoded password of p12 key in base64 format
     * @return ResponseEntity<InputStreamResource>
     */
    public FileContentHandle exportP12FromSign(int groupId, String signUserId, String p12PasswordEncoded) {
        log.debug("start getExportP12FromSign signUserId:{}", signUserId);
        // decode p12 password
        String p12Password;
        try {
            p12Password = new String(Base64.getDecoder().decode(p12PasswordEncoded));
        } catch (Exception e) {
            log.error("decode password error:[]", e);
            throw new NodeMgrException(ConstantCode.P12_PASSWORD_ERROR);
        }

        KeyPair keyPair = getUserKeyPairFromSign(groupId, signUserId);
        String decodedPrivateKey = new String(Base64.getDecoder().decode(keyPair.getPrivateKey()));
        keyPair.setPrivateKey(decodedPrivateKey);
        String filePath = NodeMgrTools.writePrivateKeyP12(p12Password, keyPair.getPrivateKey(),
            keyPair.getAddress(), keyPair.getUserName(), cryptoSuite);
        try {
            log.debug("end getExportP12FromSign, filePath:{}", filePath);
            return new FileContentHandle(keyPair.getAddress() + P12_FILE_FORMAT,
                new FileInputStream(filePath));
        } catch (IOException e) {
            log.error("exportPrivateKeyPem fail:[]", e);
            throw new NodeMgrException(ConstantCode.WRITE_PRIVATE_KEY_CRT_KEY_FILE_FAIL);
        }
    }

    private String getAddressFromPrivateKeyEncoded(String privateKeyEncoded) {
        String hexPrivateKey = new String(Base64.getDecoder().decode(privateKeyEncoded.getBytes()));
        CryptoKeyPair cryptoKeyPair = cryptoSuite.createKeyPair(hexPrivateKey);
        return cryptoKeyPair.getAddress();
    }
}
