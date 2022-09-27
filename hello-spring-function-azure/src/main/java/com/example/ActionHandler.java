package com.example;

import com.example.model.User;
import com.microsoft.azure.functions.*;
import com.microsoft.azure.functions.annotation.AuthorizationLevel;
import com.microsoft.azure.functions.annotation.FunctionName;
import com.microsoft.azure.functions.annotation.HttpTrigger;
import org.springframework.cloud.function.adapter.azure.FunctionInvoker;

import java.util.Optional;

public class ActionHandler extends FunctionInvoker<User, User> {

    @FunctionName("action")
    public HttpResponseMessage execute(
            @HttpTrigger(name = "request", methods = {HttpMethod.POST}, authLevel = AuthorizationLevel.FUNCTION, route = "hello/action") HttpRequestMessage<Optional<User>> request,
            ExecutionContext context) {
        String requestPath = request.getHeaders().get("x-ms-customproviders-requestpath");
        User user = request.getBody()
                .filter((u -> u.getName() != null))
                .orElseGet(() -> new User(
                        request.getQueryParameters()
                                .getOrDefault("name", "world")));

        context.getLogger().info("Request path: " + requestPath);
        context.getLogger().info("Request uri: " + request.getUri());
        context.getLogger().info("HTTP method: " + request.getHttpMethod());
        context.getLogger().info("Greeting user name from route 'hello/action': " + user.getName());

        return request
                .createResponseBuilder(HttpStatus.OK)
                .body(handleRequest(user, context))
                .header("Content-Type", "application/json")
                .build();
    }
}
