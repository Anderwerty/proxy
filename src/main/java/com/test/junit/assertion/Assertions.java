package com.test.junit.assertion;

import java.util.Objects;

public final class Assertions {
    private Assertions() {

    }

    public static void asserEquals(Object actual, Object expected) {
        if (!Objects.equals(actual, expected)) {
            throw new AssertionsRuntimeException(actual, expected);
        }
    }

    public static <E extends Exception> E assertThrow(Executor executor, Class<E> aClass) {
        try {
            executor.execute();
            return null;
        } catch (Exception e){
            if(!e.getClass().equals(aClass)){
               throw new AssertionsRuntimeException(e.getClass(), aClass);
            } else {
                return (E) e;
            }
        }
    }
}
