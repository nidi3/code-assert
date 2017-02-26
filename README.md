code-assert
===========
[![Build Status](https://travis-ci.org/nidi3/code-assert.svg?branch=master)](https://travis-ci.org/nidi3/code-assert)
[![codecov](https://codecov.io/gh/nidi3/code-assert/branch/master/graph/badge.svg)](https://codecov.io/gh/nidi3/code-assert)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)

Assert that the java code of a project satisfies certain rules.

Nobody follows rules that are not checked. 
If they are only checked periodically / manually by an "architect", it's often too late because there are already too many violations.   
A better way is to define coding rules in JUnit tests. 
This way, they are asserted automatically and regularly. 
Violations of rules break the build and therefore, one is forced to either 
adjust the code to comply with the rules or to adapt the rules in a reasonable way.

code-assert supports rules on the package structure and the test coverage.
It also integrates findbugs, PMD and CPD.

## Dependency checks

This is based on code from [JDepend](https://github.com/clarkware/jdepend).
It can be checked if the package structure contains cycles and/or follows the defined rules.

[//]: # (dependency)
```java
public class DependencyTest {

    private AnalyzerConfig config;

    @Before
    public void setup() throws IOException {
        // Analyze all sources in src/main/java
        config = AnalyzerConfig.maven().main();
    }

    @Test
    public void noCycles() {
        assertThat(new ModelAnalyzer(config).analyze(), hasNoPackageCycles());
    }

    @Test
    public void dependency() {
        // Defines the dependency rules for package org.project
        class OrgProj extends DependencyRuler {
            // Rules for org.proj, org.proj.dependency (with sub packages), org.proj.model, org.proj.util
            DependencyRule $self, dependency_, model, util;

            @Override
            public void defineRules() {
                $self.mayUse(util, dependency_);
                dependency_.mustUse(model);
                model.mayUse(util).mustNotUse($self);
            }
        }

        // All dependencies are forbidden, except the ones defined in OrgProject
        // java, org, net packages are ignored
        DependencyRules rules = DependencyRules.denyAll()
                .withRelativeRules(new OrgProj())
                .withExternals("java.*", "org.*", "net.*");

        assertThat(new ModelAnalyzer(config).analyze(), packagesMatchExactly(rules));
    }
}
```
[//]: # (end)

## FindBugs checks

Runs [FindBugs](http://findbugs.sourceforge.net/) on the code and finds questionable constructs.

[//]: # (findBugs)
```java
public class FindBugsTest {
    @Test
    public void findBugs() {
        // Analyze all sources in src/main/java
        AnalyzerConfig config = AnalyzerConfig.maven().main();

        // Only treat bugs with rank < 17 and with NORMAL_PRIORITY or higher
        // Ignore the given bug types in the given classes / methods.
        BugCollector collector = new BugCollector().maxRank(17).minPriority(Priorities.NORMAL_PRIORITY)
                .just(In.everywhere().ignore("UWF_FIELD_NOT_INITIALIZED_IN_CONSTRUCTOR"))
                .because("It's checked and OK like this",
                        In.classes(DependencyRules.class, Ruleset.class).ignore("DP_DO_INSIDE_DO_PRIVILEGED"),
                        In.locs("ClassFileParser#parse", "*Test", "Rulesets").ignore("URF_UNREAD_FIELD"));

        FindBugsResult result = new FindBugsAnalyzer(config, collector).analyze();
        assertThat(result, hasNoBugs());
    }
}
```
[//]: # (end)


## PMD checks

Runs [PMD](https://pmd.github.io/) on the code and finds questionable constructs and code duplications.

[//]: # (pmd)
```java
public class PmdTest {

    private AnalyzerConfig config;

    @Before
    public void setup() throws IOException {
        // Analyze all sources in src/main/java
        config = AnalyzerConfig.maven().main();
    }

    @Test
    public void pmd() {
        // Only treat violations with MEDIUM priority or higher
        // Ignore the given violations in the given classes / methods
        PmdViolationCollector collector = new PmdViolationCollector().minPriority(RulePriority.MEDIUM)
                .because("It's not severe and occurs very often",
                        In.everywhere().ignore("MethodArgumentCouldBeFinal"),
                        In.locs("JavaClassBuilder#build", "FindBugsMatchers").ignore("AvoidInstantiatingObjectsInLoops"))
                .because("it'a an enum",
                        In.loc("SignatureParser").ignore("SwitchStmtsShouldHaveDefault"))
                .just(In.loc("*Test").ignore("TooManyStaticImports"));

        // Define and configure the rule sets to be used
        PmdAnalyzer analyzer = new PmdAnalyzer(config, collector).withRulesets(
                basic(), braces(), design(), empty(), optimizations(),
                codesize().excessiveMethodLength(40).tooManyMethods(30));

        assertThat(analyzer.analyze(), hasNoPmdViolations());
    }

    @Test
    public void cpd() {
        // Ignore duplications in the given classes
        CpdMatchCollector collector = new CpdMatchCollector()
                .because("equals",
                        In.everywhere().ignore("public boolean equals(Object o) {"))
                .just(
                        In.classes(DependencyMap.class, RuleResult.class).ignoreAll(),
                        In.loc("SignatureParser").ignoreAll());

        // Only treat duplications with at least 20 tokens
        CpdAnalyzer analyzer = new CpdAnalyzer(config, 20, collector);

        assertThat(analyzer.analyze(), hasNoCodeDuplications());
    }
}
```
[//]: # (end)

## Configuration reuse

Collector configurations can be defined separately and thus reused.
Some configurations are defined in [PredefConfig](code-assert/src/main/java/guru/nidi/codeassert/junit/PredefConfig.java).

[//]: # (reuse)
```java
private CollectorTemplate<Ignore> pmdTestCollector = CollectorTemplate.forA(PmdViolationCollector.class)
        .because("It's a test", In.loc("*Test")
                .ignore("JUnitSpelling", "AvoidDuplicateLiterals", "SignatureDeclareThrowsException"));

@Test
public void pmd() {
    PmdViolationCollector collector = new PmdViolationCollector().minPriority(RulePriority.MEDIUM)
            .apply(pmdTestCollector)
            .because("It's not severe and occurs often", In.everywhere().ignore("MethodArgumentCouldBeFinal"));

    PmdAnalyzer analyzer = new PmdAnalyzer(config, collector).withRulesets(rules);
    assertThat(analyzer.analyze(), hasNoPmdViolations());
}
```
[//]: # (end)


## Standard tests

A test can inherit from `CodeAssertTest`. It should override one or more `analyzeXXX` methods.
If it does so, these standard checks will be executed:
* circular dependencies
* PMD
* PMD - unused actions
* CPD
* CPD - unused actions
* FindBugs
* FindBugs - unused actions

[//]: # (codeTest)
```java
public class CodeTest extends CodeAssertTest {

    private static final AnalyzerConfig config = AnalyzerConfig.maven().main();

    @Test
    public void dependency() {
        class MyProject extends DependencyRuler {
            DependencyRule packages;

            @Override
            public void defineRules() {
                //TODO
            }
        }
        final DependencyRules rules = denyAll().withExternals("java*").withRelativeRules(new MyProject());
        assertThat(modelResult(), packagesMatchExactly(rules));
    }

    @Override
    protected ModelResult analyzeModel() {
        return new ModelAnalyzer(config).analyze();
    }

    @Override
    protected FindBugsResult analyzeFindBugs() {
        final BugCollector bugCollector = new BugCollector().just(
                In.loc("*Exception").ignore("SE_BAD_FIELD"));
        return new FindBugsAnalyzer(config, bugCollector).analyze();
    }

    @Override
    protected PmdResult analyzePmd() {
        final PmdViolationCollector collector = new PmdViolationCollector().just(
                In.everywhere().ignore("MethodArgumentCouldBeFinal"));
        return new PmdAnalyzer(config, collector).withRulesets(basic(), braces()).analyze();
    }
}
```
[//]: # (end)

## Test coverage

To verify the test coverage of a project, [JaCoCo](http://eclemma.org/jacoco/trunk/index.html) can be used.
The following steps are needed:
* Add this to the `<build><plugins>` section of `pom.xml`:
```xml
<plugin>
    <groupId>guru.nidi</groupId>
    <artifactId>code-assert-maven-plugin</artifactId>
    <version>0.0.6</version>
    <executions>
        <execution>
            <goals>
                <goal>prepare</goal>
                <goal>assert</goal>
            </goals>
        </execution>
    </executions>
</plugin>
```            
* `prepare` sets up the surefire plugin to run the tests with the JaCoCo agent which collects coverage data.
* `assert` generates a coverage report and runs a coverage test
    (default is `src/test/java/CodeCoverage.java`, configurable through the `testClass` property).
* Write a code coverage test:

[//]: # (codeCoverage)
```java
public class CodeCoverage {
    @Test
    public void coverage() {
        // Coverage of branches must be at least 70%, lines 80% and methods 90%
        // This is checked globally and for all packages except for entities.
        JacocoAnalyzer analyzer = new JacocoAnalyzer(new CoverageCollector(BRANCH, LINE, METHOD)
                .just(For.global().setMinima(70, 80, 90))
                .just(For.allPackages().setMinima(70, 80, 90))
                .just(For.packge("org.proj.entity.*").setNoMinima()));
        assertThat(analyzer.analyze(), hasEnoughCoverage());
    }
}
```
[//]: # (end)

