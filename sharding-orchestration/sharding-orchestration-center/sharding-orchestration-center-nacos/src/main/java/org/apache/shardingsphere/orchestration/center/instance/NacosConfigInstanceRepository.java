/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.shardingsphere.orchestration.center.instance;

import com.alibaba.nacos.api.NacosFactory;
import com.alibaba.nacos.api.PropertyKeyConst;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.config.listener.Listener;
import com.alibaba.nacos.api.exception.NacosException;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.orchestration.center.api.ConfigCenterRepository;
import org.apache.shardingsphere.orchestration.center.configuration.InstanceConfiguration;
import org.apache.shardingsphere.orchestration.center.listener.DataChangedEvent;
import org.apache.shardingsphere.orchestration.center.listener.DataChangedEventListener;
import org.apache.shardingsphere.orchestration.center.util.ConfigKeyUtils;

import java.util.List;
import java.util.Properties;
import java.util.concurrent.Executor;

/**
 * The nacos instance for ConfigCenter.
 */
@Slf4j
public final class NacosConfigInstanceRepository implements ConfigCenterRepository {
    
    private ConfigService configService;
    
    private NacosProperties nacosProperties;
    
    @Getter
    @Setter
    private Properties properties = new Properties();
    
    /**
     * Initialize nacos instance.
     *
     * @param config config center configuration
     */
    @Override
    public void init(final InstanceConfiguration config) {
        try {
            nacosProperties = new NacosProperties(properties);
            Properties properties = new Properties();
            properties.put(PropertyKeyConst.SERVER_ADDR, config.getServerLists());
            properties.put(PropertyKeyConst.NAMESPACE, null == config.getNamespace() ? "" : config.getNamespace());
            configService = NacosFactory.createConfigService(properties);
        } catch (final NacosException ex) {
            log.error("Init nacos config center exception for: {}", ex.toString());
        }
    }
    
    /**
     * Get data from nacos instance.
     *
     * @param key key of data
     * @return value of data
     */
    @Override
    public String get(final String key) {
        try {
            String dataId = ConfigKeyUtils.path2Key(key);
            String group = nacosProperties.getValue(NacosPropertiesEnum.GROUP);
            long timeoutMs = nacosProperties.getValue(NacosPropertiesEnum.TIMEOUT);
            return configService.getConfig(dataId, group, timeoutMs);
        } catch (final NacosException ex) {
            log.debug("Nacos get config value exception for: {}", ex.toString());
            return null;
        }
    }
    
    /**
     * Get node's sub-nodes list.
     *
     * @param key key of data
     * @return sub-nodes name list
     */
    @Override
    public List<String> getChildrenKeys(final String key) {
        return null;
    }
    
    /**
     * Persist data.
     *
     * @param key key of data
     * @param value value of data
     */
    @Override
    public void persist(final String key, final String value) {
        try {
            String dataId = ConfigKeyUtils.path2Key(key);
            String group = nacosProperties.getValue(NacosPropertiesEnum.GROUP);
            configService.publishConfig(dataId, group, value);
        } catch (final NacosException ex) {
            log.debug("Nacos persist config exception for: {}", ex.toString());
        }
    }
    
    /**
     * Watch key or path of the config server.
     *
     * @param key key of data
     * @param dataChangedEventListener data changed event listener
     */
    @Override
    public void watch(final String key, final DataChangedEventListener dataChangedEventListener) {
        try {
            String dataId = ConfigKeyUtils.path2Key(key);
            String group = nacosProperties.getValue(NacosPropertiesEnum.GROUP);
            configService.addListener(dataId, group, new Listener() {
                
                @Override
                public Executor getExecutor() {
                    return null;
                }
                
                @Override
                public void receiveConfigInfo(final String configInfo) {
                    dataChangedEventListener.onChange(new DataChangedEvent(key, configInfo, DataChangedEvent.ChangedType.UPDATED));
                }
            });
        } catch (final NacosException ex) {
            log.debug("Nacos watch key exception for: {}", ex.toString());
        }
    }
    
    @Override
    public void close() {
    }
    
    /**
     * Get algorithm type.
     *
     * @return type
     */
    @Override
    public String getType() {
        return "nacos";
    }
}
