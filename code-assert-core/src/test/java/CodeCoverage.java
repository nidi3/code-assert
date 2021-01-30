import guru.nidi.codeassert.config.For;
import guru.nidi.codeassert.jacoco.CoverageCollector;
import guru.nidi.codeassert.jacoco.JacocoAnalyzer;
import org.junit.jupiter.api.Test;

import static guru.nidi.codeassert.jacoco.CoverageType.*;
import static guru.nidi.codeassert.junit.CodeAssertCoreMatchers.hasEnoughCoverage;
import static org.hamcrest.MatcherAssert.assertThat;

public class CodeCoverage {
    @Test
    void coverage() {
        JacocoAnalyzer analyzer = new JacocoAnalyzer(new CoverageCollector(BRANCH, LINE, METHOD).just(
                For.allPackages().setMinima(75, 75, 75),
                For.thePackage("*.codeassert").setMinima(75, 75, 65),
                For.thePackage("*.config").setMinima(70, 75, 70),
                For.thePackage("*.io").setMinima(0, 0, 0),
                For.thePackage("*.junit").setMinima(40, 50, 55),
                For.thePackage("*.util").setMinima(50, 45, 55)
        ));
        assertThat(analyzer.analyze(), hasEnoughCoverage());
    }
}
