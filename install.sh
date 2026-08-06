#!/bin/bash
# Installation script for the 'cicd' CLI tool

# Define target directories
INSTALL_DIR="$HOME/.cicd/bin"
JAR_NAME="cicd-cli.jar"
BIN_NAME="cicd"

echo "=== Installing cicd CLI ==="

# 1. Create installation directory
mkdir -p "$INSTALL_DIR"

# 2. Locate jar file from source path
SOURCE_JAR=""
if [ -f "./cicd-cli/target/cicd-cli-1.0-SNAPSHOT.jar" ]; then
    SOURCE_JAR="./cicd-cli/target/cicd-cli-1.0-SNAPSHOT.jar"
elif [ -f "./target/cicd-cli-1.0-SNAPSHOT.jar" ]; then
    SOURCE_JAR="./target/cicd-cli-1.0-SNAPSHOT.jar"
elif [ -f "./cicd-cli.jar" ]; then
    SOURCE_JAR="./cicd-cli.jar"
fi

if [ -z "$SOURCE_JAR" ]; then
    echo "Error: Could not find cicd-cli-1.0-SNAPSHOT.jar in current directory."
    echo "Please place the JAR file in this folder and run again."
    exit 1
fi

# 3. Copy jar file to install location
cp "$SOURCE_JAR" "$INSTALL_DIR/$JAR_NAME"

# 4. Generate the wrapper script
cat << 'EOF' > "$INSTALL_DIR/$BIN_NAME"
#!/bin/bash
# Executable wrapper for cicd CLI
JAR_PATH="$HOME/.cicd/bin/cicd-cli.jar"
if [ ! -f "$JAR_PATH" ]; then
    echo "Error: cicd-cli.jar is missing from $JAR_PATH."
    exit 1
fi
java -jar "$JAR_PATH" "$@"
EOF

# 5. Make wrapper script executable
chmod +x "$INSTALL_DIR/$BIN_NAME"

# 6. Try to symlink to /usr/local/bin for global command access
echo "Attempting to create global symlink in /usr/local/bin..."
if sudo ln -sf "$INSTALL_DIR/$BIN_NAME" /usr/local/bin/$BIN_NAME 2>/dev/null; then
    echo "Success: Global symlink created!"
    echo "You can now type: cicd"
else
    echo "Permission denied for /usr/local/bin."
    echo "Adding install directory to your shell PATH (~/.zshrc)..."
    
    # Add to shell profile if not already present
    if ! grep -q "$INSTALL_DIR" "$HOME/.zshrc" 2>/dev/null; then
        echo 'export PATH="$HOME/.cicd/bin:$PATH"' >> "$HOME/.zshrc"
        echo "PATH updated in ~/.zshrc."
    fi
    echo "Installation finished! Please run 'source ~/.zshrc' to start using 'cicd'."
fi
echo "============================="
