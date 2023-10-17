package com.test.mockito;

import com.test.junit.assertion.AssertionsRuntimeException;

import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MockConfig<T> {

    // return values for primitives
    private static final Map<Class<?>, Object> CLASS_TO_RETURN_VALUE_FOR_PRIMITIVES = new HashMap<>();

    static {
        CLASS_TO_RETURN_VALUE_FOR_PRIMITIVES.put(byte.class, 0);
        CLASS_TO_RETURN_VALUE_FOR_PRIMITIVES.put(short.class, 0);
        CLASS_TO_RETURN_VALUE_FOR_PRIMITIVES.put(int.class, 0);
        CLASS_TO_RETURN_VALUE_FOR_PRIMITIVES.put(long.class, 0);
        CLASS_TO_RETURN_VALUE_FOR_PRIMITIVES.put(double.class, 0);
        CLASS_TO_RETURN_VALUE_FOR_PRIMITIVES.put(float.class, 0);
        CLASS_TO_RETURN_VALUE_FOR_PRIMITIVES.put(char.class, Character.valueOf('\u0000'));
        CLASS_TO_RETURN_VALUE_FOR_PRIMITIVES.put(boolean.class, false);
    }

    //interface to mock
    private final Class<T> mock;
    private final Map<String, MockMethodSettings<T>> methodNameToSettings = new HashMap<>();

    MockConfig(Class<T> mock) {
        this.mock = mock;
    }

    public MockMethodSettings<T> when(String methodName) {
        MockMethodSettings<T> mockMethodSettings = methodNameToSettings.get(methodName);
        if (mockMethodSettings == null) {
            mockMethodSettings = new MockMethodSettings<T>()
                    .setMock(this);
            methodNameToSettings.put(methodName, mockMethodSettings);
        }

        return mockMethodSettings;
    }


    //method returns proxy object with custom behavior
    @SuppressWarnings("unchecked")
    public T getMock() {
        Class<?>[] interfaces = mock.isInterface() ? new Class<?>[]{mock} : mock.getInterfaces();

        return (T) Proxy.newProxyInstance(mock.getClassLoader(), interfaces, (proxy, method, args) -> {
            String name = method.getName();
            MockMethodSettings<T> settings = methodNameToSettings.get(name);
            if (settings != null && settings.assertArgs(args)) {
                return settings.getReturnedValueIfExist(args);
            }

            Class<?> returnType = method.getReturnType();

            return CLASS_TO_RETURN_VALUE_FOR_PRIMITIVES.getOrDefault(returnType, null);

        });
    }

    public void verify(String methodName, int times) {
        if (times < 0) {
            throw new IllegalArgumentException("times should >=0");
        }
        int counter = -1;
        MockMethodSettings<T> settings = methodNameToSettings.get(methodName);
        if (settings != null) {
            counter = settings.counterOfInvocation;
        }

        if (counter != times) {
            throw new AssertionsRuntimeException(counter, times);
        }
    }


    public void verify(String methodName, List<Object> args, int times) {
        if (times < 0) {
            throw new IllegalArgumentException("times should >=0");
        }
        int counter = -1;
        MockMethodSettings<T> settings = methodNameToSettings.get(methodName);
        if (settings != null) {
            List<MockMethodSettings.MethodResult> methodResults = settings.argumentsToReturnedValue.get(args);
            for (var methodResult : methodResults) {
                counter += methodResult.counter;
            }
        }

        if (counter != times) {
            throw new AssertionsRuntimeException(counter, times);
        }
    }

    public static class MockMethodSettings<T> {
        private MockConfig<T> mockConfig;

        private final Map<List<Object>, List<MethodResult>> argumentsToReturnedValue = new HashMap<>();

        private int counterOfInvocation = 0;

        private MockMethodSettings() {
        }

        private boolean assertArgs(Object[] args) {
            return argumentsToReturnedValue.containsKey(Arrays.asList(args));
        }

        public MockMethodSettings<T> withArgsAndReturnedValue(List<Object> args, Object returnedValue) {
            ArrayList<MethodResult> returnedValues = new ArrayList<>();
            MethodResult result = new MethodResult(returnedValue);
            returnedValues.add(result);
            argumentsToReturnedValue.merge(args, returnedValues, ((odlValues, newValues) -> {
                odlValues.add(result);
                return odlValues;
            }));
            return this;
        }

        public MockMethodSettings<T> withArgsAndThrowException(List<Object> args, RuntimeException exception) {
            ArrayList<MethodResult> returnedValues = new ArrayList<>();
            MethodResult result = new MethodResult(exception);
            returnedValues.add(result);
            argumentsToReturnedValue.merge(args, returnedValues, ((odlValues, newValues) -> {
                odlValues.add(result);
                return odlValues;
            }));
            return this;
        }

        private Object getReturnedValueIfExist(Object[] args) {
            List<MethodResult> returnedValues = argumentsToReturnedValue.get(Arrays.asList(args));
            return (returnedValues != null && !returnedValues.isEmpty()) ? getReturnedValue(returnedValues) : null;
        }

        private Object getReturnedValue(List<MethodResult> returnedValues) {
            int lastIndex = returnedValues.size() - 1;
            int index = Math.min(counterOfInvocation, lastIndex);
            counterOfInvocation++;
            MethodResult methodResult = returnedValues.get(index);
            methodResult.counter++;
            if (!methodResult.isReturned) {
                throw methodResult.toThrow;
            } else {
                return methodResult.returnedValue;
            }
        }

        private MockMethodSettings<T> setMock(MockConfig<T> mockConfig) {
            this.mockConfig = mockConfig;
            return this;
        }


        private static class MethodResult {
            public final Object returnedValue;
            public final RuntimeException toThrow;

            public final boolean isReturned;

            private int counter = 0;

            public MethodResult(Object returnedValue) {
                this.returnedValue = returnedValue;
                this.toThrow = null;
                this.isReturned = true;
            }

            public MethodResult(RuntimeException toThrow) {
                this.toThrow = toThrow;
                this.returnedValue = null;
                this.isReturned = false;
            }
        }
    }

}
