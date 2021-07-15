/**
 * Copyright 2014-2021  the original author or authors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.webank.webase.node.mgr.base.code;

/**
 * A-BB-CCC A:error level. <br/>
 * 1:system exception <br/>
 * 2:business exception <br/>
 * B:project number <br/>
 * WeBASE-Node-Manager:02 <br/>
 * C: error code <br/>
 */
public class ConstantCode {

    /* return success */
    public static final RetCode SUCCESS = RetCode.mark(0, "success");

    /* system exception */
    public static final RetCode SYSTEM_EXCEPTION = RetCode.mark(102000, "system exception");
    public static final RetCode SYSTEM_EXCEPTION_GET_PRIVATE_KEY_FAIL = RetCode.mark(102001, "system exception: please check front");
    public static final RetCode SYSTEM_ERROR_GROUP_LIST_EMPTY = RetCode.mark(102002, "No group belongs to this groupId(node not belongs to this group)");
    public static final RetCode WEBASE_VERSION_NOT_MATCH_FISCO_SUPPORT_VERSION = RetCode.mark(102003, "When webase is v1.3.2 above(inclusive), fisco-bcos node must be v2.4.1 above(inclusive)!");

    /**
     * Business exception.
     */
    public static final RetCode INVALID_FRONT_ID = RetCode.mark(202000, "invalid front id");

    public static final RetCode DB_EXCEPTION = RetCode.mark(202001, "database exception");

    public static final RetCode FRONT_LIST_NOT_FOUNT = RetCode.mark(202002, "not found any front for this group");

    public static final RetCode INVALID_FRONT_IP = RetCode.mark(202003, "not support this ip");

    public static final RetCode FRONT_EXISTS = RetCode.mark(202004, "front already exists");

    public static final RetCode GROUP_ID_NULL = RetCode
            .mark(202005, "group id cannot be empty");

    public static final RetCode INVALID_GROUP_ID = RetCode.mark(202006, "invalid group id");

    public static final RetCode CHECK_CODE_NULL = RetCode.mark(202007, "checkCode is null");

    public static final RetCode INVALID_CHECK_CODE = RetCode.mark(202008, "invalid checkCode");

    public static final RetCode SAVE_FRONT_FAIL = RetCode.mark(202009, "save front fail");

    public static final RetCode REQUEST_FRONT_FAIL = RetCode.mark(202010, "request front fail");

    public static final RetCode CONTRACT_ABI_EMPTY = RetCode.mark(202011, "abiInfo cannot be empty");

    public static final RetCode USER_ID_NULL = RetCode.mark(202012, "user id cannot be empty");

    public static final RetCode USER_NOT_EXIST = RetCode.mark(202013, "invalid user(NOT EXIST)");

    public static final RetCode USER_EXISTS = RetCode.mark(202014, "user already exists");

    public static final RetCode CONTRACT_EXISTS = RetCode.mark(202015, "contract already exists");

    public static final RetCode INVALID_CONTRACT_ID = RetCode.mark(202017, "invalid contract id");

    public static final RetCode INVALID_PARAM_INFO = RetCode.mark(202018, "invalid param info");

    public static final RetCode CONTRACT_NAME_REPEAT = RetCode.mark(202019, "contract name cannot be repeated");
    
    public static final RetCode DEPLOYED_CANNOT_MODIFIED = RetCode.mark(202020, "deployed contract cannot be modified");

    public static final RetCode CONTRACT_NOT_DEPLOY = RetCode.mark(202023, "contract has not deploy");

    public static final RetCode ACCOUNT_EXISTS = RetCode
            .mark(202026, "account info already exists");

    public static final RetCode ACCOUNT_NOT_EXISTS = RetCode
            .mark(202027, "account info not exists");

    public static final RetCode ACCOUNT_NAME_EMPTY = RetCode.mark(202028, "account name empty");

    public static final RetCode INVALID_ACCOUNT_NAME = RetCode.mark(202029, "invalid account name");

