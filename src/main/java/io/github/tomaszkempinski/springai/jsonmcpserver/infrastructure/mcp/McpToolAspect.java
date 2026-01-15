package io.github.tomaszkempinski.springai.jsonmcpserver.infrastructure.mcp;

import java.util.Arrays;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springaicommunity.mcp.annotation.McpTool;
import org.springaicommunity.mcp.context.McpSyncRequestContext;
import org.springframework.stereotype.Component;

@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class McpToolAspect {

  private final McpErrorHandler mcpErrorHandler;

  @Around("@annotation(mcpTool)")
  public Object handleToolLifecycle(ProceedingJoinPoint joinPoint, McpTool mcpTool) {
    String toolName = mcpTool.name();
    Object[] args = joinPoint.getArgs();

    McpSyncRequestContext context =
        Arrays.stream(args)
            .filter(McpSyncRequestContext.class::isInstance)
            .map(McpSyncRequestContext.class::cast)
            .findFirst()
            .orElseThrow(
                () ->
                    new IllegalStateException(
                        "McpSyncRequestContext not found in tool arguments for tool: " + toolName));

    try {
      logAndSendInfo(context, "Starting execution of tool: " + toolName);
      Object result = joinPoint.proceed();

      logAndSendInfo(context, "Tool '" + toolName + "' completed successfully");
      return result != null ? result.toString() : "";
    } catch (Throwable t) {
      return mcpErrorHandler.handleException(t, context);
    }
  }

  private void logAndSendInfo(McpSyncRequestContext context, String message) {
    log.info(message);
    context.info(message);
  }
}
