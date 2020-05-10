#!/bin/bash
# Copies code style into project's idea folder
# @author Bojan Kseneman

selfPath=$(dirname "$0")
projectRoot="$selfPath/../"
styleFileName="Style.xml"

targetFolder="$projectRoot/.idea/codeStyles"
targetFile="$targetFolder/Project.xml"
mkdir -p "$targetFolder" && cp "$projectRoot/$styleFileName" "$targetFile" && echo "Done. Restart IDE for changes to kick in." || echo "Oooops... we ran into an issue there" ; exit 1
