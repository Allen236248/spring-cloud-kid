package com.allen.spring.cloud.nacos.eureka.sync;

import com.alibaba.nacos.api.naming.pojo.Instance;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class NacosInstanceCache {

    private ConcurrentHashMap<String, List<Instance>> instanceHolder= new ConcurrentHashMap<>();

    public void add(Instance instance) {
        if(null == instance)
            return;

        String serviceName = instance.getServiceName();
        List<Instance> list = instanceHolder.get(serviceName);
        if (list == null) {
            list = new ArrayList<>();
            instanceHolder.put(serviceName, list);
        }
        list.add(instance);
    }

    public void remove(Instance instance) {
        String serviceName = instance.getServiceName();
        List<Instance> list = instanceHolder.get(serviceName);
        if (list == null)
            return;

        for (Instance item : list) {
            if (item.getIp().equals(instance.getIp()) && item.getPort() == instance.getPort()) {
                list.remove(item);
                if(list.isEmpty()) {
                    instanceHolder.remove(serviceName);
                }
                return;
            }
        }
    }

    public boolean containService(String serviceName) {
        List<Instance> list = instanceHolder.get(serviceName);
        return list != null && !list.isEmpty();
    }

    public boolean containInstance(Instance instance) {
        String serviceName = instance.getServiceName();
        List<Instance> list = instanceHolder.get(serviceName);
        if (list == null || list.isEmpty())
            return false;

        for (Instance item : list) {
            if (item.getIp().equals(instance.getIp()) && item.getPort() == instance.getPort()) {
                return true;
            }
        }
        return false;
    }
}
