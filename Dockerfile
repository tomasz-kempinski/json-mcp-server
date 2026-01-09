# Use official Node.js LTS image
FROM node:22-alpine

# Install jq binary (required dependency)
RUN apk add --no-cache jq

# Set working directory
WORKDIR /app

# Copy package files
COPY package*.json ./

# Install dependencies using npm
RUN npm ci

# Copy application files
COPY . .

# Verify jq installation
RUN jq --version

# Expose default HTTP port (if using HTTP transport)
EXPOSE 3000

# Default command (stdio transport)
CMD ["node", "index.js", "--transport=http", "--port=3000", "--verbose=true"]
