package lernen.orderapp;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;

@AnalyzeClasses(packages = "lernen.orderapp", importOptions = ImportOption.OnlyIncludeTests.class)
final class TestsuiteArchTest {

    @ArchTest
    static final ArchRule testNamingConvention = classes()
            .that().haveSimpleNameContaining("Test")
            .should().haveSimpleNameEndingWith("IntTest")
            .orShould().haveSimpleNameEndingWith("EndToEndTest")
            .orShould().haveSimpleNameEndingWith("UnitTest")
            .orShould().haveSimpleNameEndingWith("ArchTest")
            .allowEmptyShould(true).as("Testklassen sollen nach einem Namenschema benutzen")
            .because("Damit es eine Folgeverarbeitung geben könnte");
}
