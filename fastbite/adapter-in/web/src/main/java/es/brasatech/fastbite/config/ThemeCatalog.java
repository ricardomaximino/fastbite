package es.brasatech.fastbite.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Component;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;

/** Operator-installed packages only; tenants choose registered IDs, never CSS or paths. */
@Component
public class ThemeCatalog {
    public record Theme(String id, String name, String description) {
        public String stylesheet() { return "/images/themes/"+id+"/theme.css"; }
        public String menuPreview() { return "/images/themes/"+id+"/menu.svg"; }
        public String counterPreview() { return "/images/themes/"+id+"/counter.svg"; }
        public String kitchenPreview() { return "/images/themes/"+id+"/kitchen.svg"; }
    }
    private record Package(Theme theme, Map<String,Resource> assets) {}
    private final Map<String,Package> packages = new TreeMap<>();
    public ThemeCatalog(@Value("${fastbite.themes.directory:themes}") String directory) throws java.io.IOException {
        var resolver = new PathMatchingResourcePatternResolver();
        for (Resource manifest : resolver.getResources("classpath*:themes/*/theme.properties")) register(manifest);
        Path external = Path.of(directory).toAbsolutePath().normalize();
        if (Files.isDirectory(external)) {
            try (var folders=Files.list(external)) {
                for (Path folder:folders.filter(Files::isDirectory).sorted().toList()) {
                    Path manifest=folder.resolve("theme.properties");
                    if(Files.isRegularFile(manifest)) register(new FileSystemResource(manifest));
                }
            }
        }
        if (!packages.containsKey("fastfood")) throw new IllegalStateException("Default FastBite theme is missing");
    }
    private void register(Resource manifest) throws java.io.IOException {
        Properties p=new Properties();
        try(var reader=new InputStreamReader(manifest.getInputStream(),StandardCharsets.UTF_8)) { p.load(reader); }
        String id=p.getProperty("id","");
        String name=p.getProperty("name","");
        if(!id.matches("[a-z][a-z0-9-]{0,63}") || name.isBlank()) throw new IllegalArgumentException("Invalid theme manifest: "+manifest);
        Map<String,Resource> assets=new HashMap<>();
        for(String file:List.of("theme.css","menu.svg","counter.svg","kitchen.svg")) {
            Resource asset=manifest.createRelative(file);
            if(!asset.exists()) throw new IllegalArgumentException("Missing theme asset: "+id+"/"+file);
            assets.put(file,asset);
        }
        packages.put(id,new Package(new Theme(id,name,p.getProperty("description","")),Map.copyOf(assets)));
    }
    public List<Theme> all() { return packages.values().stream().map(Package::theme).toList(); }
    public Theme resolve(String id) { return packages.getOrDefault(id==null?"fastfood":id,packages.get("fastfood")).theme(); }
    public void require(String id) { if(id==null || !packages.containsKey(id)) throw new IllegalArgumentException("Choose an available theme."); }
    public Resource asset(String id,String file) {
        var pack=packages.get(id);
        return pack==null?null:pack.assets().get(file);
    }
}
