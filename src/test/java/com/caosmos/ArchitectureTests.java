package com.caosmos;

import static com.tngtech.archunit.library.Architectures.layeredArchitecture;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.library.Architectures;
import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;

public class ArchitectureTests {

  ApplicationModules modules = ApplicationModules.of(CaosmosApplication.class);

  @Test
  void verifyBoundaryViolations() {
    modules.verify();
  }

  @Test
  void verifyLayeredArchitecture() {
    JavaClasses importedClasses = new ClassFileImporter().importPackages("com.caosmos");

    Architectures.LayeredArchitecture architecture = layeredArchitecture()
        .consideringAllDependencies()
        .layer("Domain").definedBy("..domain..")
        .layer("Application").definedBy("..application..")
        .layer("Infrastructure").definedBy("..infrastructure..")

        .whereLayer("Domain").mayOnlyBeAccessedByLayers("Application", "Infrastructure")
        .whereLayer("Application").mayOnlyBeAccessedByLayers("Infrastructure")
        .whereLayer("Infrastructure").mayNotBeAccessedByAnyLayer();

    architecture.check(importedClasses);
  }
}
