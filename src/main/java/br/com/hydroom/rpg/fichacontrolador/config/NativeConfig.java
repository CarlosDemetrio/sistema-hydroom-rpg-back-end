package br.com.hydroom.rpg.fichacontrolador.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportRuntimeHints;

@Configuration
@ImportRuntimeHints(NativeHintsRegistrar.class)
public class NativeConfig {
    // Activated by Spring AOT processing (native build only)
}
