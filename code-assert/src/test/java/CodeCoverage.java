import guru.nidi.codeassert.config.For;
import guru.nidi.codeassert.jacoco.CoverageCollector;
import guru.nidi.codeassert.jacoco.JacocoAnalyzer;
import org.junit.Test;

import static guru.nidi.codeassert.jacoco.CoverageType.*;
import static guru.nidi.codeassert.junit.CodeAssertMatchers.hasEnoughCoverage;
import static org.junit.Assert.assertThat;

/**
 *
 */
public class CodeCoverage {
    @Test
    public void coverage() {
        JacocoAnalyzer analyzer = new JacocoAnalyzer(new CoverageCollector(BRANCH, LINE, METHOD)
                .just(For.global().setMinima(75, 75, 75))
                .just(For.allPackages().setMinima(75, 75, 75))
                .just(For.packge("*junit").setMinima(65, 75, 65))
                .just(For.packge("*util").setMinima(75, 75, 65))
        );
        assertThat(analyzer.analyze(), hasEnoughCoverage());
    }
}
