package com.example;

import com.example.model.User;
import com.microsoft.azure.functions.ExecutionContext;
import org.junit.jupiter.api.Test;
import org.springframework.cloud.function.adapter.azure.FunctionInvoker;
import reactor.core.publisher.Mono;

import java.util.logging.Logger;

import static org.assertj.core.api.Assertions.assertThat;

public class UserActionTest {

    @Test
    public void test() {
        Mono<User> result = new UserAction().apply(Mono.just(new User("foo")));
        assertThat(result.block().getName()).isEqualTo("foo");
    }

    @Test
    public void start() {
        FunctionInvoker<User, User> handler = new FunctionInvoker<>(
                UserAction.class);
        User result = handler.handleRequest(new User("foo"), new ExecutionContext() {
            @Override
            public Logger getLogger() {
                return Logger.getLogger(UserActionTest.class.getName());
            }

            @Override
            public String getInvocationId() {
                return "id1";
            }

            @Override
            public String getFunctionName() {
                return "hello";
            }
        });
        handler.close();
        assertThat(result.getName()).isEqualTo("foo");
    }
}
