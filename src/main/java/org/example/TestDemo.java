package org.example;

import com.test.junit.assertion.Assertions;
import com.test.junit.anotation.Test;
import com.test.junit.TestRunner;
import com.test.mockito.MockConfig;
import com.test.mockito.SimpleMockito;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static com.test.junit.assertion.Assertions.asserEquals;
import static com.test.junit.assertion.Assertions.assertThrow;

public class TestDemo {
    public static void main(String[] args) {
        TestRunner testRunner = new TestRunner();
        testRunner.register(CalculatorTest.class);
        testRunner.run();
    }
}

class Calculator {
    int sum(int a, int b) {
        return a + b;
    }

    int div(int a, int b) {
        if (b == 0) {
            throw new IllegalArgumentException();
        }
        return a / b;
    }
}

class CalculatorTest {
    Calculator calculator = new Calculator();

    public CalculatorTest() {
    }

    @Test
    public void testSumMethodShouldReturnPositiveFor2PositiveArgs() {
        int actual = calculator.sum(1, 2);
        int expected = 3;
        asserEquals(actual, expected);
    }

    @Test
    public void failedSumTest() {
        int actual = calculator.sum(1, 2);
        int expected = 300;
        asserEquals(actual, expected);
    }

    @Test
    public void testDivMethod() {
        int actual = calculator.div(1, 2);
        int expected = 0;
        asserEquals(actual, expected);
    }

    @Test
    public void testDivMethodShouldThrowException() {
        IllegalArgumentException exception =
                assertThrow(() -> calculator.div(1, 0), IllegalArgumentException.class);
        asserEquals(exception.getMessage(), null);
    }
}

interface UserRepository {

    Optional<User> findById(Integer id);
}

class UserRepositoryImp implements UserRepository {

    private static final Map<Integer, User> ID_TO_USER = new HashMap<>();

    static {
        ID_TO_USER.put(1, new User(1, "Alex1"));
        ID_TO_USER.put(2, new User(2, "Alex2"));
        ID_TO_USER.put(3, new User(3, "Alex3"));
    }

    public Optional<User> findById(Integer id) {
        return Optional.ofNullable(ID_TO_USER.get(id));
    }

}

class User {

    private final Integer id;
    private final String name;

    public User(Integer id, String name) {
        this.id = id;
        this.name = name;
    }

    public Integer getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", name='" + name + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(id, user.id) && Objects.equals(name, user.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name);
    }
}

class UserService {
    private final UserRepository userRepository;

    UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User findById(int id) {
        return userRepository.findById(id).orElseThrow(() -> new RuntimeException());
    }
}

class UserServiceTest {

    @Test
    public void testFindByIdShouldReturnUser() {
        User user = new User(100, "Andrii");
        MockConfig<UserRepository> mockConfig = SimpleMockito.mock(UserRepository.class);
        mockConfig.when("findById").withArgsAndReturnedValue(Arrays.asList(100), Optional.of(user));
        UserRepository mock = mockConfig.getMock();

        UserService userService = new UserService(mock);
        User actual = userService.findById(100);
        Assertions.asserEquals(actual, user);
    }
}
