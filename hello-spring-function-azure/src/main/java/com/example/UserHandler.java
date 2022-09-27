package com.example;

import com.azure.data.tables.TableClient;
import com.azure.data.tables.TableServiceClient;
import com.azure.data.tables.TableServiceClientBuilder;
import com.azure.data.tables.models.ListEntitiesOptions;
import com.azure.data.tables.models.TableEntity;
import com.example.model.User;
import com.microsoft.azure.functions.*;
import com.microsoft.azure.functions.annotation.AuthorizationLevel;
import com.microsoft.azure.functions.annotation.BindingName;
import com.microsoft.azure.functions.annotation.FunctionName;
import com.microsoft.azure.functions.annotation.HttpTrigger;
import org.springframework.cloud.function.adapter.azure.FunctionInvoker;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class UserHandler extends FunctionInvoker<User, User> {

    @FunctionName("createGetDeleteUser")
    public HttpResponseMessage createGetDeleteUser(
            @HttpTrigger(name = "request", methods = {HttpMethod.PUT, HttpMethod.GET, HttpMethod.DELETE}, authLevel = AuthorizationLevel.FUNCTION,
                    route = "subscriptions/{subscriptionId}/resourcegroups/{resourceGroupName}/providers/Microsoft.CustomProviders/resourceproviders/{minirpname}/users/{username}") HttpRequestMessage<Optional<User>> request,
            @BindingName("username") String username,
            ExecutionContext context) {
        context.getLogger().info("Request path: " + request.getHeaders().get("x-ms-customproviders-requestpath"));
        context.getLogger().info("Request uri: " + request.getUri());
        context.getLogger().info("HTTP method: " + request.getHttpMethod());
        context.getLogger().info("Greeting username from path: " + username);

        // TODO: generate id and type

        TableClient tableClient = getTableClient("users");

        User user = null;
        switch (request.getHttpMethod()) {
            case PUT:
                user = request.getBody().get();
                context.getLogger().info("Username from request body: " + user.getName());
                context.getLogger().info("User request body: " + User.toJson(user));
                if (user.getName() == null || user.getName().isBlank()) {
                    user.setName(username);
                }
                TableEntity entity = new TableEntity("users", user.getName())
                        .addProperty("data", User.toJson(user));
                tableClient.createEntity(entity);
                break;
            case GET:
                user = User.fromJson((String)tableClient.getEntity("users", username).getProperties().get("data"));
                break;
            case DELETE:
                tableClient.deleteEntity("users", username);
                break;
        }

        return request
                .createResponseBuilder(HttpStatus.OK)
                .body(user)
                .header("Content-Type", "application/json")
                .build();
    }

    @FunctionName("getUsers")
    public HttpResponseMessage getUsers(
            @HttpTrigger(name = "request", methods = {HttpMethod.GET}, authLevel = AuthorizationLevel.FUNCTION,
                    route = "subscriptions/{subscriptionId}/resourcegroups/{resourceGroupName}/providers/Microsoft.CustomProviders/resourceproviders/{minirpname}/users") HttpRequestMessage<Optional<User>> request,
            ExecutionContext context) {
        context.getLogger().info("Request path: " + request.getHeaders().get("x-ms-customproviders-requestpath"));
        context.getLogger().info("Request uri: " + request.getUri());
        context.getLogger().info("HTTP method: " + request.getHttpMethod());

        TableClient tableClient = getTableClient("users");

        List<User> users = new ArrayList<>();
        ListEntitiesOptions options = new ListEntitiesOptions()
                .setFilter(String.format("PartitionKey eq '%s'", "users"));
        for (TableEntity entity : tableClient.listEntities(options, null, null)) {
            users.add(User.fromJson((String) entity.getProperties().get("data")));
        }

        return request
                .createResponseBuilder(HttpStatus.OK)
                .body(Map.of("value", users))
                .header("Content-Type", "application/json")
                .build();
    }

    @FunctionName("userAction")
    public HttpResponseMessage executeUserAction(
            @HttpTrigger(name = "request", methods = {HttpMethod.POST}, authLevel = AuthorizationLevel.FUNCTION,
                    route = "subscriptions/{subscriptionId}/resourcegroups/{resourceGroupName}/providers/Microsoft.CustomProviders/resourceproviders/{minirpname}/users/{username}/action") HttpRequestMessage<Optional<User>> request,
            @BindingName("username") String username,
            ExecutionContext context) {
        context.getLogger().info("Request path: " + request.getHeaders().get("x-ms-customproviders-requestpath"));
        context.getLogger().info("Request uri: " + request.getUri());
        context.getLogger().info("HTTP method: " + request.getHttpMethod());
        context.getLogger().info("Greeting username from path: " + username);

        User updatedUser = request.getBody().get();
        context.getLogger().info("Updated username from request body: " + updatedUser.getName());
        context.getLogger().info("Updated user from request body: " + User.toJson(updatedUser));

        TableClient tableClient = getTableClient("users");
        User user = User.fromJson((String)tableClient.getEntity("users", username).getProperties().get("data"));
        user.setProperties(updatedUser.getProperties());
        context.getLogger().info("Updated user to be persisted: " + User.toJson(user));
        tableClient.upsertEntity(new TableEntity("users", user.getName()).addProperty("data", User.toJson(user)));

        return request
                .createResponseBuilder(HttpStatus.OK)
                .body(handleRequest(user, context))
                .header("Content-Type", "application/json")
                .build();
    }

    private TableClient getTableClient(String tableName) {
        TableServiceClient tableServiceClient = new TableServiceClientBuilder()
                .connectionString(System.getenv("AzureWebJobsStorage"))
                .buildClient();
        tableServiceClient.createTableIfNotExists(tableName);
        return tableServiceClient.getTableClient(tableName);
    }
}
