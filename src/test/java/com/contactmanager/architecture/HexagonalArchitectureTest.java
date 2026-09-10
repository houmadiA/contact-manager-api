package com.contactmanager.architecture;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class HexagonalArchitectureTest {

    private static final String APPLICATION = "com.contactmanager.application..";
    private static final String DOMAIN = "com.contactmanager.domain..";
    private static final String INFRASTRUCTURE = "com.contactmanager.infrastructure..";

    private static JavaClasses classes;

    @BeforeAll
    static void importClasses() {
        classes = new ClassFileImporter()
                .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                .importPackages("com.contactmanager");
    }

    @Test
    void domainIsFrameworkFree() {
        noClasses()
                .that()
                .resideInAPackage(DOMAIN)
                .should()
                .dependOnClassesThat()
                .resideInAnyPackage(
                        "org.springframework..",
                        "jakarta.persistence..",
                        "jakarta.servlet..",
                        "jakarta.validation..",
                        "com.fasterxml.jackson..",
                        "tools.jackson..",
                        "org.apache.commons..",
                        "com.nimbusds..",
                        "io.swagger..")
                .because("the hexagon must be usable, and testable, without any of them")
                .check(classes);
    }

    @Test
    void domainDependsOnNeitherSideAroundIt() {
        noClasses()
                .that()
                .resideInAPackage(DOMAIN)
                .should()
                .dependOnClassesThat()
                .resideInAnyPackage(APPLICATION, INFRASTRUCTURE)
                .because("dependencies point inwards: the outer packages know the domain, never the reverse")
                .check(classes);
    }

    @Test
    void applicationAndInfrastructureMeetOnlyInTheDomain() {
        noClasses()
                .that()
                .resideInAPackage(APPLICATION)
                .should()
                .dependOnClassesThat()
                .resideInAPackage(INFRASTRUCTURE)
                .because("a controller talks to use cases, not to the database or the CSV parser")
                .check(classes);

        noClasses()
                .that()
                .resideInAPackage(INFRASTRUCTURE)
                .should()
                .dependOnClassesThat()
                .resideInAPackage(APPLICATION)
                .because("an adapter has no business knowing which DTO a controller returns")
                .check(classes);
    }
}
