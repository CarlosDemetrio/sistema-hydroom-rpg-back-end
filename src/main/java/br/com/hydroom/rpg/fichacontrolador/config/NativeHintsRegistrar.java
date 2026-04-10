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

        // Cloudinary: loads HTTP strategies via Class.forName() at runtime
        hints.reflection().registerType(
            com.cloudinary.http5.UploaderStrategy.class,
            MemberCategory.values()
        );
        hints.reflection().registerType(
            com.cloudinary.http5.ApiStrategy.class,
            MemberCategory.values()
        );
        hints.reflection().registerType(
            com.cloudinary.Cloudinary.class,
            MemberCategory.values()
        );
        hints.reflection().registerType(
            com.cloudinary.Configuration.class,
            MemberCategory.values()
        );
    }
}
