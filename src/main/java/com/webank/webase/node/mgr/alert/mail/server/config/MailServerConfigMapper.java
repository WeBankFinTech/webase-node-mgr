/**
 * Copyright 2014-2021 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.webank.webase.node.mgr.alert.mail.server.config;

import com.webank.webase.node.mgr.alert.mail.server.config.entity.TbMailServerConfig;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MailServerConfigMapper {

    void add(TbMailServerConfig tbMailServerConfig);

    void deleteByServerId(@Param("serverId") int serverId);

    void update(TbMailServerConfig tbMailServerConfig);

    TbMailServerConfig queryByServerId(@Param("serverId") int serverId);

    List<TbMailServerConfig> listOfMailServerConfig();
}
