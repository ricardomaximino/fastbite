package es.brasatech.fastbite.application.kds;

import es.brasatech.fastbite.domain.kds.KdsConfig;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class KdsConfigService {

    private final Map<String, KdsConfig> tenantKdsConfigs = new ConcurrentHashMap<>();

    public KdsConfigService() {
        // Pre-configure kebab demo tenant with active KDS timer thresholds
        tenantKdsConfigs.put("kebab", new KdsConfig(5, 10));
    }

    public KdsConfig getKdsConfig(String tenantId) {
        if (tenantId == null) {
            return new KdsConfig(0, 0);
        }
        return tenantKdsConfigs.getOrDefault(tenantId.trim().toLowerCase(), new KdsConfig(0, 0));
    }

    public KdsConfig updateKdsConfig(String tenantId, int yellowMinutes, int redMinutes) {
        String key = (tenantId != null) ? tenantId.trim().toLowerCase() : "default";
        KdsConfig updated = new KdsConfig(Math.max(0, yellowMinutes), Math.max(0, redMinutes));
        tenantKdsConfigs.put(key, updated);
        return updated;
    }
}
