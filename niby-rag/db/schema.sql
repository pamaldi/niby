-- Create user if not exists
DO $$
BEGIN
  IF NOT EXISTS (SELECT FROM pg_catalog.pg_user WHERE usename = 'niby') THEN
    CREATE USER niby WITH PASSWORD 'niby';
END IF;
END
$$;

-- Create schema
CREATE SCHEMA IF NOT EXISTS niby;

-- Grant privileges to the user
GRANT ALL PRIVILEGES ON SCHEMA niby TO niby;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA niby TO niby;
ALTER DEFAULT PRIVILEGES IN SCHEMA niby GRANT ALL ON TABLES TO niby;

-- Enable pgvector extension if not already enabled
CREATE EXTENSION IF NOT EXISTS vector;

-- Note: The nifi_doc_embeddings table will be created automatically by LangChain4j
-- when the application starts (quarkus.langchain4j.pgvector.create-table=true)