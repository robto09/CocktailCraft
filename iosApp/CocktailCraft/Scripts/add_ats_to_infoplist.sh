#!/bin/bash

# This script adds App Transport Security settings to the generated Info.plist
# It should be added as a Run Script Build Phase in Xcode

set -e

echo "Adding App Transport Security settings..."

# The Info.plist location in the built app
INFO_PLIST="${TARGET_BUILD_DIR}/${INFOPLIST_PATH}"

# Add ATS settings using PlistBuddy
/usr/libexec/PlistBuddy -c "Add :NSAppTransportSecurity dict" "$INFO_PLIST" 2>/dev/null || true
/usr/libexec/PlistBuddy -c "Add :NSAppTransportSecurity:NSExceptionDomains dict" "$INFO_PLIST" 2>/dev/null || true
/usr/libexec/PlistBuddy -c "Add :NSAppTransportSecurity:NSExceptionDomains:thecocktaildb.com dict" "$INFO_PLIST" 2>/dev/null || true
/usr/libexec/PlistBuddy -c "Set :NSAppTransportSecurity:NSExceptionDomains:thecocktaildb.com:NSIncludesSubdomains YES" "$INFO_PLIST" 2>/dev/null || /usr/libexec/PlistBuddy -c "Add :NSAppTransportSecurity:NSExceptionDomains:thecocktaildb.com:NSIncludesSubdomains bool YES" "$INFO_PLIST"
/usr/libexec/PlistBuddy -c "Set :NSAppTransportSecurity:NSExceptionDomains:thecocktaildb.com:NSTemporaryExceptionAllowsInsecureHTTPLoads YES" "$INFO_PLIST" 2>/dev/null || /usr/libexec/PlistBuddy -c "Add :NSAppTransportSecurity:NSExceptionDomains:thecocktaildb.com:NSTemporaryExceptionAllowsInsecureHTTPLoads bool YES" "$INFO_PLIST"

echo "App Transport Security settings added successfully!"