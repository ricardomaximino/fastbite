package es.brasatech.fastbite.menu;

import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class AotWarmupTest {

    @Test
    public void warmupJacksonJsonMapper() {
        // Explicitly call JsonMapper.builder() so that GraalVM agent registers it reflectively during test execution
        JsonMapper.Builder builder = JsonMapper.builder();
        assertNotNull(builder);
        
        JsonMapper mapper = builder.build();
        assertNotNull(mapper);
    }
}
