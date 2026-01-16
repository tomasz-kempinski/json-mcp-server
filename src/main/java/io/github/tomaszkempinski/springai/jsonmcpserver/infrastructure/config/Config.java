package io.github.tomaszkempinski.springai.jsonmcpserver.infrastructure.config;

import com.networknt.schema.Schema;
import com.networknt.schema.SchemaLocation;
import com.networknt.schema.SchemaRegistry;
import com.networknt.schema.dialect.Dialects;
import com.saasquatch.jsonschemainferrer.JsonSchemaInferrer;
import com.saasquatch.jsonschemainferrer.SpecVersion;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Config {

  @Bean
  public JsonSchemaInferrer jsonSchemaInferrer() {
    return JsonSchemaInferrer.newBuilder().setSpecVersion(SpecVersion.DRAFT_2020_12).build();
  }

  @Bean
  public SchemaRegistry schemaRegistry() {
    return SchemaRegistry.withDialect(Dialects.getDraft202012());
  }

  @Bean
  public Schema metaSchema(SchemaRegistry schemaRegistry) {
    return schemaRegistry.getSchema(SchemaLocation.of(Dialects.getDraft202012().getId()));
  }
}
