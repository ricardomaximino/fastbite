package es.brasatech.fastbite.controller;

import es.brasatech.fastbite.application.office.I18nConfig;
import es.brasatech.fastbite.application.office.TranslationService;
import es.brasatech.fastbite.config.TenantRoutingResolver;
import es.brasatech.fastbite.domain.I18nField;
import es.brasatech.fastbite.domain.TranslatableText;
import es.brasatech.fastbite.domain.TranslatableType;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Translation editor for catalog items (groups, products, customizations, tables, discounts):
 * a grid of the item's texts by language.
 */
@Controller
// Path-style tenant pages (/{tenantId}/backoffice) link here with their prefix
@RequestMapping({"/backoffice/translations", "/{tenantId}/backoffice/translations"})
@RequiredArgsConstructor
public class I18nController {

        private final TranslationService translationService;
        private final I18nConfig i18nConfig;

        @GetMapping("/{type}/{id}")
        public String showTranslations(@PathVariable String type, @PathVariable String id,
                        HttpServletRequest request, Model model) {
                TranslatableType translatable = typeOf(type);
                List<TranslatableText> texts = findTexts(translatable, id);
                String defaultLanguage = i18nConfig.getDefaultLanguage();

                model.addAttribute("typeLabel", translatable.label());
                model.addAttribute("title", texts.getFirst().value().get(defaultLanguage, defaultLanguage));
                model.addAttribute("texts", texts);
                model.addAttribute("formAction", backOfficeUrl(request) + "/translations/" + translatable.path() + "/" + id);
                model.addAttribute("backOfficeUrl", backOfficeUrl(request));
                model.addAttribute("defaultLanguage", defaultLanguage);
                model.addAttribute("availableLocales", i18nConfig.getSupportedLocales());
                return "fastfood/translations";
        }

        /** Form fields are named {textKey}_{locale}, e.g. "name_es" or "option_cust-sauce-opt-0_pt". */
        @PostMapping("/{type}/{id}")
        public String saveTranslations(@PathVariable String type, @PathVariable String id,
                        @RequestParam Map<String, String> formData, HttpServletRequest request,
                        RedirectAttributes redirectAttributes) {
                TranslatableType translatable = typeOf(type);
                Map<String, I18nField> updated = findTexts(translatable, id).stream()
                                .collect(Collectors.toMap(TranslatableText::key, text -> withFormValues(text, formData)));
                translationService.saveTexts(translatable, id, updated);

                redirectAttributes.addFlashAttribute("message", "Translations saved successfully!");
                return "redirect:" + backOfficeUrl(request);
        }

        private List<TranslatableText> findTexts(TranslatableType type, String id) {
                return translationService.findTexts(type, id)
                                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, type.label() + " not found: " + id));
        }

        private static TranslatableType typeOf(String path) {
                return TranslatableType.fromPath(path)
                                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        }

        /** The stored text with the languages posted in the form applied; an empty field removes that language. */
        private static I18nField withFormValues(TranslatableText text, Map<String, String> formData) {
                I18nField updated = new I18nField(text.value().getAll());
                String prefix = text.key() + "_";
                formData.forEach((name, value) -> {
                        if (name.startsWith(prefix) && name.indexOf('_', prefix.length()) < 0) {
                                updated.set(name.substring(prefix.length()), value);
                        }
                });
                return updated;
        }

        /** The current tenant's back office, on its host or under its path prefix. */
        private static String backOfficeUrl(HttpServletRequest request) {
                Object prefix = request.getAttribute(TenantRoutingResolver.TENANT_URL_PREFIX);
                return (prefix != null ? prefix : "") + "/backoffice";
        }
}
