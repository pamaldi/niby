# Secrets Directory

**Secrets Management** - This directory contains sensitive credentials and configuration files for Docker Compose deployments. Used to securely store API keys, database passwords, and other sensitive data required by the Niby application stack.

This directory contains sensitive credentials for Docker Compose.

## Setup

Create these files with your actual credentials:

### 1. anthropic_api_key.txt
```bash
echo "your-anthropic-api-key" > anthropic_api_key.txt
```

### 2. db_password.txt
```bash
echo "your-postgres-password" > db_password.txt
```

## Security Notes

- These files are ignored by git (see .gitignore)
- Use strong passwords in production
- Never commit actual secrets
- Files should contain only the secret value (no newlines)

## Production Deployment

For production environments (Kubernetes, Docker Swarm), use external secrets:

```yaml
secrets:
  anthropic_api_key:
    external: true
  db_password:
    external: true
```
