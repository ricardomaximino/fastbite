package es.brasatech.fastbite.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.*;
import static org.assertj.core.api.Assertions.*;

class ThemeCatalogTest {
    @TempDir Path directory;
    @Test void discoversAnOperatorInstalledThemeWithoutChangingApplicationCode() throws Exception {
        Path folder=Files.createDirectory(directory.resolve("coastal"));
        Files.writeString(folder.resolve("theme.properties"),"id=coastal\nname=Coastal\ndescription=Fresh sea colours\n");
        Files.writeString(folder.resolve("theme.css"),":root { --primary-color: #125678; }");
        for(String file:new String[]{"menu.svg","counter.svg","kitchen.svg"})
            Files.writeString(folder.resolve(file),"<svg xmlns=\"http://www.w3.org/2000/svg\"/>");
        var catalog=new ThemeCatalog(directory.toString());
        assertThat(catalog.all()).extracting(ThemeCatalog.Theme::id).contains("fastfood","cafe","modern","coastal");
        assertThat(catalog.resolve("coastal").name()).isEqualTo("Coastal");
        assertThat(catalog.asset("coastal","theme.css").getContentAsString(java.nio.charset.StandardCharsets.UTF_8)).contains("#125678");
        assertThat(catalog.asset("coastal","../theme.properties")).isNull();
        assertThat(catalog.resolve("removed-theme").id()).isEqualTo("fastfood");
        assertThatThrownBy(()->catalog.require("removed-theme")).isInstanceOf(IllegalArgumentException.class);
    }
    @Test void rejectsAnIncompletePackageAtStartup() throws Exception {
        Path folder=Files.createDirectory(directory.resolve("broken"));
        Files.writeString(folder.resolve("theme.properties"),"id=broken\nname=Broken\n");
        assertThatThrownBy(()->new ThemeCatalog(directory.toString())).hasMessageContaining("Missing theme asset");
    }
}
