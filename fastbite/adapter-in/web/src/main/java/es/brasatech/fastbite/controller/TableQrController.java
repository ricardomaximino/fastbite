package es.brasatech.fastbite.controller;

import es.brasatech.fastbite.application.table.TableService;
import es.brasatech.fastbite.application.table.TableSignatureUtil;
import es.brasatech.fastbite.config.TenantRoutingResolver;
import es.brasatech.fastbite.domain.table.Table;
import es.brasatech.fastbite.domain.tenant.TenantContext;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;

/** Printable QR codes for the tables: scanning one opens the menu bound to that table. */
@Controller
public class TableQrController {

    private final TableService tableService;
    private final TableSignatureUtil tableSignatureUtil;
    private final String protocol;

    public TableQrController(TableService tableService, TableSignatureUtil tableSignatureUtil,
            @Value("${fastbite.protocol:http}") String protocol) {
        this.tableService = tableService;
        this.tableSignatureUtil = tableSignatureUtil;
        this.protocol = protocol;
    }

    public record TableQr(String tableName, String url) {
    }

    /** One table's code, or the codes of all active tables. */
    @GetMapping({"/backoffice/tables/qr-codes", "/{tenantId}/backoffice/tables/qr-codes"})
    public String qrCodes(@RequestParam(required = false) String table, HttpServletRequest request, Model model) {
        String tenantId = TenantContext.getCurrentTenant();
        String host = request.getHeader("Host") != null ? request.getHeader("Host") : "localhost:8080";
        Object prefix = request.getAttribute(TenantRoutingResolver.TENANT_URL_PREFIX);
        String menuUrl = protocol + "://" + host + (prefix != null ? prefix : "") + "/menu";

        List<TableQr> codes = tableService.findAll().stream()
                .filter(t -> table == null ? t.active() : t.id().equals(table))
                .map(t -> new TableQr(t.name(), UriComponentsBuilder.fromUriString(menuUrl)
                        .queryParam("table", t.id())
                        .queryParam("token", tableSignatureUtil.generateSignature(tenantId, t.id()))
                        .encode().toUriString()))
                .toList();
        if (table != null && codes.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Table not found: " + table);
        }

        model.addAttribute("codes", codes);
        // Codes printed from a local or test address would send guests nowhere
        model.addAttribute("localAddress", host.startsWith("localhost") || host.startsWith("127.") || host.contains(".localhost"));
        return "fastfood/tableQrCodes";
    }
}
