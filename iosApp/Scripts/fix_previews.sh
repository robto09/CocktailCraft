#!/bin/bash
# Fix duplicate preview dylib issue

# Exit if not in Xcode build
if [ -z "$BUILT_PRODUCTS_DIR" ]; then
    exit 0
fi

# Remove duplicate preview files
PREVIEW_DYLIB="$BUILT_PRODUCTS_DIR/$PRODUCT_NAME.app/__preview.dylib"
if [ -f "$PREVIEW_DYLIB" ]; then
    echo "Removing duplicate preview dylib: $PREVIEW_DYLIB"
    rm -f "$PREVIEW_DYLIB"
fi

# Remove preview-related files from build directory
find "$BUILT_PRODUCTS_DIR" -name "*preview*" -type f -delete 2>/dev/null || true

echo "Preview cleanup completed"