package com.allen.spring.cloud.nacos.eureka.sync;

import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingMaintainService;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.netflix.appinfo.InstanceInfo;
import com.netflix.discovery.shared.Application;
import com.netflix.eureka.registry.PeerAwareInstanceRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.netflix.eureka.server.event.EurekaInstanceCanceledEvent;
import org.springframework.cloud.netflix.eureka.server.event.EurekaInstanceRegisteredEvent;
import org.springframework.cloud.netflix.eureka.server.event.EurekaInstanceRenewedEvent;
import org.springframework.cloud.netflix.eureka.server.event.EurekaRegistryAvailableEvent;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 把Eureka中的服务同步到Nacos
 */
@Component
public class SynchronizeEurekaToNacos implements ApplicationListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(SynchronizeEurekaToNacos.class);

    private NacosInstanceCache nacosInstanceCache = new NacosInstanceCache();

    @Autowired
    private NamingService namingService;

    @Autowired
    private NamingMaintainService namingMaintainService;

    @Autowired
    private PeerAwareInstanceRegistry peerAwareInstanceRegistry;

    @Override
    public void onApplicationEvent(ApplicationEvent event) {
        if (event instanceof EurekaInstanceRegisteredEvent) {
            register((EurekaInstanceRegisteredEvent) event);
        } else if (event instanceof EurekaInstanceRenewedEvent) {
            setStatus((EurekaInstanceRenewedEvent) event);
        } else if (event instanceof EurekaInstanceCanceledEvent) {
            deregister((EurekaInstanceCanceledEvent) event);
        } else if (event instanceof EurekaRegistryAvailableEvent) {
            LOGGER.info("EurekaRegistryAvailableEvent");
        }
    }

    private void register(EurekaInstanceRegisteredEvent event) {
        InstanceInfo instanceInfo = event.getInstanceInfo();
        if (instanceInfo == null || isFromNacos(instanceInfo)) {
            //实例本身来自Nacos，则忽略
            return;
        }

        Instance instance = convertEurekaInstanceToNacosInstance(instanceInfo);
        // 相同的服务实例（服务名、IP、端口相同）重复注册服务到Nacos，会导致Nacos启动多个服务心跳监测，导致服务无法下线
        if (nacosInstanceCache.containInstance(instance)) {
            return;
        }
        try {
            namingService.registerInstance(instanceInfo.getAppName().toLowerCase(), Constants.DEFAULT_GROUP, instance);
            nacosInstanceCache.add(instance);
        } catch (NacosException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    private void setStatus(EurekaInstanceRenewedEvent event) {
        InstanceInfo instanceInfo = event.getInstanceInfo();
        if (instanceInfo == null || isFromNacos(instanceInfo)) {
            return;
        }

        Instance instance = convertEurekaInstanceToNacosInstance(instanceInfo);
        InstanceInfo.InstanceStatus status = instanceInfo.getStatus();
        if (status.equals(InstanceInfo.InstanceStatus.UP) || status.equals(InstanceInfo.InstanceStatus.STARTING)) {
            instance.setEnabled(true);
        } else {
            instance.setEnabled(false);
        }
        if (!nacosInstanceCache.containService(instanceInfo.getAppName())) {
            return;
        }
        try {
            namingMaintainService.updateInstance(instanceInfo.getAppName().toLowerCase(), instance);
        } catch (NacosException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    private void deregister(EurekaInstanceCanceledEvent event) {
        String serverId = event.getServerId();
        String appName = event.getAppName();

        List<Application> applicationList = peerAwareInstanceRegistry.getSortedApplications();
        Optional<Application> optional = applicationList.stream().filter(app -> app.getName().equals(appName)).findFirst();
        if (optional.isPresent()) {
            Application application = optional.get();
            InstanceInfo instanceInfo = application.getByInstanceId(serverId);
            if (isFromNacos(instanceInfo)) {
                return;
            }
            try {
                Instance instance = convertEurekaInstanceToNacosInstance(instanceInfo);
                namingService.deregisterInstance(instanceInfo.getAppName().toLowerCase(), Constants.DEFAULT_GROUP, instance);
                nacosInstanceCache.remove(instance);
            } catch (NacosException e) {
                LOGGER.error(e.getMessage(), e);
            }
        }
    }

    private Instance convertEurekaInstanceToNacosInstance(InstanceInfo instanceInfo) {
        Map<String, String> metadata = new HashMap<>(instanceInfo.getMetadata());
        metadata.put(Constants.METADATA_REGISTRY_CLIENT, Constants.EUREKA);
        Instance instance = new Instance();
        instance.setServiceName(instanceInfo.getAppName());
        instance.setIp(instanceInfo.getIPAddr());
        instance.setPort(instanceInfo.getPort());
        instance.setWeight(1);
        instance.setClusterName(com.alibaba.nacos.api.common.Constants.DEFAULT_CLUSTER_NAME);
        instance.setMetadata(metadata);
        return instance;
    }

    private boolean isFromNacos(InstanceInfo instanceInfo) {
        String registryClient = instanceInfo.getMetadata().get(Constants.METADATA_REGISTRY_CLIENT);
        return !StringUtils.isEmpty(registryClient) && registryClient.equals(Constants.NACOS);
    }
}