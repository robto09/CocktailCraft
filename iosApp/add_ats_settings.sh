#!/bin/bash

# Script to add App Transport Security settings to Info.plist
# Run this after building the app

INFO_PLIST_PATH="$1"

if [ -z "$INFO_PLIST_PATH" ]; then
    echo "Usage: $0 <path-to-Info.plist>"
    echo "Example: $0 /path/to/CocktailCraft.app/Info.plist"
    exit 1
fi

if [ ! -f "$INFO_PLIST_PATH" ]; then
    echo "Error: Info.plist not found at $INFO_PLIST_PATH"
    exit 1
fi

# Add ATS settings using PlistBuddy
/usr/libexec/PlistBuddy -c "Add :NSAppTransportSecurity dict" "$INFO_PLIST_PATH" 2>/dev/null
/usr/libexec/PlistBuddy -c "Add :NSAppTransportSecurity:NSExceptionDomains dict" "$INFO_PLIST_PATH" 2>/dev/null
/usr/libexec/PlistBuddy -c "Add :NSAppTransportSecurity:NSExceptionDomains:thecocktaildb.com dict" "$INFO_PLIST_PATH" 2>/dev/null
/usr/libexec/PlistBuddy -c "Add :NSAppTransportSecurity:NSExceptionDomains:thecocktaildb.com:NSIncludesSubdomains bool YES" "$INFO_PLIST_PATH" 2>/dev/null
/usr/libexec/PlistBuddy -c "Add :NSAppTransportSecurity:NSExceptionDomains:thecocktaildb.com:NSTemporaryExceptionAllowsInsecureHTTPLoads bool YES" "$INFO_PLIST_PATH" 2>/dev/null

echo "App Transport Security settings added successfully!"