    public static final RetCode PASSWORD_ERROR = RetCode.mark(202030, "password error");

    public static final RetCode ROLE_ID_EMPTY = RetCode.mark(202031, "role id cannot be empty");

    public static final RetCode INVALID_ROLE_ID = RetCode.mark(202032, "invalid role id");

    public static final RetCode CONTRACT_ADDRESS_INVALID = RetCode.mark(202033, "invalid contract address");

    public static final RetCode LOGIN_FAIL = RetCode.mark(202034, "login fail");

    public static final RetCode CONTRACT_HAS_BEAN_DEPLOYED = RetCode
            .mark(202035, "contract has been deployed");

    public static final RetCode PUBLICKEY_NULL = RetCode.mark(202036, "publicKey cannot be empty");
    
    public static final RetCode ACCOUNT_CANNOT_BE_EMPTY = RetCode.mark(202037, "associated account cannot be empty");

    public static final RetCode CONTRACT_DEPLOY_FAIL = RetCode
            .mark(202040, "contract deploy not success");

    public static final RetCode NOW_PWD_EQUALS_OLD = RetCode
            .mark(202045, "the new password cannot be same as old");

    public static final RetCode PUBLICKEY_LENGTH_ERROR = RetCode
            .mark(202050, "publicKey's length is 130, address's length is 42");

    public static final RetCode SERVER_CONNECT_FAIL = RetCode
            .mark(202051, "wrong host ip or wrong port: connect failed");
    public static final RetCode INVALID_TOKEN = RetCode.mark(202052, "invalid token");
    public static final RetCode TOKEN_EXPIRE = RetCode.mark(202053, "token expire");

    public static final RetCode AVAILABLE_FRONT_URL_IS_NULL = RetCode.mark(202054, "Available front url is empty, check front status");

    // 证书管理
    public static final RetCode CERT_ERROR = RetCode.mark(202060, "cert handle error");
    public static final RetCode FAIL_SAVE_CERT_ERROR = RetCode.mark(202061, "store cert error");
    public static final RetCode CERT_FORMAT_ERROR = RetCode.mark(202062,
            "cert format error, must start with -----BEGIN CERTIFICATE-----\\n, end with end");
    public static final RetCode SAVING_FRONT_CERT_ERROR = RetCode.mark(202063, "saving front's cert error");

    // 邮件告警错误
    public static final RetCode MAIL_SERVER_CONFIG_ERROR = RetCode.mark(202070, "Mail server config error.");
    public static final RetCode MAIL_SERVER_CONFIG_PARAM_EMPTY = RetCode.mark(202071,
            "Mail server config param empty/not match.");
    public static final RetCode MAIL_SERVER_CONFIG_ERROR_NO_DATA_IN_DB = RetCode.mark(202072,
            "Mail server config error, db's server config is empty");
    public static final RetCode ALERT_RULE_ERROR = RetCode.mark(202076, "Alert rule error.");
    public static final RetCode ALERT_RULE_PARAM_EMPTY = RetCode.mark(202077, "Alert rule param not match.");
    public static final RetCode SEND_MAIL_ERROR = RetCode.mark(202080,
            "Send mail error, please check mail server configuration.");
    public static final RetCode SEND_MAIL_ERROR_FOR_SERVER_IS_OFF = RetCode.mark(202081,
            "Send mail error, please enable mail server before send.");
    public static final RetCode ALERT_LOG_ERROR = RetCode.mark(202086, "Alert log error.");
    public static final RetCode ALERT_LOG_PARAM_EMPTY = RetCode.mark(202087,
            "Alert log param: status/logId is empty.");

    /* guomi exception */
    public static final RetCode UPDATE_METHOD_ID_GM_ERROR = RetCode.mark(202090, "Update guomi methodId error");
    public static final RetCode ENCRYPT_TYPE_NOT_MATCH = RetCode.mark(202091,
            "Front's encrypt type not matches with nodemgr");

