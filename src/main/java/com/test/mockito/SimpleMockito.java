package com.test.mockito;

public class SimpleMockito {
    public static <T> MockConfig<T> mock(Class<T> aClass) {
        return new MockConfig<>(aClass);
    }

    public <T> void verifyThat(MockConfig<T> config, String method, int times) {
        config.verify(method, times);
    }

}


