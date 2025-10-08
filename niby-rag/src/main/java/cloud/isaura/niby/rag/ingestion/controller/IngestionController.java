package cloud.isaura.niby.rag.ingestion.controller;

import cloud.isaura.niby.rag.ingestion.service.DocumentIngestionService;


import dev.langchain4j.data.document.Document;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Path("/niby/rag/ingestion")
public class IngestionController
{
    private static final Logger log = LoggerFactory.getLogger(IngestionController.class);

    @Inject
    DocumentIngestionService documentIngestionService;

    @POST
    @Path("/directory")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response ingestDirectory(Map<String, String> request) {
        String directoryPath = request.get("directoryPath");

        if (directoryPath == null || directoryPath.isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", "directoryPath is required"))
                    .build();
        }

        try {
            documentIngestionService.ingestWithCustomParsers(directoryPath);

            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Documents ingested successfully from " + directoryPath);

            log.info("Successfully ingested documents from directory: {}", directoryPath);
            return Response.ok(response).build();

        } catch (IOException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", "Failed to ingest documents: " + e.getMessage());

            log.error("Failed to ingest documents from directory: {}", directoryPath, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(response).build();
        }
    }

    @POST
    @Path("/manual")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response manualIngest(List<Document> documents) {
        if (documents == null || documents.isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", "documents list cannot be empty"))
                    .build();
        }

        try {
            documentIngestionService.manualIngest(documents);

            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Documents ingested successfully");
            response.put("count", documents.size());

            log.info("Successfully ingested {} documents manually", documents.size());
            return Response.ok(response).build();

        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", "Failed to ingest documents: " + e.getMessage());

            log.error("Failed to manually ingest documents", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(response).build();
        }
    }
}