    // abi import
    public static final RetCode CONTRACT_ADDRESS_ALREADY_EXISTS = RetCode.mark(202096, "contract address already exists");
    public static final RetCode ABI_INFO_NOT_EXISTS = RetCode.mark(202097, "abi info of this id not exists");
    public static final RetCode PARAM_FAIL_ABI_INVALID = RetCode.mark(202098, "Contract abi invalid, please check abi");
    public static final RetCode PARAM_FAIL_ABI_ID_EMPTY = RetCode.mark(202099, "Abi Id cannot be empty");
    public static final RetCode CONTRACT_ADDRESS_NULL = RetCode.mark(202100, "contractAddress is null");

    public static final RetCode USER_SIGN_USER_ID_NOT_EXIST = RetCode.mark(202110, "User's signUserId not exist");
    /* Json parse error */
    public static final RetCode FAIL_PARSE_JSON = RetCode.mark(202111, "Fail to parse json");

    /* cert import error */
    public static final RetCode CERT_FILE_NOT_FOUND = RetCode.mark(202121, "Cert file not found, please check cert path in config");
    public static final RetCode PEM_FORMAT_ERROR = RetCode.mark(202122, "Pem file format error, must surrounded by -----XXXXX PRIVATE KEY-----");
    public static final RetCode PEM_CONTENT_ERROR = RetCode.mark(202123, "Pem file content error");
    public static final RetCode P12_PASSWORD_NOT_CHINESE = RetCode.mark(202124, "p12's password cannot be chinese");
    public static final RetCode P12_PASSWORD_ERROR = RetCode.mark(202125, "p12's password not match");
    public static final RetCode P12_FILE_ERROR = RetCode.mark(202126, "P12 file content error");

    /* dynamic group manage */
    public static final RetCode GROUP_ID_EXISTS = RetCode.mark(202300, "group id already exists");
    public static final RetCode NODE_NOT_EXISTS = RetCode.mark(202301, "node's front not exists");

    /* chain governance */
    public static final RetCode GOVERN_VOTE_RECORD_NOT_EXIST = RetCode.mark(202310, "govern vote record not exist");
    public static final RetCode PERMISSION_DENIED_ON_CHAIN = RetCode.mark(202311, "permission denied on chain");

    public static final RetCode CONTRACT_PATH_CONTAIN_DEPLOYED = RetCode.mark(202321, "path contains deployed contract, please delete one by one.");
    public static final RetCode CONTRACT_PATH_CANNOT_BE_BLANK = RetCode.mark(202322, "contract path cannot be blank(use \"/\" instead) ");
    public static final RetCode PRIVATE_KEY_DECODE_FAIL = RetCode.mark(202323, "privateKey decode fail");
    public static final RetCode PASSWORD_DECODE_FAIL = RetCode.mark(202324, "password decode fail");

