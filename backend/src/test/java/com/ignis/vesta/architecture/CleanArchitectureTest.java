package com.ignis.vesta.architecture;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.Architectures.layeredArchitecture;

/** Enforces the dependency rule from docs/architecture.md: dependencies point inward only. */
@AnalyzeClasses(packages = "com.ignis.vesta", importOptions = ImportOption.DoNotIncludeTests.class)
class CleanArchitectureTest {

    private static final String DOMAIN = "com.ignis.vesta.domain..";
    private static final String APPLICATION = "com.ignis.vesta.application..";
    private static final String INFRASTRUCTURE = "com.ignis.vesta.infrastructure..";
    private static final String ENTRYPOINT = "com.ignis.vesta.entrypoint..";

    @ArchTest
    static final ArchRule layers_respect_dependency_rule = layeredArchitecture()
            .consideringOnlyDependenciesInLayers()
            .withOptionalLayers(true)
            .layer("Domain").definedBy(DOMAIN)
            .layer("Application").definedBy(APPLICATION)
            .layer("Infrastructure").definedBy(INFRASTRUCTURE)
            .layer("Entrypoint").definedBy(ENTRYPOINT)
            .whereLayer("Entrypoint").mayNotBeAccessedByAnyLayer()
            .whereLayer("Infrastructure").mayNotBeAccessedByAnyLayer()
            .whereLayer("Application").mayOnlyBeAccessedByLayers("Infrastructure", "Entrypoint")
            .whereLayer("Domain").mayOnlyBeAccessedByLayers("Application", "Infrastructure", "Entrypoint");

    @ArchTest
    static final ArchRule domain_is_framework_free = noClasses()
            .that().resideInAPackage(DOMAIN)
            .should().dependOnClassesThat().resideInAnyPackage(
                    "org.springframework..", "io.r2dbc..", "com.anthropic..", "jakarta.persistence..",
                    "com.fasterxml..", "org.apache.commons.csv..")
            .allowEmptyShould(true);

    /** Reactor types (Mono/Flux) are allowed only in port signatures, so the model stays plain Java. */
    @ArchTest
    static final ArchRule reactor_only_in_domain_ports = noClasses()
            .that().resideInAPackage(DOMAIN)
            .and().resideOutsideOfPackage("com.ignis.vesta.domain.port..")
            .should().dependOnClassesThat().resideInAPackage("reactor..")
            .allowEmptyShould(true);

    @ArchTest
    static final ArchRule application_does_not_know_adapters = noClasses()
            .that().resideInAPackage(APPLICATION)
            .should().dependOnClassesThat().resideInAnyPackage(
                    "org.springframework.web..", "org.springframework.r2dbc..", "io.r2dbc..", "com.anthropic..")
            .allowEmptyShould(true);
}
