#!/bin/bash
# ─────────────────────────────────────────────
#  Build & Run: Distributed E-Commerce Order Engine
# ─────────────────────────────────────────────

set -e

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
SRC_DIR="$SCRIPT_DIR/src"
OUT_DIR="$SCRIPT_DIR/out"

echo "🔨 Compiling..."
mkdir -p "$OUT_DIR"
find "$SRC_DIR" -name "*.java" > /tmp/sources.txt
javac -d "$OUT_DIR" @/tmp/sources.txt

echo "✅ Compilation successful!"
echo ""
echo "🚀 Starting E-Commerce Order Engine..."
echo ""
cd "$OUT_DIR"
java Main