    // add in v1.4.0
    public static final RetCode NO_DOCKER_TAG_UPDATE_URL_ERROR = RetCode.mark(202401, "No docker image tag update url.");
    public static final RetCode UPDATE_DOCKER_TAG_ERROR = RetCode.mark(202402, "Update docker tag from registry error.");
    public static final RetCode UNKNOWN_CONFIG_TYPE_ERROR = RetCode.mark(202403, "Unknown config type.");
    public static final RetCode SAVE_IP_CONFIG_FILE_ERROR = RetCode.mark(202404, "Save IP config error.");
    public static final RetCode TAG_ID_PARAM_ERROR = RetCode.mark(202405, "Tag id param error.");
    public static final RetCode IP_CONF_PARAM_NULL_ERROR = RetCode.mark(202406, "ipconf null.");
    public static final RetCode CHAIN_NAME_EXISTS_ERROR = RetCode.mark(202407, "Chain name exists.");
    public static final RetCode INSERT_CHAIN_ERROR = RetCode.mark(202408, "Insert new chain failed.");
    public static final RetCode CHAIN_ROOT_DIR_EXIST = RetCode.mark(202409, "Chain root dir exist, please move it manually.");
    public static final RetCode EXEC_BUILD_CHAIN_ERROR = RetCode.mark(202410, "Exec build chain script failed.");
    public static final RetCode IP_CONFIG_LINE_ERROR = RetCode.mark(202411, "ipconf line error.");
    public static final RetCode IP_NUM_ERROR = RetCode.mark(202412, "IP and num config error.");
    public static final RetCode AGENCY_NAME_CONFIG_ERROR = RetCode.mark(202413, "Agency name config error.");
    public static final RetCode GROUPS_CONFIG_ERROR = RetCode.mark(202414, "Groups config error.");
    public static final RetCode HOST_CONNECT_ERROR = RetCode.mark(202415, "Connect to host error.");
    public static final RetCode INSERT_AGENCY_ERROR = RetCode.mark(202416, "Insert new agency failed.");
    public static final RetCode INSERT_GROUP_ERROR = RetCode.mark(202417, "Insert new group failed.");
    public static final RetCode INSERT_HOST_ERROR = RetCode.mark(202418, "Insert new host failed.");
    public static final RetCode INSERT_FRONT_ERROR = RetCode.mark(202419, "Insert new front failed.");
    public static final RetCode INSERT_NODE_ERROR = RetCode.mark(202420, "Insert new node failed.");
    public static final RetCode INSERT_FRONT_GROUP_ERROR = RetCode.mark(202421, "Insert new front node group failed.");
    public static final RetCode PARSE_HOST_INDEX_ERROR = RetCode.mark(202422, "Parse host index from node directory failed.");
    public static final RetCode HOST_ONLY_BELONGS_ONE_AGENCY_ERROR = RetCode.mark(202423, "A host only belongs to one agency.");
    public static final RetCode DEPLOY_WITH_UNKNOWN_EXCEPTION_ERROR = RetCode.mark(202424, "Unexpected exception occurred when deploy.");
    public static final RetCode UNSUPPORTED_PASSWORD_SSH_ERROR = RetCode.mark(202425, "SSH password login not supported yet.");
    public static final RetCode CHAIN_WITH_NO_AGENCY_ERROR = RetCode.mark(202426, "Chain has no agency.");
    public static final RetCode CHAIN_NAME_NOT_EXISTS_ERROR = RetCode.mark(202427, "Chain name not exists.");
    public static final RetCode IP_FORMAT_ERROR = RetCode.mark(202428, "IP format error.");
    public static final RetCode AGENCY_NAME_EMPTY_ERROR = RetCode.mark(202429, "Agency name is null when host ip is new.");
    public static final RetCode AGENCY_NAME_EXISTS_ERROR = RetCode.mark(202430, "Agency name exists when host ip is new.");
    public static final RetCode ADD_NODE_WITH_UNKNOWN_EXCEPTION_ERROR = RetCode.mark(202431, "Unexpected exception occurred when add new node.");
    public static final RetCode CHAIN_CERT_NOT_EXISTS_ERROR = RetCode.mark(202432, "Chain cert directory not exists.");
    public static final RetCode EXEC_GEN_AGENCY_ERROR = RetCode.mark(202433, "Exec generate agency script failed.");
    public static final RetCode HOST_WITH_NO_AGENCY_ERROR = RetCode.mark(202434, "Host's agency is null.");
    public static final RetCode NODES_NUM_ERROR = RetCode.mark(202435, "Num should be positive integer and less then 4.");
    public static final RetCode EXEC_GEN_SDK_ERROR = RetCode.mark(202436, "Exec generate node script to generate sdk dir failed.");
    public static final RetCode EXEC_GEN_NODE_ERROR = RetCode.mark(202437, "Exec generate node script to generate node dir failed.");
    public static final RetCode COPY_SDK_FILES_ERROR = RetCode.mark(202438, "Copy sdk config files error.");
    public static final RetCode SEND_SDK_FILES_ERROR = RetCode.mark(202439, "Send sdk config files error.");
    public static final RetCode SEND_NODE_FILES_ERROR = RetCode.mark(202440, "Send node config files error.");
    public static final RetCode COPY_GROUP_FILES_ERROR = RetCode.mark(202441, "Copy original group config files error.");
    public static final RetCode DELETE_OLD_AGENCY_DIR_ERROR = RetCode.mark(202442, "Delete old agency config files error.");
    public static final RetCode DELETE_OLD_SDK_DIR_ERROR = RetCode.mark(202443, "Delete old sdk of host config files error.");
    public static final RetCode DELETE_OLD_NODE_DIR_ERROR = RetCode.mark(202444, "Delete old node config files error.");
    public static final RetCode NODE_ID_NOT_EXISTS_ERROR = RetCode.mark(202445, "Nodeid not exists.");
    public static final RetCode STOP_NODE_ERROR = RetCode.mark(202446, "Stop node failed.");
    public static final RetCode START_NODE_ERROR = RetCode.mark(202447, "Start node failed.");
    public static final RetCode UPGRADE_WITH_SAME_TAG_ERROR = RetCode.mark(202448, "New image tag and current are the same.");
    public static final RetCode UPDATE_CHAIN_WITH_NEW_VERSION_ERROR = RetCode.mark(202449, "Update chain version error.");
    public static final RetCode NODE_IN_GROUP_ERROR = RetCode.mark(202450, "Node still in group, remove before deleting.");
    public static final RetCode READ_NODE_CONFIG_ERROR = RetCode.mark(202451, "Read node config error.");
    public static final RetCode DELETE_NODE_DIR_ERROR = RetCode.mark(202452, "Delete node config files error.");
    public static final RetCode NODE_RUNNING_ERROR = RetCode.mark(202453, "Node is running.");
    public static final RetCode UPDATE_RELATED_NODE_ERROR = RetCode.mark(202454, "Update related nodes error.");
    public static final RetCode DELETE_CHAIN_ERROR = RetCode.mark(202455, "Delete chain error.");
    public static final RetCode NODE_NEED_REMOVE_FROM_GROUP_ERROR = RetCode.mark(202456, "Node is sealer or observer, remove from group first.");
    public static final RetCode LIST_HOST_NODE_DIR_ERROR = RetCode.mark(202457, "List node dirs of host error.");
    public static final RetCode GENERATE_FRONT_YML_ERROR = RetCode.mark(202458, "Generate front application.yml file failed.");
    public static final RetCode EXEC_HOST_INIT_SCRIPT_ERROR = RetCode.mark(202459, "Exec host init script failed.");
    public static final RetCode TRANSFER_FILES_ERROR = RetCode.mark(202460, "Transfer files error.");
    public static final RetCode HOST_ONLY_ALL_LOCALHOST_OR_NOT_LOCALHOST = RetCode.mark(202461, "Host must be all localhost(127.0.0.1) or all non-localhost");
    public static final RetCode TWO_NODES_AT_LEAST = RetCode.mark(202462, "Two nodes at least.");
    public static final RetCode TWO_SEALER_IN_GROUP_AT_LEAST = RetCode.mark(202463, "Group need two sealers at least.");
    public static final RetCode WEBASE_SIGN_CONFIG_ERROR = RetCode.mark(202464, "Please check webaseSignAddress in application.yml file.");
    public static final RetCode WEBASE_SIGN_NOT_LOCALHOST_ERROR = RetCode.mark(202465, "webaseSignAddress cannot be 127.0.0.1 or localhost in application.yml file");
    public static final RetCode IMAGE_NOT_EXISTS_ON_HOST = RetCode.mark(202466, "Image not exists on host.");
    public static final RetCode NODES_NUM_EXCEED_MAX_ERROR = RetCode.mark(202467, "Max 4 nodes on a same host.");
    public static final RetCode SAME_HOST_ERROR = RetCode.mark(202468, "Host of WeBASE-Node-Manager's ip is already existed.");
    // add in 1.4.3
    public static final RetCode EXEC_DOCKER_CHECK_SCRIPT_ERROR = RetCode.mark(202469, "Check docker installed and running of host");
    public static final RetCode EXEC_HOST_CHECK_SCRIPT_ERROR_FOR_MEM = RetCode.mark(202470, "Check host memory not enough for nodes(s)");
    public static final RetCode EXEC_HOST_CHECK_SCRIPT_ERROR_FOR_CPU = RetCode.mark(202471, "Check host cpu core count not enough for node(s)");
    public static final RetCode EXEC_CHECK_SCRIPT_INTERRUPT = RetCode.mark(202472, "Host check had been interrupt");
    public static final RetCode EXEC_CHECK_SCRIPT_FAIL_FOR_PARAM = RetCode.mark(202473, "Host check fail for inpurt param");
    public static final RetCode CONFIG_CHAIN_LOCALLY_FAIL = RetCode.mark(202475, "Fail to generate chain and front config locally");
    public static final RetCode NOT_ALL_HOST_INIT_SUCCESS = RetCode.mark(202476, "Not all host init success");
    public static final RetCode NODE_PORT_CONFIG_ERROR = RetCode.mark(202477, "Ipconf's node port config error");
    public static final RetCode DEPLOY_INFO_NOT_MATCH_IP_CONF = RetCode.mark(202478, "Ipconf not match with deploy info list");
    public static final RetCode DELETE_HOST_FAIL_FOR_STILL_CONTAIN_NODE = RetCode.mark(202479, "Delete host fail for host contains node(front)");
    // ansible
    public static final RetCode ANSIBLE_NOT_INSTALLED = RetCode.mark(202480, "Ansible not installed!");
    public static final RetCode ANSIBLE_FETCH_NOT_DIR = RetCode.mark(202481, "Ansible fetch not support fetch directory");
    public static final RetCode ANSIBLE_PING_NOT_REACH = RetCode.mark(202482, "Ansible ping cannot reach target ip");
    public static final RetCode ANSIBLE_INIT_HOST_CDN_SCP_NOT_ALL_SUCCESS = RetCode.mark(202483, "Ansible init host of image and scp config not all success");
    public static final RetCode ANSIBLE_PULL_DOCKER_HUB_ERROR = RetCode.mark(202484, "Ansible pull docker hub fail");
    public static final RetCode ANSIBLE_PULL_DOCKER_CDN_ERROR = RetCode.mark(202485, "Ansible pull docker cdn fail");
    public static final RetCode ANSIBLE_DOCKER_COMMAND_ERROR = RetCode.mark(202486, "Ansible run docker command fail");
    public static final RetCode ANSIBLE_COMMON_COMMAND_ERROR = RetCode.mark(202487, "Ansible exec command error");
    public static final RetCode ANSIBLE_SCP_COPY_ERROR = RetCode.mark(202488, "Ansible exec scp(copy) error");
    public static final RetCode ANSIBLE_SCP_FETCH_ERROR = RetCode.mark(202489, "Ansible exec scp(fetch) error");
    public static final RetCode ANSIBLE_CHECK_DOCKER_IMAGE_ERROR = RetCode.mark(202491, "Ansible check image exist error for param");
    public static final RetCode ANSIBLE_CHECK_CONTAINER_ERROR = RetCode.mark(202492, "Ansible check docker container exist error for param");

