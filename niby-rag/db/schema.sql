-- Create user if not exists
DO $$
BEGIN
  IF NOT EXISTS (SELECT FROM pg_catalog.pg_user WHERE usename = 'niby') THEN
    CREATE USER niby WITH PASSWORD 'niby';
END IF;
END
$$;

-- Create schema
CREATE SCHEMA IF NOT EXISTS "niby-emb";

-- Grant privileges to the user
GRANT ALL PRIVILEGES ON SCHEMA "niby-emb" TO niby;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA "niby-emb" TO niby;
ALTER DEFAULT PRIVILEGES IN SCHEMA "niby-emb" GRANT ALL ON TABLES TO niby;

-- Enable pgvector extension if not already enabled
CREATE EXTENSION IF NOT EXISTS vector;

-- Create the nifi_doc table in the niby-emb schema
CREATE TABLE "niby-emb".nifi_doc (
                                     id BIGSERIAL PRIMARY KEY,
                                     content TEXT,
                                     metadata JSONB,
                                     embedding VECTOR(1536),
                                     created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create an index for similarity searches
CREATE INDEX ON "niby-emb".nifi_doc USING hnsw (embedding vector_cosine_ops);

-- Grant privileges on the table to niby user
GRANT ALL PRIVILEGES ON TABLE "niby-emb".nifi_doc TO niby;
GRANT USAGE, SELECT ON SEQUENCE "niby-emb".nifi_doc_id_seq TO niby;