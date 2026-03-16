# Use a minimal base image for native binaries
FROM alpine:latest

# Install dependencies (if needed)
RUN apk add --no-cache libc6-compat

# Set working directory
WORKDIR /app

# Copy the native binary [Linux only]
COPY webapplication/target/webapplication ./fastbite

# Make it executable
RUN chmod +x fastbite

# Expose the port
EXPOSE 8080

# Run the binary
ENTRYPOINT ["./fastbite"]