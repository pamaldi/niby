package cloud.isaura.niby.rag.ingestion.service;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;

public class HtmlContentExtractor {

    public static void main(String[] args) {
        Path sourceDir = Paths.get("c:\\svi\\niby\\niby-rag\\nifi-doc\\components-1753475787835\\html");
        // Create a sibling "txt" directory for the output
        Path destDir = sourceDir.getParent().resolve("txt");

        System.out.println("Starting HTML to Text conversion...");
        System.out.println("Source: " + sourceDir);
        System.out.println("Destination: " + destDir);

        try {
            convertHtmlDirToText(sourceDir, destDir);
            System.out.println("Conversion completed successfully.");
        } catch (IOException e) {
            System.err.println("Error during conversion: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void convertHtmlDirToText(Path sourceDir, Path destDir) throws IOException {
        if (!Files.exists(sourceDir)) {
            throw new IllegalArgumentException("Source directory does not exist: " + sourceDir);
        }

        Files.walkFileTree(sourceDir, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                Path relativePath = sourceDir.relativize(dir);
                Path targetDir = destDir.resolve(relativePath);
                if (!Files.exists(targetDir)) {
                    Files.createDirectories(targetDir);
                }
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                if (file.toString().toLowerCase().endsWith(".html") || file.toString().toLowerCase().endsWith(".htm")) {
                    extractAndSave(file, sourceDir, destDir);
                }
                return FileVisitResult.CONTINUE;
            }
        });
    }

    private static void extractAndSave(Path file, Path sourceDir, Path destDir) {
        try {
            Document doc = Jsoup.parse(file.toFile(), "UTF-8");

            // Remove script, style, and other non-content elements
            doc.select("script, style, nav, header, footer, aside").remove();

            String title = doc.title();

            // Use wholeText() to preserve line breaks and format better
            doc.select("br").append("\\n");
            doc.select("p").append("\\n\\n");
            doc.select("h1, h2, h3, h4, h5, h6").append("\\n\\n");
            doc.select("li").prepend("• ");
            doc.select("tr").append("\\n");
            doc.select("th, td").append(" | ");

            String text = doc.body().text()
                .replaceAll("\\\\n", "\n")
                .replaceAll(" +", " ")
                .replaceAll("\n{3,}", "\n\n")
                .trim();

            // Simple formatting: Title on first line, then text
            StringBuilder content = new StringBuilder();
            if (title != null && !title.isEmpty()) {
                content.append(title).append(System.lineSeparator()).append(System.lineSeparator());
            }
            content.append(text);

            Path relativePath = sourceDir.relativize(file);
            // Replace extension with .txt
            String fileName = relativePath.getFileName().toString();
            String nameWithoutExt = fileName.substring(0, fileName.lastIndexOf('.'));
            Path targetPath = destDir.resolve(relativePath.resolveSibling(nameWithoutExt + ".txt"));

            Files.writeString(targetPath, content.toString(), StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING);
            System.out.println("Processed: " + file);

        } catch (IOException e) {
            System.err.println("Failed to process file: " + file + " - " + e.getMessage());
        }
    }
}
