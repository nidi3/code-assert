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
                For.allPackages().setMinima(75, 80, 80),
                For.thePackage("*.detekt").setMinima(35, 80, 65),
                For.thePackage("*.findbugs").setMinima(70, 80, 80),
                For.thePackage("*.junit").setMinima(35, 55, 50)
        ));
        assertThat(analyzer.analyze(), hasEnoughCoverage());
    }
}
