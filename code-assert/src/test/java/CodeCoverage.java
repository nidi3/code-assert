import guru.nidi.codeassert.config.For;
import guru.nidi.codeassert.jacoco.CoverageCollector;
import guru.nidi.codeassert.jacoco.JacocoAnalyzer;
import org.junit.jupiter.api.Test;

import static guru.nidi.codeassert.jacoco.CoverageType.*;
import static guru.nidi.codeassert.junit.CodeAssertMatchers.hasEnoughCoverage;
import static org.hamcrest.MatcherAssert.assertThat;

public class CodeCoverage {
    @Test
    void coverage() {
        JacocoAnalyzer analyzer = new JacocoAnalyzer(new CoverageCollector(BRANCH, LINE, METHOD)
                .just(For.global().setMinima(80, 80, 80))
                .just(For.allPackages().setMinima(75, 80, 80))
                .just(For.thePackage("*junit").setMinima(70, 70, 60))
        );
        assertThat(analyzer.analyze(), hasEnoughCoverage());
    }
}
