package io.github.tomaszkempinski.springai.jsonmcpserver.feature.query;

import lombok.RequiredArgsConstructor;
import org.springaicommunity.mcp.annotation.McpTool;
import org.springaicommunity.mcp.annotation.McpToolParam;
import org.springaicommunity.mcp.context.McpSyncRequestContext;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class JsonQueryMcpTools {

  private final JsonQueryService jsonQueryService;

  @McpTool(
      name = "query_json",
      description =
          """
                Searches a local JSON file using JsonPath query and returns the result as a JSON string

                Parameters:
                - absolutePath: absolute path to the local JSON file
                - query: JsonPath expression

                Examples:
                - $.users[*].name
                - $.orders[?(@.total > 100)]
                - $.users[?(@.active == true && @.age > 30)]
                """)
  public String queryJson(
      McpSyncRequestContext context,
      @McpToolParam(description = "The absolute path to the local JSON file") String absolutePath,
      @McpToolParam(description = "The JsonPath query to execute (e.g. '$.store.book[*].author')")
          String query) {

    return jsonQueryService.query(absolutePath, query);
  }
}
