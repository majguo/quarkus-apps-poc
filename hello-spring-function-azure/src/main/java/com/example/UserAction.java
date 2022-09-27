package com.example;

import com.example.model.User;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.function.Function;

@Component
public class UserAction implements Function<Mono<User>, Mono<User>> {

    public Mono<User> apply(Mono<User> mono) {
        return mono.map(user -> user);
    }
}
