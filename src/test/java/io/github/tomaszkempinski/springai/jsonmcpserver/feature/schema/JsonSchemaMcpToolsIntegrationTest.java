package io.github.tomaszkempinski.springai.jsonmcpserver.feature.schema;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.springaicommunity.mcp.context.McpSyncRequestContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class JsonSchemaMcpToolsIntegrationTest {

  @Autowired private JsonSchemaMcpTools jsonSchemaMcpTools;

  @Test
  void testAopProxyWorking() {
    McpSyncRequestContext context = mock(McpSyncRequestContext.class);

    Object result = jsonSchemaMcpTools.validateJsonSchema(context, "{}", null);

    assertNotNull(result);
    verify(context, atLeastOnce()).info(anyString());
  }
}
