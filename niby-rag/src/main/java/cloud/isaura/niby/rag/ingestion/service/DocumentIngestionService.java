package cloud.isaura.niby.rag.ingestion.service;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.DocumentParser;
import dev.langchain4j.data.document.DocumentSplitter;
import dev.langchain4j.data.document.loader.FileSystemDocumentLoader;
import dev.langchain4j.data.document.parser.TextDocumentParser;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import io.quarkiverse.langchain4j.pgvector.PgVectorEmbeddingStore;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import static dev.langchain4j.data.document.splitter.DocumentSplitters.recursive;

 @ApplicationScoped
public class DocumentIngestionService
{
    private static final Logger LOG = LoggerFactory.getLogger(DocumentIngestionService.class);

    @Inject
    PgVectorEmbeddingStore embeddingStore;

    @Inject
    EmbeddingModel embeddingModel;

    /**
     * Ingest with file type detection and custom parsers
     */
    public void ingestWithCustomParsers(String directoryPath) throws IOException
    {
        Path path = Path.of(directoryPath);

        try (Stream<Path> paths = Files.walk(path)) {
            List<Document> documents = new ArrayList<>();

            paths.filter(Files::isRegularFile)
                    .forEach(file -> {
                        Document doc = loadDocumentWithParser(file);
                        if (doc != null) {
                            documents.add(doc);
                        }
                    });

            if (!documents.isEmpty()) {
                ingestDocuments(documents);
            }
        }
    }

    private Document loadDocumentWithParser(Path file) {
        String fileName = file.getFileName().toString().toLowerCase();
        DocumentParser parser;

        try {
                parser = new TextDocumentParser();
                return FileSystemDocumentLoader.loadDocument(file, parser);
        } catch (Exception e) {
            LOG.error( "Failed to load document: %s", file);
            return null;
        }
    }

    private void ingestDocuments(List<Document> documents) {
        DocumentSplitter splitter = recursive(1500, 200);

        // Split all documents into segments first
        List<TextSegment> allSegments = splitter.splitAll(documents);
        LOG.info("Split %d documents into %d segments", documents.size(), allSegments.size());

        // Batch process segments to avoid token limit (each segment ~1500 tokens, limit is 300k tokens)
        // Use conservative batch of 100 segments (~150k tokens) to stay well under limit
        int segmentBatchSize = 100;
        
        for (int i = 0; i < allSegments.size(); i += segmentBatchSize) {
            int endIndex = Math.min(i + segmentBatchSize, allSegments.size());
            List<TextSegment> segmentBatch = allSegments.subList(i, endIndex);
            
            LOG.info("Embedding segments %d-%d of %d", i + 1, endIndex, allSegments.size());
            
            // Generate embeddings for this batch
            List<Embedding> embeddings = embeddingModel.embedAll(segmentBatch).content();
            
            // Store in PGVector
            embeddingStore.addAll(embeddings, segmentBatch);
        }
        
        LOG.info("Successfully ingested %d documents (%d segments)", documents.size(), allSegments.size());
    }

    /**
     * Manual ingestion with more control
     */
    public void manualIngest(List<Document> documents) {
        DocumentSplitter splitter = recursive(1500, 200);

        // Split documents into segments
        List<TextSegment> segments = splitter.splitAll(documents);

        LOG.info("Split %d documents into %d segments", documents.size(), segments.size());

        // Generate embeddings
        List<Embedding> embeddings = embeddingModel.embedAll(segments).content();

        // Store in PGVector
        embeddingStore.addAll(embeddings, segments);

        LOG.info("Manual ingestion completed");
    }
}
