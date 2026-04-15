package com.whylog.server.global.config;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.RequestBody;
import io.swagger.v3.oas.models.responses.ApiResponse;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerSchemaNamingConfig {

    private static final PropertyNamingStrategies.SnakeCaseStrategy SNAKE_CASE =
            new PropertyNamingStrategies.SnakeCaseStrategy();

    @Bean
    public OpenApiCustomizer snakeCaseSchemaCustomizer() {
        return openApi -> {
            IdentityHashMap<Schema<?>, Boolean> visited = new IdentityHashMap<>();
            applyToComponentSchemas(openApi, visited);
            applyToPathSchemas(openApi, visited);
        };
    }

    private void applyToComponentSchemas(OpenAPI openApi, IdentityHashMap<Schema<?>, Boolean> visited) {
        if (openApi.getComponents() == null || openApi.getComponents().getSchemas() == null) {
            return;
        }

        for (Schema<?> schema : openApi.getComponents().getSchemas().values()) {
            renameSchemaProperties(schema, visited);
        }
    }

    private void applyToPathSchemas(OpenAPI openApi, IdentityHashMap<Schema<?>, Boolean> visited) {
        if (openApi.getPaths() == null) {
            return;
        }

        openApi.getPaths().values().forEach(pathItem -> pathItem.readOperations().forEach(operation -> {
            if (operation.getRequestBody() != null) {
                renameRequestBodySchema(operation.getRequestBody(), visited);
            }

            if (operation.getResponses() != null) {
                for (ApiResponse response : operation.getResponses().values()) {
                    renameResponseSchema(response, visited);
                }
            }
        }));
    }

    private void renameRequestBodySchema(RequestBody requestBody, IdentityHashMap<Schema<?>, Boolean> visited) {
        if (requestBody.getContent() == null) {
            return;
        }

        for (MediaType mediaType : requestBody.getContent().values()) {
            renameSchemaProperties(mediaType.getSchema(), visited);
        }
    }

    private void renameResponseSchema(ApiResponse response, IdentityHashMap<Schema<?>, Boolean> visited) {
        if (response.getContent() == null) {
            return;
        }

        for (MediaType mediaType : response.getContent().values()) {
            renameSchemaProperties(mediaType.getSchema(), visited);
        }
    }

    private void renameSchemaProperties(Schema<?> schema, IdentityHashMap<Schema<?>, Boolean> visited) {
        if (schema == null || visited.containsKey(schema)) {
            return;
        }
        visited.put(schema, Boolean.TRUE);

        renameObjectProperties(schema);
        renameRequiredFields(schema);

        if (schema.getItems() != null) {
            renameSchemaProperties(schema.getItems(), visited);
        }

        Object additionalProperties = schema.getAdditionalProperties();
        if (additionalProperties instanceof Schema<?> additionalSchema) {
            renameSchemaProperties(additionalSchema, visited);
        }

        if (schema.getAllOf() != null) {
            for (Schema<?> child : schema.getAllOf()) {
                renameSchemaProperties(child, visited);
            }
        }
        if (schema.getOneOf() != null) {
            for (Schema<?> child : schema.getOneOf()) {
                renameSchemaProperties(child, visited);
            }
        }
        if (schema.getAnyOf() != null) {
            for (Schema<?> child : schema.getAnyOf()) {
                renameSchemaProperties(child, visited);
            }
        }
        if (schema.getNot() != null) {
            renameSchemaProperties(schema.getNot(), visited);
        }
    }

    private void renameObjectProperties(Schema<?> schema) {
        Map<String, Schema> properties = schema.getProperties();
        if (properties == null || properties.isEmpty()) {
            return;
        }

        LinkedHashMap<String, Schema> renamed = new LinkedHashMap<>();
        for (Map.Entry<String, Schema> entry : properties.entrySet()) {
            renamed.put(toSnakeCase(entry.getKey()), entry.getValue());
        }
        schema.setProperties(renamed);
    }

    private void renameRequiredFields(Schema<?> schema) {
        List<String> required = schema.getRequired();
        if (required == null || required.isEmpty()) {
            return;
        }

        List<String> renamedRequired = new ArrayList<>(required.size());
        for (String name : required) {
            renamedRequired.add(toSnakeCase(name));
        }
        schema.setRequired(renamedRequired);
    }

    private String toSnakeCase(String value) {
        return SNAKE_CASE.translate(value);
    }
}
