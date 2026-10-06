package es.brasatech.fastbite.jpa.tenant;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import es.brasatech.fastbite.application.tenant.TenantBackupRestorePort.DemoTemplateDescriptor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

@Component
@Slf4j
public class DynamicDemoTemplateRegistry {

    private final ObjectMapper objectMapper;
    private final String templatesDir;
    private final PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();

    public DynamicDemoTemplateRegistry(
            ObjectMapper objectMapper,
            @Value("${fastbite.demo.templates-directory:demo-templates}") String templatesDir) {
        this.objectMapper = objectMapper;
        this.templatesDir = templatesDir;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class TemplateManifest {
        private String id;
        private String icon;
        private int version;
        private String name;
        private String description;
        private Map<String, LocalizedText> translations = new HashMap<>();

        @Data
        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class LocalizedText {
            private String name;
            private String description;
        }
    }

    private Path findTemplatesDir(String dirName) {
        Path path = Paths.get(dirName).toAbsolutePath().normalize();
        if (Files.exists(path) && Files.isDirectory(path)) {
            return path;
        }
        Path current = Paths.get("").toAbsolutePath();
        for (int i = 0; i < 4 && current != null; i++) {
            Path candidate = current.resolve(dirName).normalize();
            if (Files.exists(candidate) && Files.isDirectory(candidate)) {
                return candidate;
            }
            current = current.getParent();
        }
        return path;
    }

    public List<DemoTemplateDescriptor> discoverTemplates(Locale locale) {
        Map<String, DemoTemplateDescriptor> discovered = new LinkedHashMap<>();
        String lang = locale != null ? locale.getLanguage().toLowerCase() : "en";

        // 1. Scan external templates folder (drop-in zip directory)
        Path externalPath = findTemplatesDir(templatesDir);
        if (Files.exists(externalPath) && Files.isDirectory(externalPath)) {
            try (var stream = Files.newDirectoryStream(externalPath, "*.zip")) {
                for (Path zipFile : stream) {
                    try {
                        var desc = inspectZipFile(zipFile.toFile(), lang);
                        if (desc != null) {
                            discovered.put(desc.id(), desc);
                        }
                    } catch (Exception e) {
                        log.warn("Skipping invalid demo archive in directory {}: {}", zipFile, e.getMessage());
                    }
                }
            } catch (IOException e) {
                log.warn("Failed to scan templates directory {}", externalPath, e);
            }
        }

        // 2. Scan classpath archives (e.g. /cafe_demo.zip, /kebab_demo.zip, or classpath*:*.zip)
        List<String> builtInNames;
        try (var index=getClass().getResourceAsStream("/demo-templates.list")) {
            builtInNames=index==null?List.of():new java.io.BufferedReader(new java.io.InputStreamReader(index,java.nio.charset.StandardCharsets.UTF_8))
                    .lines().map(String::trim).filter(name->name.matches("[a-zA-Z0-9_-]+\\.zip")).toList();
        } catch(IOException e) { throw new java.io.UncheckedIOException(e); }
        for (String builtIn : builtInNames) {
            String fallbackId = builtIn.replace("_demo.zip", "").replace(".zip", "");
            if (discovered.containsKey(fallbackId)) {
                continue; // External folder override takes precedence
            }
            try (InputStream is = getClass().getResourceAsStream("/" + builtIn)) {
                if (is != null) {
                    var desc = inspectInputStream(is, fallbackId, lang);
                    if (desc != null) {
                        discovered.put(desc.id(), desc);
                    }
                }
            } catch (Exception e) {
                log.debug("Builtin template {} not loaded from classpath: {}", builtIn, e.getMessage());
            }
        }

        return new ArrayList<>(discovered.values());
    }

    public Optional<InputStream> openTemplateStream(String templateId) {
        if (templateId == null || !templateId.matches("[a-zA-Z0-9_-]+")) {
            return Optional.empty();
        }

        // 1. Check external directory first
        Path externalPath = findTemplatesDir(templatesDir);
        if (Files.exists(externalPath) && Files.isDirectory(externalPath)) {
            // Check direct match or templateId + "_demo.zip"
            List<String> candidateNames = List.of(
                    templateId + ".zip",
                    templateId + "_demo.zip",
                    templateId
            );
            for (String name : candidateNames) {
                Path candidate = externalPath.resolve(name).normalize();
                if (Files.exists(candidate) && Files.isRegularFile(candidate) && candidate.startsWith(externalPath)) {
                    try {
                        return Optional.of(new FileInputStream(candidate.toFile()));
                    } catch (IOException e) {
                        log.error("Failed to open template file {}", candidate, e);
                    }
                }
            }
        }

        // 2. Check classpath
        List<String> classpathCandidates = List.of(
                "/" + templateId + "_demo.zip",
                "/" + templateId + ".zip",
                "/" + templateId
        );
        for (String cp : classpathCandidates) {
            InputStream is = getClass().getResourceAsStream(cp);
            if (is != null) {
                return Optional.of(is);
            }
        }

        // Fallback: check project root if running locally
        List<String> rootCandidates = List.of(
                "d:/git/fastbite/" + templateId + "_demo.zip",
                "d:/git/fastbite/" + templateId + ".zip",
                templateId + "_demo.zip",
                templateId + ".zip"
        );
        for (String rootPath : rootCandidates) {
            Path p = Paths.get(rootPath);
            if (Files.exists(p) && Files.isRegularFile(p)) {
                try {
                    return Optional.of(new FileInputStream(p.toFile()));
                } catch (IOException ignored) {}
            }
        }

        return Optional.empty();
    }

    private DemoTemplateDescriptor inspectZipFile(File file, String lang) throws IOException {
        try (ZipFile zip = new ZipFile(file)) {
            TemplateManifest manifest = null;
            ZipEntry manifestEntry = zip.getEntry("template.json");
            if (manifestEntry != null) {
                try (InputStream is = zip.getInputStream(manifestEntry)) {
                    manifest = objectMapper.readValue(is, TemplateManifest.class);
                } catch (Exception e) {
                    log.warn("Failed to parse template.json in {}: {}", file.getName(), e.getMessage());
                }
            }

            ZipEntry dataEntry = zip.getEntry("data.json");
            if (dataEntry == null) {
                log.warn("Zip file {} missing required data.json. Skipping.", file.getName());
                return null;
            }

            int productsCount = 0;
            int categoriesCount = 0;
            List<String> supportedLanguages = new ArrayList<>(List.of("en"));

            try (InputStream is = zip.getInputStream(dataEntry)) {
                var node = objectMapper.readTree(is);
                if (node.has("products") && node.get("products").isArray()) {
                    productsCount = node.get("products").size();
                }
                if (node.has("groups") && node.get("groups").isArray()) {
                    categoriesCount = node.get("groups").size();
                }
                if (node.has("productTranslations") && node.get("productTranslations").isArray()) {
                    for (var pt : node.get("productTranslations")) {
                        if (pt.has("language")) {
                            String l = pt.get("language").asText().toLowerCase();
                            if (!supportedLanguages.contains(l)) supportedLanguages.add(l);
                        }
                    }
                }
            }

            String id = (manifest != null && manifest.getId() != null)
                    ? manifest.getId()
                    : file.getName().replace("_demo.zip", "").replace(".zip", "");

            return buildDescriptor(id, manifest, productsCount, categoriesCount, supportedLanguages, lang);
        }
    }

    private DemoTemplateDescriptor inspectInputStream(InputStream is, String fallbackId, String lang) throws IOException {
        // Read zip from stream in memory to extract template.json and data.json
        TemplateManifest manifest = null;
        int productsCount = 0;
        int categoriesCount = 0;
        List<String> supportedLanguages = new ArrayList<>(List.of("en"));

        try (ZipInputStream zis = new ZipInputStream(is)) {
            for (ZipEntry entry; (entry = zis.getNextEntry()) != null;) {
                String name = entry.getName().replace("\\", "/");
                if ("template.json".equalsIgnoreCase(name) || name.endsWith("/template.json")) {
                    byte[] bytes = zis.readAllBytes();
                    try {
                        manifest = objectMapper.readValue(bytes, TemplateManifest.class);
                    } catch (Exception ignored) {}
                } else if ("data.json".equalsIgnoreCase(name) || name.endsWith("/data.json")) {
                    byte[] bytes = zis.readAllBytes();
                    try {
                        var node = objectMapper.readTree(bytes);
                        if (node.has("products") && node.get("products").isArray()) {
                            productsCount = node.get("products").size();
                        }
                        if (node.has("groups") && node.get("groups").isArray()) {
                            categoriesCount = node.get("groups").size();
                        }
                        if (node.has("productTranslations") && node.get("productTranslations").isArray()) {
                            for (var pt : node.get("productTranslations")) {
                                if (pt.has("language")) {
                                    String l = pt.get("language").asText().toLowerCase();
                                    if (!supportedLanguages.contains(l)) supportedLanguages.add(l);
                                }
                            }
                        }
                    } catch (Exception ignored) {}
                }
            }
        }

        String id = (manifest != null && manifest.getId() != null) ? manifest.getId() : fallbackId;
        return buildDescriptor(id, manifest, productsCount, categoriesCount, supportedLanguages, lang);
    }

    private DemoTemplateDescriptor buildDescriptor(
            String id,
            TemplateManifest manifest,
            int productsCount,
            int categoriesCount,
            List<String> supportedLanguages,
            String lang) {

        String name = manifest != null && manifest.getName() != null ? manifest.getName() : id;
        String desc = manifest != null && manifest.getDescription() != null ? manifest.getDescription() : "Pre-configured sample menu and catalog.";
        String icon = manifest != null && manifest.getIcon() != null ? manifest.getIcon() : "🍽️";

        if (manifest != null && manifest.getTranslations() != null && manifest.getTranslations().containsKey(lang)) {
            var localized = manifest.getTranslations().get(lang);
            if (localized.getName() != null && !localized.getName().isBlank()) {
                name = localized.getName();
            }
            if (localized.getDescription() != null && !localized.getDescription().isBlank()) {
                desc = localized.getDescription();
            }
        }

        Collections.sort(supportedLanguages);

        return new DemoTemplateDescriptor(
                id,
                name,
                desc,
                icon,
                productsCount,
                categoriesCount,
                supportedLanguages
        );
    }
}
