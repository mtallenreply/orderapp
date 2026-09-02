package lernen.orderapp;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.Architectures.layeredArchitecture;
import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;

@AnalyzeClasses(packages = "lernen.orderapp", importOptions = ImportOption.DoNotIncludeTests.class)
final class ArchitectureTest {

    @ArchTest
    static final ArchRule schichtenGrenzenWerdenEingehalten = layeredArchitecture()
            .consideringOnlyDependenciesInLayers()
            .layer("Controller").definedBy("..controller..")
            .layer("Service").definedBy("..service..")
            .layer("Repository").definedBy("..repository..")
            .layer("Batch").definedBy("..batch..")
            .layer("Config").definedBy("..config..")
            .layer("Entity").definedBy("..entity..")
            .whereLayer("Controller").mayNotBeAccessedByAnyLayer()
            .whereLayer("Repository").mayOnlyBeAccessedByLayers("Service", "Batch", "Config");

    @ArchTest
    static final ArchRule entitiesSindFreiVonFachlogikAbhaengigkeiten = noClasses()
            .that().resideInAPackage("..entity..")
            .should().dependOnClassesThat()
            .resideInAnyPackage("..controller..", "..service..", "..repository..", "..batch..", "..config..");

    @ArchTest
    static final ArchRule repositoriesSindInterfacesMitPassendemNamen = classes()
            .that().resideInAPackage("..repository..")
            .and().areTopLevelClasses()
            .should().beInterfaces()
            .andShould().haveSimpleNameEndingWith("Repository");

    @ArchTest
    static final ArchRule controllerLiegenImControllerPaket = classes()
            .that().areAnnotatedWith("org.springframework.web.bind.annotation.RestController")
            .should().resideInAPackage("..controller..");

    @ArchTest
    static final ArchRule keineZyklenZwischenPaketen = slices()
            .matching("lernen.orderapp.(*)..")
            .should().beFreeOfCycles();
}
