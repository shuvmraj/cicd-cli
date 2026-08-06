#!/bin/bash
# Installation script for the 'cicd' CLI tool downloaded from GitHub Releases

INSTALL_DIR="$HOME/.cicd/bin"
JAR_NAME="cicd-cli.jar"
BIN_NAME="cicd"
RELEASE_URL="https://github.com/shuvmraj/cicd-cli/releases/download/v1.0.0/cicd-cli.jar"

echo "=== Installing cicd CLI ==="

# 1. Create installation directory
mkdir -p "$INSTALL_DIR"

# 2. Download the JAR file from GitHub Releases
echo "Downloading executable from GitHub Releases..."
if ! curl -L -o "$INSTALL_DIR/$JAR_NAME" "$RELEASE_URL"; then
    echo "Error: Failed to download the JAR from $RELEASE_URL."
    echo "Please check your internet connection or verify the release exists."
    exit 1
fi

# 3. Generate the wrapper script
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

# 4. Make wrapper script executable
chmod +x "$INSTALL_DIR/$BIN_NAME"

# 5. Try to symlink to /usr/local/bin for global command access
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
