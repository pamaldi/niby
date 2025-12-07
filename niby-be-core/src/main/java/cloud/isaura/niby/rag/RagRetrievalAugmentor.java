package cloud.isaura.niby.rag;

import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.rag.DefaultRetrievalAugmentor;
import dev.langchain4j.rag.RetrievalAugmentor;
import dev.langchain4j.rag.content.Content;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.rag.query.Query;
import dev.langchain4j.store.embedding.EmbeddingStore;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.function.Supplier;

@ApplicationScoped
public class RagRetrievalAugmentor implements Supplier<RetrievalAugmentor>
{
    private static final Logger LOG = LoggerFactory.getLogger(RagRetrievalAugmentor.class);

    @ConfigProperty(name = "rag.retrieval.max-results", defaultValue = "5")
    int maxResults;

    @Inject
    EmbeddingModel embeddingModel;
    @Inject
    EmbeddingStore embeddingStore;

    @Override
    public RetrievalAugmentor get() {
        EmbeddingStoreContentRetriever baseRetriever =
                EmbeddingStoreContentRetriever.builder()
                        .embeddingModel(embeddingModel)
                        .embeddingStore(embeddingStore)
                        .maxResults(maxResults)
                        .build();

        // Wrap retriever to add logging
        ContentRetriever loggingRetriever = new ContentRetriever() {
            @Override
            public List<Content> retrieve(Query query) {
                LOG.info("RAG Query: {}", query.text());
                List<Content> contents = baseRetriever.retrieve(query);
                LOG.info("RAG retrieved {} documents", contents.size());
                for (int i = 0; i < contents.size(); i++) {
                    Content content = contents.get(i);
                    String textPreview = content.textSegment().text().substring(0, Math.min(200, content.textSegment().text().length()));
                    LOG.info("RAG Document {}: {}... (score: {})",
                            i + 1,
                            textPreview,
                            content);
                }
                return contents;
            }
        };

        return DefaultRetrievalAugmentor.builder()
                .contentRetriever(loggingRetriever)
                .build();
    }
}