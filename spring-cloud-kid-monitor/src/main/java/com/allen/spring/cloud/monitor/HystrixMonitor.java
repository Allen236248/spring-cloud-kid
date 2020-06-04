package com.allen.spring.cloud.monitor;

import com.netflix.hystrix.HystrixCommandMetrics;
import com.netflix.hystrix.HystrixThreadPoolMetrics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;
import org.springframework.context.*;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.util.NumberUtils;
import org.springframework.util.StringUtils;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Collection;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Component
public final class HystrixMonitor extends PropertyPlaceholderConfigurer implements InitializingBean, ApplicationContextAware {

    private static final Logger LOGGER = LoggerFactory.getLogger(HystrixMonitor.class);

    private transient ApplicationContext applicationContext;

    private static final ScheduledExecutorService HYSTRIX_COMMAND_SCHEDULED = Executors.newSingleThreadScheduledExecutor();

    private static final ScheduledExecutorService HYSTRIX_THREAD_POOL_SCHEDULED = Executors.newSingleThreadScheduledExecutor();

    private static final String HYSTRIX_MONITOR_INTERVAL_IN_SECONDS = "hystrix.monitor.interval.in.seconds";

    private static final long DEFAULT_INTERVAL_IN_SECONDS = 5000L;

    private long intervalInSeconds = DEFAULT_INTERVAL_IN_SECONDS;

    private transient volatile boolean started;

    @Override
    public void setApplicationContext(ApplicationContext context) throws BeansException {
        this.applicationContext = context;

        if (context instanceof ConfigurableApplicationContext) {
            ((ConfigurableApplicationContext) context).registerShutdownHook();
            ((ConfigurableApplicationContext) context).addApplicationListener(new ShutdownHookListener());
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        //读取配置
        Environment env = applicationContext.getEnvironment();
        String intervalInSeconds = env.getProperty(HYSTRIX_MONITOR_INTERVAL_IN_SECONDS);
        LOGGER.info("The property intervalInSeconds is " + intervalInSeconds);
        if(StringUtils.hasText(intervalInSeconds)) {
            setIntervalInSeconds(NumberUtils.parseNumber(intervalInSeconds, Long.class));
        }

        start();
    }

    public void start() {
        if (started) {
            return;
        }

        started = true;
        HYSTRIX_COMMAND_SCHEDULED.scheduleAtFixedRate(new HystrixCommandMonitorTask(), intervalInSeconds, intervalInSeconds, TimeUnit.SECONDS);
        HYSTRIX_THREAD_POOL_SCHEDULED.scheduleAtFixedRate(new HystrixThreadPoolMonitorTask(), intervalInSeconds, intervalInSeconds, TimeUnit.SECONDS);
    }

    public void close() {
        if(!started) {
            return;
        }

        started = false;
        HYSTRIX_COMMAND_SCHEDULED.shutdown();
        HYSTRIX_THREAD_POOL_SCHEDULED.shutdown();
    }

    public long getIntervalInSeconds() {
        return intervalInSeconds;
    }

    public void setIntervalInSeconds(long intervalInSeconds) {
        this.intervalInSeconds = intervalInSeconds;
    }

    private static class HystrixCommandMonitorTask implements Runnable {

        @Override
        public void run() {
            // 获取Hystrix命令执行情况数据（Hystrix自动收集的）
            Collection<HystrixCommandMetrics> metrics = HystrixCommandMetrics.getInstances();
            StringBuffer cmdExecInfo = new StringBuffer();
            for (HystrixCommandMetrics metric : metrics) {
                String cmdGroup = metric.getCommandGroup().name();
                String cmdKey = metric.getCommandKey().name();
                String tpKey = metric.getThreadPoolKey().name();
                int ccec = metric.getCurrentConcurrentExecutionCount();
                int etm = metric.getExecutionTimeMean();
                long rmce = metric.getRollingMaxConcurrentExecutions();

                try {
                    cmdExecInfo.append("服务器:").append(InetAddress.getLocalHost().getHostAddress()).append(",");
                } catch (UnknownHostException e) {

                }
                cmdExecInfo.append("CommandGroup:").append(cmdGroup).append(",")
                        .append("CommandKey:").append(cmdKey).append(",")
                        .append("ThreadPoolKey:").append(tpKey).append(",")
                        .append("当前并发数:").append(ccec).append(",")
                        .append("平均执行时间:").append(etm).append(",")
                        .append("窗口期最大并发数:").append(rmce).append("\n");
            }
            if(cmdExecInfo.length() > 0) {
                LOGGER.info(cmdExecInfo.toString());
            }
        }
    }

    private static class HystrixThreadPoolMonitorTask implements Runnable {

        @Override
        public void run() {
            // 获取Hystrix线程池指标数据（Hystrix自动收集的）
            Collection<HystrixThreadPoolMetrics> metrics = HystrixThreadPoolMetrics.getInstances();
            StringBuffer threadPoolInfo = new StringBuffer();
            for (HystrixThreadPoolMetrics metric : metrics) {
                // 线程池组名
                String groupName = metric.getThreadPoolKey().name();
                // 最大线程数
                int maxPoolSize = metric.getCurrentMaximumPoolSize().intValue();
                // 当前线程池大小
                int currentPoolSize = metric.getCurrentPoolSize().intValue();
                // 当前活跃线程数
                int currentActive = metric.getCurrentActiveCount().intValue();

                // 窗口期(默认最近10s)线程执行的次数
                long rollingCountThreadsExecuted = metric.getRollingCountThreadsExecuted();
                // 窗口期(默认最近10s)线程拒绝的次数
                long rollingCountThreadsRejected = metric.getRollingCountThreadsRejected();
                // 窗口期(默认最近10s)最大并发线程数
                long rollingMaxActiveThreads = metric.getRollingMaxActiveThreads();

                try {
                    threadPoolInfo.append("服务器:").append(InetAddress.getLocalHost().getHostAddress()).append(",");
                } catch (UnknownHostException e) {

                }
                threadPoolInfo.append("线程池:").append(groupName).append(",")
                        .append("最大线程数:").append(maxPoolSize).append(",")
                        .append("当前线程数:").append(currentPoolSize).append(",")
                        .append("活跃线程数:").append(currentActive).append(",")
                        .append("窗口期线程数执行次数:").append(rollingCountThreadsExecuted).append(",")
                        .append("窗口期线程数拒绝次数:").append(rollingCountThreadsRejected).append(",")
                        .append("窗口期最大并发数:").append(rollingMaxActiveThreads).append("\n");
            }
            if(threadPoolInfo.length() > 0) {
                LOGGER.info(threadPoolInfo.toString());
            }
        }
    }

    private class ShutdownHookListener implements ApplicationListener {
        @Override
        public void onApplicationEvent(ApplicationEvent event) {
            if (event instanceof ContextClosedEvent) {
                close();
            }
        }
    }
}
