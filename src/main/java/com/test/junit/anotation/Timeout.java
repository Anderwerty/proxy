package com.test.junit.anotation;

public @interface Timeout {
    int time();

    TimeUnit timeUnit() default TimeUnit.MILLISECOND;
}
