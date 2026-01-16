# json-mcp-server
MCP server for JSON files

## Docker

### Build
```bash
docker build -t json-mcp-server .
```

### Run (HTTP STREAMABLE mode)
```bash
docker run --name json-mcp-server -v /path/to/local/json/files:/tmp -p 3000:3000 -e MCP_LOG_LEVEL=DEBUG json-mcp-server
```

### Run (Stdio mode)
```bash
docker run --name json-mcp-server -i --rm -v /path/to/local/json/files:/tmp -e SPRING_PROFILES_ACTIVE=stdio json-mcp-server
```

> **Note:** Mounting a local directory to `/tmp` allows the server to access your JSON files. When calling tools, use absolute paths starting with `/tmp/`.

### Configuration Parameters
The following environment variables can be used to configure the server:

| Environment Variable | Default | Description |
|----------------------|---------|-------------|
| `SERVER_PORT` | `3000`  | Port for the web server |
| `MCP_LOG_LEVEL` | `INFO`  | Logging level for MCP components |
| `SPRING_PROFILES_ACTIVE` | `http`  | Set to `stdio` to run in stdio mode |
| `MCP_TOOLS_FILE_ALLOWED_EXTENSIONS` | `.json` | Comma-separated list of allowed file extensions |
