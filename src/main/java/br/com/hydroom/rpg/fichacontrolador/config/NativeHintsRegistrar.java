package br.com.hydroom.rpg.fichacontrolador.config;

import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;

public class NativeHintsRegistrar implements RuntimeHintsRegistrar {
    @Override
    public void registerHints(RuntimeHints hints, ClassLoader classLoader) {
        // exp4j: uses reflection to parse mathematical expressions
        hints.reflection().registerType(
            net.objecthunter.exp4j.Expression.class,
            MemberCategory.values()
        );
        hints.reflection().registerType(
            net.objecthunter.exp4j.ExpressionBuilder.class,
            MemberCategory.values()
        );
        // Add more classes here as native build testing reveals reflection issues
    }
}
