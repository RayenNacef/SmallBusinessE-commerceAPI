package com.smallshop;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import java.util.List;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

@AnalyzeClasses(packages = "com.smallshop", importOptions = ImportOption.DoNotIncludeTests.class)
class ArchitectureTest {

    private static final List<String> MODULES = List.of("catalog", "inventory", "cart", "ordering", "user");

    @ArchTest
    static final ArchRule domainDoesNotDependOnFrameworks = noClasses()
            .that().resideInAPackage("..domain..")
            .should().dependOnClassesThat().resideInAnyPackage(
                    "org.springframework..", "jakarta..", "org.hibernate..", "com.fasterxml..");

    @ArchTest
    static final ArchRule domainDoesNotDependOnOuterLayers = noClasses()
            .that().resideInAPackage("..domain..")
            .should().dependOnClassesThat().resideInAnyPackage("..application..", "..infrastructure..", "..api..");

    @ArchTest
    static final ArchRule applicationDoesNotDependOnAdapters = noClasses()
            .that().resideInAPackage("..application..")
            .should().dependOnClassesThat().resideInAnyPackage("..infrastructure..", "..api..");

    @ArchTest
    static final ArchRule sharedKernelDoesNotDependOnModules = noClasses()
            .that().resideInAPackage("com.smallshop.shared..")
            .should().dependOnClassesThat().resideInAnyPackage(
                    "com.smallshop.catalog..", "com.smallshop.inventory..", "com.smallshop.cart..",
                    "com.smallshop.ordering..", "com.smallshop.user..");

    @ArchTest
    static void modulesTalkToEachOtherOnlyThroughApplicationServices(JavaClasses classes) {
        for (String module : MODULES) {
            String root = "com.smallshop." + module;
            noClasses()
                    .that().resideOutsideOfPackage(root + "..")
                    .should().dependOnClassesThat().resideInAnyPackage(
                            root + ".domain..", root + ".infrastructure..", root + ".api..")
                    .because("other modules must go through the " + module + " application layer")
                    .allowEmptyShould(true)
                    .check(classes);
        }
    }
}
