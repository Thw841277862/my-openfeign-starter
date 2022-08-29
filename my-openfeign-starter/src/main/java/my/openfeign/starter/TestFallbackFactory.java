package my.openfeign.starter;

import feign.hystrix.FallbackFactory;

/**
 * feign错误回退机制
 *
 * @param <T>
 */
public class TestFallbackFactory<T> implements FallbackFactory<T> {

    @Override
    public T create(Throwable cause) {
        System.out.println("----------触发了错误回退--------------");
        return null;
    }
}
