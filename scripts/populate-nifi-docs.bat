@echo off
REM Script to populate nifi-doc-data volume with documentation files (Windows)

echo Populating nifi-doc-data volume...

REM Create the volume if it doesn't exist
docker volume create niby_nifi-doc-data 2>nul

REM Use a temporary container to copy files
docker run --rm -v "%cd%\niby-rag\nifi-doc:/source:ro" -v niby_nifi-doc-data:/destination alpine sh -c "cp -r /source/* /destination/"

echo Successfully populated nifi-doc-data volume
echo Volume contents:
docker run --rm -v niby_nifi-doc-data:/data alpine ls -lah /data

pause
