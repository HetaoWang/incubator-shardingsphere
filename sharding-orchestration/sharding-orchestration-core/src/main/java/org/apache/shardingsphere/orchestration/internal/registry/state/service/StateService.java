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

package org.apache.shardingsphere.orchestration.internal.registry.state.service;

import org.apache.shardingsphere.orchestration.center.api.RegistryCenterRepository;
import org.apache.shardingsphere.orchestration.internal.registry.state.instance.OrchestrationInstance;
import org.apache.shardingsphere.orchestration.internal.registry.state.node.StateNode;

/**
 * Instance state service.
 */
public final class StateService {
    
    private final StateNode stateNode;
    
    private final RegistryCenterRepository registryCenterRepository;
    
    private final OrchestrationInstance instance;
    
    public StateService(final String name, final RegistryCenterRepository registryCenterRepository) {
        stateNode = new StateNode(name);
        this.registryCenterRepository = registryCenterRepository;
        instance = OrchestrationInstance.getInstance();
    }
    
    /**
     * Persist instance online.
     */
    public void persistInstanceOnline() {
        registryCenterRepository.persistEphemeral(stateNode.getInstancesNodeFullPath(instance.getInstanceId()), "");
    }
    
    /**
     * Initialize data sources node.
     */
    public void persistDataSourcesNode() {
        registryCenterRepository.persist(stateNode.getDataSourcesNodeFullRootPath(), "");
    }
}
