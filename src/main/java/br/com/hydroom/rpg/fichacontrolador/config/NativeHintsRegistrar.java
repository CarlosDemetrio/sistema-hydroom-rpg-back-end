package br.com.hydroom.rpg.fichacontrolador.config;

import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;

public class NativeHintsRegistrar implements RuntimeHintsRegistrar {
    @Override
    public void registerHints(RuntimeHints hints, ClassLoader classLoader) {
        // Hibernate 7 strategy classes: AOT-generated reflect-config references
        // MetadataBuildingOptionsImpl$1 and $2 which no longer exist in Hibernate 7.x,
        // so the conditional registrations are never triggered. Register unconditionally.
        hints.reflection().registerType(
            org.hibernate.boot.model.relational.ColumnOrderingStrategyStandard.class,
            MemberCategory.INVOKE_DECLARED_CONSTRUCTORS
        );
        hints.reflection().registerType(
            org.hibernate.boot.model.relational.ColumnOrderingStrategyLegacy.class,
            MemberCategory.INVOKE_DECLARED_CONSTRUCTORS
        );
        hints.reflection().registerType(
            org.hibernate.boot.model.naming.ImplicitNamingStrategyJpaCompliantImpl.class,
            MemberCategory.INVOKE_DECLARED_CONSTRUCTORS
        );
        hints.reflection().registerType(
            org.hibernate.boot.model.naming.PhysicalNamingStrategyStandardImpl.class,
            MemberCategory.INVOKE_DECLARED_CONSTRUCTORS
        );

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
