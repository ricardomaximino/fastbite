package es.brasatech.fastbite.controller;

import es.brasatech.fastbite.application.table.TableService;
import es.brasatech.fastbite.domain.table.Table;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/backoffice/tables")
@RequiredArgsConstructor
public class BackOfficeTableController {

    private final TableService tableService;

    @GetMapping
    public String listTables(Model model) {
        model.addAttribute("tables", tableService.findAll());
        return "fastfood/backOffice :: tables-section"; // This might need a new fragment
    }

    @PostMapping
    @ResponseBody
    public Table createTable(@RequestBody Table table) {
        return tableService.create(table);
    }

    @PutMapping("/{id}")
    @ResponseBody
    public Table updateTable(@PathVariable String id, @RequestBody Table table) {
        return tableService.update(id, table).orElseThrow(() -> new RuntimeException("Table not found"));
    }

    @DeleteMapping("/{id}")
    @ResponseBody
    public void deleteTable(@PathVariable String id) {
        tableService.delete(id);
    }
}
