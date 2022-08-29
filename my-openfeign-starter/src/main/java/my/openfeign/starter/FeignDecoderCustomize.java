package my.openfeign.starter;

import feign.Response;

import java.lang.reflect.Type;

public interface FeignDecoderCustomize {

    String feignDecoder(Response response, Type type);
}
