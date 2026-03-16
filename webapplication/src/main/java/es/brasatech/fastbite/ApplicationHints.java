package es.brasatech.fastbite;

import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;

public class ApplicationHints implements RuntimeHintsRegistrar {

    @Override
    public void registerHints(RuntimeHints hints, ClassLoader classLoader) {
        hints.resources().registerPattern("static/*");
        hints.resources().registerPattern("templates/*");
        hints.resources().registerResourceBundle("i18n/messages");
        hints.resources().registerResourceBundle("i18n/messages_es");
        hints.resources().registerResourceBundle("i18n/messages_pt");
    }
}
