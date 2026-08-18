package com.broadcom.springconsulting.springnotes.architecture;

import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

// Encodes the hexagonal architecture conventions documented in CLAUDE.md ("Architecture" /
// "Conventions" sections) as executable rules, so a violation is caught in this build rather
// than discovered later. A failure here means either a change violates those conventions, or
// the conventions themselves deliberately changed and CLAUDE.md needs updating to match -
// either way it should be a conscious decision, not drift.
@AnalyzeClasses(
        packages = "com.broadcom.springconsulting.springnotes",
        importOptions = ImportOption.DoNotIncludeTests.class
)
class ArchitectureTest {

    // Build-generated classes (Spring CGLIB proxies for @Transactional beans, Spring Data AOT
    // accessor/instantiator classes) land in the same output directory as hand-written source
    // and get swept into the same classpath scan. They're always public regardless of what the
    // real class looks like, so the visibility rules below need to ignore them - their naming
    // (a "$$" or "__" marker Java identifiers can't otherwise contain) is how ArchUnit itself
    // tells them apart from top-level classes that just happen to share a package.
    private static final DescribedPredicate<JavaClass> ARE_BUILD_GENERATED =
            DescribedPredicate.describe(
                    "are build-generated (CGLIB proxy or Spring Data AOT accessor)",
                    javaClass -> javaClass.getName().contains( "$$" ) || javaClass.getName().contains( "__" )
            );

    @ArchTest
    static final ArchRule domain_must_not_depend_on_adapters =
            noClasses().that().resideInAPackage( "..application.domain.." )
                    .should().dependOnClassesThat().resideInAPackage( "..adapter.." )
                    .because( "the domain is the center of the hexagon - it must have no idea adapters exist" );

    @ArchTest
    static final ArchRule ports_must_not_depend_on_adapters =
            noClasses().that().resideInAPackage( "..application.port.." )
                    .should().dependOnClassesThat().resideInAPackage( "..adapter.." )
                    .because( "ports are the boundary the domain defines - adapters depend on ports, never the reverse" );

    @ArchTest
    static final ArchRule input_adapters_must_not_depend_on_domain_services =
            noClasses().that().resideInAPackage( "..adapter.in.." )
                    .should().dependOnClassesThat().resideInAPackage( "..application.domain.service.." )
                    .because( "input adapters are thin - they must delegate through a *UseCase port, never call a service implementation directly (CLAUDE.md: \"contain no business logic\")" );

    @ArchTest
    static final ArchRule input_adapters_must_not_depend_on_output_adapters =
            noClasses().that().resideInAPackage( "..adapter.in.." )
                    .should().dependOnClassesThat().resideInAPackage( "..adapter.out.." )
                    .because( "adapters only ever talk to the domain through ports, never to another adapter directly" );

    @ArchTest
    static final ArchRule output_adapters_must_not_depend_on_input_adapters =
            noClasses().that().resideInAPackage( "..adapter.out.." )
                    .should().dependOnClassesThat().resideInAPackage( "..adapter.in.." )
                    .because( "adapters only ever talk to the domain through ports, never to another adapter directly" );

    @ArchTest
    static final ArchRule input_ports_are_named_use_case =
            classes().that().resideInAPackage( "..application.port.in.." ).and().areInterfaces()
                    .should().haveSimpleNameEndingWith( "UseCase" )
                    .because( "CLAUDE.md: input ports are named <Verb><Feature>UseCase" );

    @ArchTest
    static final ArchRule output_ports_are_named_port =
            classes().that().resideInAPackage( "..application.port.out.." ).and().areInterfaces()
                    .should().haveSimpleNameEndingWith( "Port" )
                    .because( "CLAUDE.md: output ports end with Port" );

    @ArchTest
    static final ArchRule domain_model_types_do_not_use_model_suffix =
            classes().that().resideInAPackage( "..application.domain.model.." )
                    .should().haveSimpleNameNotEndingWith( "Model" )
                    .because( "CLAUDE.md: domain model types are named after the real-world concept, no suffix (Note, not NoteModel)" );

    @ArchTest
    static final ArchRule domain_service_implementations_are_package_private =
            classes().that().resideInAPackage( "..application.domain.service.." )
                    .and().areTopLevelClasses()
                    .and( ARE_BUILD_GENERATED.negate() )
                    .should().notBePublic()
                    .because( "service implementations are only ever referenced through their *UseCase port - callers outside the feature package never name the class directly" );

    @ArchTest
    static final ArchRule adapters_are_package_private =
            classes().that().resideInAPackage( "..adapter.." )
                    .and().areTopLevelClasses()
                    .and( ARE_BUILD_GENERATED.negate() )
                    .should().notBePublic()
                    .because( "adapters are Spring-managed implementation details wired in via their port interface - nothing references the adapter class by name" );

    @ArchTest
    static final ArchRule notes_must_not_depend_on_chat =
            noClasses().that().resideInAPackage( "..notes.." )
                    .should().dependOnClassesThat().resideInAPackage( "..chat.." )
                    .because( "notes must stay unaware chat exists - chat reacts to notes' published events over RabbitMQ, not the reverse" );

    @ArchTest
    static final ArchRule chat_domain_must_not_depend_on_notes =
            noClasses().that().resideInAPackage( "..chat.application.." )
                    .should().dependOnClassesThat().resideInAPackage( "..notes.." )
                    .because( "chat's coupling to notes is confined to its adapter/configuration layers (consuming notes' published events and exchange name) - chat's domain and ports stay feature-agnostic" );

    @ArchTest
    static final ArchRule root_configuration_must_not_depend_on_a_specific_feature =
            noClasses().that().resideInAPackage( "com.broadcom.springconsulting.springnotes.configuration" )
                    .should().dependOnClassesThat().resideInAnyPackage( "..notes..", "..chat.." )
                    .because( "root configuration is cross-cutting only (security, observability, ...) - feature-scoped wiring belongs in each feature's own configuration package" );

}