    public static final RetCode CHECK_HOST_MEM_CPU_DOCKER_FAIL = RetCode.mark(202493, "Check host not pass, please check host remark");
    public static final RetCode CHECK_HOST_PORT_IN_USE = RetCode.mark(202494, "Check host port is in use, please check host remark");
    public static final RetCode HOST_ALREADY_EXIST = RetCode.mark(202495, "Host already exist");
    public static final RetCode HOST_ROOT_DIR_ACCESS_DENIED = RetCode.mark(202496, "Host root dir access denied");
    public static final RetCode HOST_NOT_EXIST = RetCode.mark(202497, "Host not exist or already been deleted");


    // add in v1.4.2
    public static final RetCode CONTRACT_PATH_IS_EXISTS = RetCode.mark(202501, "contract path is exists.");
    
    // add in v1.4.3
    public static final RetCode VERSION_CANNOT_EMPTY = RetCode.mark(202502, "version cannot be empty.");
    public static final RetCode CNS_NAME_CANNOT_EMPTY = RetCode.mark(202503, "cns name cannot be empty.");
    public static final RetCode VERSION_ALREADY_EXISTS = RetCode.mark(202504,"version already exists");

    // add in v1.5.0
    public static final RetCode SDK_CRT_KEY_FILE_NOT_FOUND = RetCode.mark(202511,"Front's sdk cert and key not found!");
    public static final RetCode WRITE_SDK_CRT_KEY_FILE_FAIL = RetCode.mark(202512,"Write front's sdk cert and key fail!");
    public static final RetCode WRITE_PRIVATE_KEY_CRT_KEY_FILE_FAIL = RetCode.mark(202513,"Write private key file fail!");
    public static final RetCode GENESIS_CONF_NOT_FOUND = RetCode.mark(202514, "group genesis conf not found");

