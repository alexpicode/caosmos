#!/bin/sh

# Directory where the app looks for configuration files
CONFIG_DIR="/app/config"
# Directory where default configurations are stored in the image
DEFAULTS_DIR="/app/config-defaults"

echo "Checking configuration in $CONFIG_DIR..."

# If the configuration directory is empty, copy the default values
if [ -z "$(ls -A $CONFIG_DIR 2>/dev/null)" ]; then
    echo "Configuration directory is empty or missing. Copying default manifests..."
    mkdir -p $CONFIG_DIR
    cp -r $DEFAULTS_DIR/. $CONFIG_DIR/
    echo "Initial configuration copied successfully."
else
    echo "Existing configuration detected. Skipping default files copy."
fi

# Ensure the spring user has write permissions (crucial for bind mounts)
# Note: This might require the container to run as root initially or careful UID management
# In most development environments, the bind mount inherits host permissions.

echo "Starting Caosmos Engine..."
# Execute the application (replaces the original ENTRYPOINT command)
exec java -jar app.jar
