package com.allen.spring.cloud.nacos.eureka.sync;

import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.listener.Event;
import com.alibaba.nacos.api.naming.listener.EventListener;
import com.alibaba.nacos.api.naming.listener.NamingEvent;
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.alibaba.nacos.api.naming.pojo.ListView;
import com.alibaba.nacos.api.naming.pojo.ServiceInfo;
import com.netflix.appinfo.DataCenterInfo;
import com.netflix.appinfo.InstanceInfo;
import com.netflix.discovery.shared.Application;
import com.netflix.eureka.registry.PeerAwareInstanceRegistry;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.commons.util.InetUtils;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 把Nacos中的服务同步到Eureka
 */
@Component
public class SynchronizeNacosToEureka {

    private static final Logger LOGGER = LoggerFactory.getLogger(SynchronizeNacosToEureka.class);

    @Autowired
    private InetUtils inetUtils;

    @Autowired
    private NamingService namingService;

    @Autowired
    private PeerAwareInstanceRegistry peerAwareInstanceRegistry;

    @PostConstruct
    public void init() {
        ScheduledExecutorService executor = new ScheduledThreadPoolExecutor(1, runnable -> {
            Thread t = new Thread(runnable);
            t.setName(SynchronizeNacosToEureka.class.getName());
            t.setDaemon(true);
            return t;
        });
        executor.scheduleWithFixedDelay(() -> {
            try {
                sync();
            } catch (Throwable e) {
                LOGGER.error("synchronize service error", e);
            }
        }, 5, 10, TimeUnit.SECONDS);
    }

    public void sync() throws Exception {
        ListView<String> serviceList = namingService.getServicesOfServer(1, 1000);
        if (null == serviceList)
            return;

        List<String> data = serviceList.getData();
        if (null == data || data.isEmpty())
            return;

        for (String serviceName : data) {
            List<Instance> instanceList = namingService.getAllInstances(serviceName);
            for (Instance instance : instanceList) {
                if (isFromEureka(instance)) {
                    continue;
                }

                String instanceId = String.format("%s:%s:%s", serviceName, instance.getIp(), instance.getPort());
                //注册到Eureka
                peerAwareInstanceRegistry.renew(serviceName.toUpperCase(), instanceId, false);
            }

            List<ServiceInfo> list = namingService.getSubscribeServices();
            Optional<ServiceInfo> optional = list.stream().filter(serviceInfo -> serviceInfo.getName().equals(serviceName)).findFirst();
            if (!optional.isPresent()) {
                namingService.subscribe(serviceName, new NacosEventListener());
            }
        }
    }

    private boolean isFromEureka(Instance instance) {
        String registryClient = instance.getMetadata().get(Constants.METADATA_REGISTRY_CLIENT);
        return !StringUtils.isEmpty(registryClient) && registryClient.equals(Constants.EUREKA);
    }

    private class NacosEventListener implements EventListener {

        @Override
        public void onEvent(Event event) {
            if (event instanceof NamingEvent) {
                NamingEvent namingEvent = (NamingEvent) event;
                List<Instance> instanceList = namingEvent.getInstances();
                String serviceName = namingEvent.getServiceName().substring(namingEvent.getServiceName().lastIndexOf('@') + 1).toUpperCase();

                List<InstanceInfo> instanceInfoList = new ArrayList<>();

                List<Application> applications = peerAwareInstanceRegistry.getSortedApplications();
                Optional<Application> optional = applications.stream().filter(application -> application.getName().equals(serviceName)).findFirst();
                if (optional.isPresent()) {
                    Application application = optional.get();
                    instanceInfoList.addAll(application.getInstances());
                }
                for (InstanceInfo instanceInfo : instanceInfoList) {
                    Optional<Instance> find = instanceList.stream().filter(instance ->
                            instance.getIp().equals(instanceInfo.getIPAddr()) && instance.getPort() == instanceInfo.getPort()).findFirst();
                    if (find.isPresent()) {
                        // 相同的服务实例
                        Instance instance = find.get();
                        if (isFromEureka(instance)) {
                            continue;
                        }
                        setStatus(instanceInfo, instance.isEnabled());
                    } else {
                        // 相同的服务，不同的实例，发生状态变化，在Eureka侧，先做下线处理
                        deregister(instanceInfo);
                    }
                }
                for (Instance instance : instanceList) {
                    if (isFromEureka(instance)) {
                        continue;
                    }
                    Optional<InstanceInfo> find = instanceInfoList.stream().filter(instanceInfo ->
                            instance.getIp().equals(instanceInfo.getIPAddr()) && instance.getPort() == instanceInfo.getPort()).findFirst();
                    if (!find.isPresent()) {
                        // 再做上线处理
                        register(instance);
                    }
                }
            }
        }

        private void setStatus(InstanceInfo instanceInfo, boolean enabled) {
            InstanceInfo.InstanceStatus status;
            if (enabled) {
                status = InstanceInfo.InstanceStatus.UP;
            } else {
                status = InstanceInfo.InstanceStatus.DOWN;
            }
            peerAwareInstanceRegistry.statusUpdate(instanceInfo.getAppName(), instanceInfo.getId(), status,
                    instanceInfo.getLastDirtyTimestamp() + "", false);
        }

        private void deregister(InstanceInfo instanceInfo) {
            peerAwareInstanceRegistry.cancel(instanceInfo.getAppName(), instanceInfo.getInstanceId(), false);
        }

        private void register(Instance instance) {
            String appName = instance.getServiceName().substring(instance.getServiceName().lastIndexOf('@') + 1);
            String registryClient = instance.getMetadata().get(Constants.METADATA_REGISTRY_CLIENT);

            if (StringUtils.isEmpty(registryClient)) {
                InetUtils.HostInfo hostInfo = inetUtils.findFirstNonLoopbackHostInfo();
                String hostname = hostInfo.getHostname();

                Map<String, String> metadata = new HashMap<>(instance.getMetadata());
                metadata.put(Constants.METADATA_REGISTRY_CLIENT, Constants.NACOS);
                peerAwareInstanceRegistry.register(InstanceInfo.Builder.newBuilder()
                        .setAppName(appName.toLowerCase())
                        .setIPAddr(instance.getIp())
                        .setPort(instance.getPort())
                        .setHostName(hostname)
                        .setVIPAddress(appName)
                        .setSecureVIPAddress(appName)
                        .setInstanceId(String.format("%s:%s:%s", appName, instance.getIp(), instance.getPort()))
                        .setDataCenterInfo(() -> DataCenterInfo.Name.MyOwn)
                        .setMetadata(metadata)
                        .build(), false);
            }
        }

    }

}
