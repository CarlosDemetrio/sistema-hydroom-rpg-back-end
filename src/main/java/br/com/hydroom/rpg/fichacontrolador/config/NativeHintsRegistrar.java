package br.com.hydroom.rpg.fichacontrolador.config;

import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;

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

        // Hibernate 7 moved PostgreSQL JDBC types to org.hibernate.dialect.type package.
        // AOT-generated config still references old org.hibernate.dialect.PostgreSQL* names,
        // causing ClassNotFoundException in PgJdbcHelper.createJdbcType() via Class.forName().
        registerPostgreSQLDialectTypes(hints);

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

        // Hibernate 7 annotation model: all *Annotation classes in this package are
        // instantiated via reflection using constructor(ModelsContext) at startup.
        // There are ~277 classes — scan dynamically to avoid listing them all manually.
        registerHibernateAnnotationModelClasses(hints, classLoader);

        // DialectOverridesAnnotationHelper.buildOverrideMap() calls
        // DialectOverride.class.getDeclaredClasses() to build annotation → override mapping.
        // Without this, any entity with @SQLRestriction/@SQLInsert etc. fails at startup.
        registerDialectOverrideClasses(hints);
    }

    private void registerHibernateAnnotationModelClasses(RuntimeHints hints, ClassLoader classLoader) {
        // Include abstract classes too, since isCandidateComponent normally skips them
        ClassPathScanningCandidateComponentProvider scanner =
                new ClassPathScanningCandidateComponentProvider(false) {
                    @Override
                    protected boolean isCandidateComponent(AnnotatedBeanDefinition beanDefinition) {
                        return true;
                    }
                };
        scanner.addIncludeFilter((metadataReader, factory) -> true);

        for (BeanDefinition bd : scanner.findCandidateComponents(
                "org.hibernate.boot.models.annotations.internal")) {
            try {
                Class<?> clazz = Class.forName(bd.getBeanClassName(), false, classLoader);
                hints.reflection().registerType(clazz,
                        MemberCategory.INVOKE_DECLARED_CONSTRUCTORS,
                        MemberCategory.DECLARED_FIELDS);
            } catch (ClassNotFoundException | NoClassDefFoundError ignored) {}
        }
    }

    private void registerDialectOverrideClasses(RuntimeHints hints) {
        MemberCategory[] all = MemberCategory.values();
        // DialectOverridesAnnotationHelper.buildOverrideMap() scans DialectOverride.class.getDeclaredClasses()
        // to build its annotation → override map. GraalVM removes unreachable classes, so the inner
        // annotation types (DialectOverride$SQLInsert, $SQLRestriction, etc.) disappear from the native
        // image if not registered, making getDeclaredClasses() return an incomplete array and
        // causing "Specified Annotation type does not have an override form" at startup.
        hints.reflection().registerType(org.hibernate.annotations.DialectOverride.class, all);
        for (Class<?> inner : org.hibernate.annotations.DialectOverride.class.getDeclaredClasses()) {
            hints.reflection().registerType(inner, all);
        }
        hints.reflection().registerType(org.hibernate.boot.models.DialectOverrideAnnotations.class, all);
    }

    private void registerPostgreSQLDialectTypes(RuntimeHints hints) {
        // PgJdbcHelper.createJdbcType() uses Class.forName() to load these types at runtime.
        // Hibernate 7 moved them from org.hibernate.dialect to org.hibernate.dialect.type,
        // but the AOT config still has the old (Hibernate 6) package names.
        MemberCategory[] all = MemberCategory.values();
        hints.reflection().registerType(org.hibernate.dialect.type.PostgreSQLInetJdbcType.class, all);
        hints.reflection().registerType(org.hibernate.dialect.type.PostgreSQLCastingInetJdbcType.class, all);
        hints.reflection().registerType(org.hibernate.dialect.type.PostgreSQLIntervalSecondJdbcType.class, all);
        hints.reflection().registerType(org.hibernate.dialect.type.PostgreSQLCastingIntervalSecondJdbcType.class, all);
        hints.reflection().registerType(org.hibernate.dialect.type.PostgreSQLStructPGObjectJdbcType.class, all);
        hints.reflection().registerType(org.hibernate.dialect.type.PostgreSQLStructCastingJdbcType.class, all);
        hints.reflection().registerType(org.hibernate.dialect.type.PostgreSQLJsonPGObjectJsonType.class, all);
        hints.reflection().registerType(org.hibernate.dialect.type.PostgreSQLJsonPGObjectJsonbType.class, all);
        hints.reflection().registerType(org.hibernate.dialect.type.PostgreSQLCastingJsonJdbcType.class, all);
        hints.reflection().registerType(org.hibernate.dialect.type.PostgreSQLArrayJdbcType.class, all);
        hints.reflection().registerType(org.hibernate.dialect.type.PostgreSQLArrayJdbcTypeConstructor.class, all);
        hints.reflection().registerType(org.hibernate.dialect.type.PostgreSQLCastingJsonArrayJdbcType.class, all);
        hints.reflection().registerType(org.hibernate.dialect.type.PostgreSQLCastingJsonArrayJdbcTypeConstructor.class, all);
        hints.reflection().registerType(org.hibernate.dialect.type.PostgreSQLJsonArrayPGObjectType.class, all);
        hints.reflection().registerType(org.hibernate.dialect.type.PostgreSQLJsonArrayPGObjectJsonJdbcTypeConstructor.class, all);
        hints.reflection().registerType(org.hibernate.dialect.type.PostgreSQLJsonArrayPGObjectJsonbJdbcTypeConstructor.class, all);
        hints.reflection().registerType(org.hibernate.dialect.type.PostgreSQLEnumJdbcType.class, all);
        hints.reflection().registerType(org.hibernate.dialect.type.PostgreSQLOrdinalEnumJdbcType.class, all);
        hints.reflection().registerType(org.hibernate.dialect.type.PostgreSQLUUIDJdbcType.class, all);
    }
}
