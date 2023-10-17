package com.test.junit.assertion;

public class AssertionsRuntimeException extends RuntimeException {
    private Object actual;
    private Object expected;

    public AssertionsRuntimeException(Object actual, Object expected) {
        this.actual = actual;
        this.expected = expected;
    }

    public AssertionsRuntimeException(String message, Object actual, Object expected) {
        super(message);
        this.actual = actual;
        this.expected = expected;
    }
    public AssertionsRuntimeException() {
    }

    public AssertionsRuntimeException(String message) {
        super(message);
    }

    public Object getActual() {
        return actual;
    }

    public Object getExpected() {
        return expected;
    }
}
