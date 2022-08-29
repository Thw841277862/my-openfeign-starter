package my.openfeign.starter;

import feign.Feign;
import feign.Request;
import feign.Response;
import feign.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.context.ApplicationContext;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static feign.Util.*;

public class MyLogConfiguration extends feign.Logger implements InitializingBean {
    private final Logger logger;
    @Autowired
    private ApplicationContext applicationContext;

    private Set<String> logSet = new HashSet<>();

    public MyLogConfiguration() {
        this(feign.Logger.class);
    }

    public MyLogConfiguration(Class<?> clazz) {
        this(LoggerFactory.getLogger(clazz));
    }

    public MyLogConfiguration(String name) {
        this(LoggerFactory.getLogger(name));
    }

    MyLogConfiguration(Logger logger) {
        this.logger = logger;
    }

    @Override
    protected void logRequest(String configKey, Level logLevel, Request request) {
        if (logger.isInfoEnabled()) {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("request ---> ").append(request.httpMethod().name()).append(" ").append(request.url() + " ").append("HTTP/1.1 ");
//            log(configKey, "---> %s %s HTTP/1.1", request.httpMethod().name(), request.url());
            for (String field : request.headers().keySet()) {
                for (String value : valuesOrEmpty(request.headers(), field)) {
                    stringBuilder.append(field + " ").append(": ").append(value).append(" ").append(", ");
                }
            }
//            log(configKey, "%s", stringBuilder.toString());//如果为空则不调用
            int bodyLength = 0;
            if (request.body() != null) {
                bodyLength = request.length();
                String bodyText = request.charset() != null ? new String(request.body(), request.charset()) : null;
                stringBuilder.append(bodyText != null ? bodyText : "Binary data").append(" ");
//                log(configKey, "%s", bodyText != null ? bodyText : "Binary data");
            }
            stringBuilder.append("---> END HTTP (").append(bodyLength + " ").append("-byte body)");
//            log(configKey, "---> END HTTP (%s-byte body)", bodyLength);
            log(configKey, "%s", stringBuilder.toString());
        }
    }

    @Override
    protected void logRetry(String configKey, Level logLevel) {
        log(configKey, "---> RETRYING");
    }

    @Override
    protected Response logAndRebufferResponse(String configKey,
                                              Level logLevel,
                                              Response response,
                                              long elapsedTime)
            throws IOException {
        String reason = response.reason() != null ? response.reason() + "" : "";
        int status = response.status();
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("response <--- ").append("HTTP/1.1 ").append(status + " ").append(reason + " (").append(elapsedTime).append("ms) ");
//        log(configKey, " <--- HTTP/1.1 %s%s (%sms)", status, reason, elapsedTime);
        int bodyLength = 0;
        if (response.body() != null && !(status == 204 || status == 205)) {
            //HTTP 204无内容“……响应不得包含消息正文”
            //HTTP 205重置内容“…响应不得包含实体”
            byte[] bodyData = Util.toByteArray(response.body().asInputStream());
            bodyLength = bodyData.length;
            stringBuilder.append(decodeOrDefault(bodyData, UTF_8, "Binary data")).append(" ").append("<--- END HTTP (").append(bodyLength).append("-byte body)");
//            log(configKey, "%s", decodeOrDefault(bodyData, UTF_8, "Binary data"));
//            log(configKey, "<--- END HTTP (%s-byte body)", bodyLength);
            log(configKey, "%s", stringBuilder.toString());
            return response.toBuilder().body(bodyData).build();
        } else {
            stringBuilder.append("<--- END HTTP (").append(bodyLength).append("byte body)");
            log(configKey, "%s", stringBuilder.toString());
        }
        return response;
    }

    @Override
    protected void log(String configKey, String format, Object... args) {
        if (logger.isInfoEnabled()) {//AgentOrgApiService#queryAgentOrgInfo(Long)
            logger.info(String.format(methodTag(configKey) + format, args));
        }
    }


    /**
     * 自定义是否输出日志
     *
     * @throws Exception
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        Map<String, Object> beansWithAnnotation = applicationContext.getBeansWithAnnotation(FeignClient.class);
        for (Map.Entry<String, Object> entry : beansWithAnnotation.entrySet()) {
            Class<?> aClass = Class.forName(entry.getKey());
            Method[] methods = aClass.getDeclaredMethods();
            for (int i = 0; i < methods.length; i++) {
                FeignExcludeLog declaredAnnotation = methods[i].getDeclaredAnnotation(FeignExcludeLog.class);
                String configKey = Feign.configKey(aClass, methods[i]);
                if (declaredAnnotation == null) {
                    logSet.add(configKey);
                }
            }
        }
    }
}
