package my.openfeign.starter;

import feign.*;
import feign.codec.Decoder;
import feign.codec.Encoder;
import feign.codec.ErrorDecoder;
import feign.hystrix.FallbackFactory;
import feign.slf4j.Slf4jLogger;

import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.cloud.openfeign.FeignAutoConfiguration;
import org.springframework.cloud.openfeign.support.FallbackCommand;
import org.springframework.cloud.openfeign.support.ResponseEntityDecoder;
import org.springframework.cloud.openfeign.support.SpringDecoder;
import org.springframework.cloud.openfeign.support.SpringEncoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Scope;

import java.lang.reflect.Type;

/**
 * 自定义feign的自动配置类
 */
@Configuration
@Import(TestFallbackFactory.class)
@ConditionalOnClass(value = {Feign.class, FeignAutoConfiguration.class})
public class MyOpenFeignAutoConfiguration {


    /**
     * 自定义的解码规则
     */
    @Autowired(required = false)
    private FeignDecoderCustomize customize;

    @Bean
    public Contract contract() {
        return new Contract.Default();
    }






    /**
     * 查询定义
     *
     * @return
     */
//    @Bean
//    public QueryMapEncoder queryMapEncoder() {
//        return null;
//    }


    /**
     * 调用处理工程程序
     *
     * @return
     */
//    @Bean
//    public InvocationHandlerFactory invocationHandlerFactory() {
//        return null;
//    }

    /**
     * 控制所有客户端当前需要实现的每个请求设置
     *
     * @return 简单理解：feign之间调用的时间限制
     */
    @Bean
    public Request.Options options() {
        return new Request.Options();
    }

    /**
     * 如果 Spring Cloud CircuitBreaker 在 classpath 上，
     * 并且feign.circuitbreaker.enabled=trueFeign 将使用断路器包装所有方法。
     * <p>
     * 要在每个客户端上禁用 Spring Cloud CircuitBreaker 支持，请创建一个Feign.Builder具有“原型”范围的 vanilla，例如
     * <p>
     * 由此可见feign直接的调用也是支持“熔断”功能的！
     *
     * @return 禁用断路器支持
     */
    @Bean
    @Scope("prototype")
    public Feign.Builder feignBuilder() {
        return Feign.builder();
    }

    /**
     * 配置Feign的日志记录  默认是 NONE
     *
     * @return
     */
    @Bean
    public Logger.Level feignLoggerLevel() {
        return Logger.Level.FULL;
    }

    /**
     * feign调用的重试机制，默认是不重试,直接报错
     *
     * @return
     */
    @Bean
    public Retryer retryer() {
        return new Retryer.Default();
    }

    /**
     * 自定义feign调用日志输出
     * @return
     */
    @Bean
    public Logger logger() {
        return new MyLogConfiguration();
    }




    /**
     * feign的编码器，直接使用官方给出的SpringEncoder解码器
     *
     * @param messageConverters 消息转换器
     * @return 自定义编码器
     */
    @Bean
    public Encoder encoder(ObjectFactory<HttpMessageConverters> messageConverters) {
        return new SpringEncoder(messageConverters);
    }

    /**
     * feign的解码器 直接使用官方给出的ResponseEntityDecoder， (which wraps a SpringDecoder)
     * ResponseEntityDecoder基础自SpringDecoder解码器
     *
     * @return 自定义的编码器
     */
    @Bean
    public Decoder decoder(ObjectFactory<HttpMessageConverters> messageConverters) {
        //方式一：使用new ResponseEntityDecoder(new SpringDecoder(messageConverters)) 官方推介的解码器。
//        return new ResponseEntityDecoder(new SpringDecoder(messageConverters));
        //方式二：自定义符合自己业务的解码器
        ResponseEntityDecoder responseEntityDecoder = new ResponseEntityDecoder(new SpringDecoder(messageConverters));
        return (Response response, Type type) -> {
            //非包装的类
            if (customize == null) {
                return responseEntityDecoder.decode(response, Type.class);
            }
            //包装类的解码
            byte[] bodyData = Util.toByteArray(response.body().asInputStream());
            String bodyStr = Util.decodeOrDefault(bodyData, Util.UTF_8, "");
            String decoderStr = customize.feignDecoder(response, type);
            return responseEntityDecoder.decode(response.toBuilder().body(decoderStr, Util.UTF_8).build(), Type.class);
        };
    }

    /**
     * 当feign的调用返回码不为2xx时 会触发这个错误的编码，我们可以进行相关的业务处理
     *
     * @return ErrorDecoder
     */
    @Bean
    public ErrorDecoder errorDecoder() {
        return new ErrorDecoder.Default();
    }


}
