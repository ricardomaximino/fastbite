package es.brasatech.fastbite.jpa.tenant;

import org.flywaydb.core.api.ResourceProvider;
import org.flywaydb.core.api.resource.LoadableResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/** Spring's resolver supports resources embedded in native images as well as JARs. */
final class MigrationResourceProvider implements ResourceProvider {
    private final List<LoadableResource> resources = new ArrayList<>();

    MigrationResourceProvider(String... locations) {
        var resolver = new PathMatchingResourcePatternResolver();
        for (String location : locations) {
            String path = location.substring("classpath:".length()) + "/";
            try {
                Resource[] found = resolver.getResources("classpath*:" + path + "*.sql");
                if (found.length == 0) {
                    throw new IllegalStateException("No migrations found at " + location);
                }
                for (Resource resource : found) {
                    resources.add(new SqlResource(resource, path + resource.getFilename()));
                }
            } catch (IOException e) {
                throw new UncheckedIOException("Cannot read migrations at " + location, e);
            }
        }
    }

    @Override
    public LoadableResource getResource(String name) {
        return resources.stream().filter(resource -> resource.getAbsolutePath().equals(name))
                .findFirst().orElse(null);
    }

    @Override
    public Collection<LoadableResource> getResources(String prefix, String[] suffixes) {
        return resources.stream().filter(resource -> resource.getFilename().startsWith(prefix)
                && Arrays.stream(suffixes).anyMatch(resource.getFilename()::endsWith)).toList();
    }

    private static final class SqlResource extends LoadableResource {
        private final Resource resource;
        private final String path;

        private SqlResource(Resource resource, String path) {
            this.resource = resource;
            this.path = path;
        }

        @Override public String getAbsolutePath() { return path; }
        @Override public String getAbsolutePathOnDisk() { return null; }
        @Override public String getFilename() { return resource.getFilename(); }
        @Override public String getRelativePath() { return resource.getFilename(); }
        @Override public Reader read() {
            try {
                return new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8);
            } catch (IOException e) {
                throw new UncheckedIOException("Cannot read migration " + path, e);
            }
        }
    }
}
