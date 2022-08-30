package my.openfeign.starter;

import feign.Response;

import java.lang.reflect.Type;

/**
 * 自定义解密 扩展接口
 */
public interface FeignDecoderCustomize {

    String feignDecoder(Response response, Type type);
}
