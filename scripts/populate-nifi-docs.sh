#!/bin/bash
# Script to populate nifi-doc-data volume with documentation files

set -e

echo "Populating nifi-doc-data volume..."

# Create the volume if it doesn't exist
docker volume create niby_nifi-doc-data 2>/dev/null || true

# Use a temporary container to copy files
docker run --rm \
  -v "$(pwd)/niby-rag/nifi-doc:/source:ro" \
  -v niby_nifi-doc-data:/destination \
  alpine sh -c "cp -r /source/* /destination/"

echo "✓ Successfully populated nifi-doc-data volume"
echo "Volume contents:"
docker run --rm -v niby_nifi-doc-data:/data alpine ls -lah /data