    // app integration
    public static final RetCode APPNAME_EXISTS = RetCode.mark(202516, "app name exists");
    public static final RetCode APPNAME_NOT_EXISTS = RetCode.mark(202517, "app name not exists");
    public static final RetCode ID_NOT_EXISTS = RetCode.mark(202518, "app id not exists");
    public static final RetCode LINK_FORMAT_INVALID = RetCode.mark(202519, "link format invalid, example:[http://{ip}:{port}/index.html]");
    public static final RetCode CONTRACT_SOURCE_NOT_EXIST = RetCode.mark(202520, "contract source not exist");
    public static final RetCode TIMESTAMP_CANNOT_EMPTY = RetCode.mark(202521, "timestamp cannot be empty");
    public static final RetCode APPKEY_CANNOT_EMPTY = RetCode.mark(202522, "app key cannot be empty");
    public static final RetCode SIGNATURE_CANNOT_EMPTY = RetCode.mark(202523, "signature cannot be empty");
    public static final RetCode TIMESTAMP_TIMEOUT = RetCode.mark(202524, "timestamp timeout");
    public static final RetCode APPKEY_NOT_EXISTS = RetCode.mark(202525, "app key not exists");
    public static final RetCode SIGNATURE_NOT_MATCH = RetCode.mark(202526, "signature not match");
    public static final RetCode REQUEST_ENCRYPT_FAIL = RetCode.mark(202527, "request encrypt fail");
    public static final RetCode REQUEST_DECRYPT_FAIL = RetCode.mark(202528, "request decrypt fail");
    public static final RetCode ENCRYPT_NOT_MATCH = RetCode.mark(202529, "isTransferEncrypt config not match");

    // add in v1.5.1
    public static final RetCode GENERATE_CONTRACT_PROJECT_FAIL = RetCode.mark(202531, "generate project failed in scaffold");
    public static final RetCode GET_FRONT_SDK_MAP_FAIL = RetCode.mark(202532, "get sdk cert/key content map failed");
    public static final RetCode PARAM_INVALID_LETTER_DIGIT = RetCode.mark(202533, "Only support letter and digit, please check your params");
    public static final RetCode CONTRACT_PATH_NOT_EXISTS = RetCode.mark(202534, "contract path not exists.");

    /* auth */
    public static final RetCode USER_NOT_LOGGED_IN = RetCode.mark(302000, "user not logged in");
    public static final RetCode ACCESS_DENIED = RetCode.mark(302001, "access denied");

    /* param exception */
    public static final RetCode PARAM_EXCEPTION = RetCode.mark(402000, "param exception");

    public static final RetCode PRIVATE_KEY_NOT_EXISTS  = RetCode.mark(202513,"It is not your private key");
}
