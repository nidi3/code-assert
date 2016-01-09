code-assert [![Build Status](https://travis-ci.org/nidi3/code-assert.svg?branch=master)](https://travis-ci.org/nidi3/code-assert)
===========

Assert that the java code of a project satisfies certain checks.

Nobody follows rules that are not checked. 
If they are only checked periodically / manually by an "architect", it's often too late because there are already too many violations.   
A better way is to define coding rules in JUnit tests. 
This way, they are asserted automatically and regularly. 
Violations of rules break the build and therefore, one is forced to either 
adjust the code to comply with the rules or to adapt the rules in a reasonable way.


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
        // Ignore dependencies from/to java.*, org.*, net.*
        config = AnalyzerConfig.mavenMainClasses().collecting(allPackages().excluding("java.*", "org.*", "net.*"));
    }

    @Test
    public void noCycles() {
        assertThat(new ModelAnalyzer(config).analyze(), hasNoCycles());
    }

    @Test
    public void dependency() {
        // Defines the dependency rules for package org.project
        class OrgProject implements DependencyRuler {
            // Rules for org.project, org.project.dependency (with sub packages), org.project.model, org.project.util
            DependencyRule $self, dependency_, model, util;

            @Override
            public void defineRules() {
                $self.mayDependUpon(util, dependency_);
                dependency_.mustDependUpon(model);
                model.mayDependUpon(util).mustNotDependUpon($self);
            }
        }

        // All dependencies are forbidden, except the ones defined in OrgProject
        DependencyRules rules = DependencyRules.denyAll().withRules(new OrgProject());

        assertThat(new ModelAnalyzer(config).analyze(), matchesExactly(rules));
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
        AnalyzerConfig config = AnalyzerConfig.mavenMainClasses();

        // Only treat bugs with rank < 17 and with NORMAL_PRIORITY or higher
        // Ignore the given bug types in the given classes / methods.
        BugCollector collector = new BugCollector().maxRank(17).minPriority(Priorities.NORMAL_PRIORITY)
                .just(In.everywhere().ignore("UWF_FIELD_NOT_INITIALIZED_IN_CONSTRUCTOR"))
                .because("It's checked and OK like this",
                        In.classes(DependencyRules.class, Ruleset.class).ignore("DP_DO_INSIDE_DO_PRIVILEGED"),
                        In.locs("ClassFileParser#parse", "*Test", "Rulesets$*").ignore("URF_UNREAD_FIELD"));

        FindBugsResult result = new FindBugsAnalyzer(config, collector).analyze();
        assertThat(result, hasNoBugs());
    }
}
```
[//]: # (end)


## PmdChecks

Runs [PMD](https://pmd.github.io/) on the code and finds questionable constructs and code duplications.

[//]: # (pmd)
```java
public class PmdTest {

    private AnalyzerConfig config;

    @Before
    public void setup() throws IOException {
        // Analyze all sources in src/main/java
        config = AnalyzerConfig.mavenMainClasses();
    }

    @Test
    public void pmd() {
        // Only treat violations with MEDIUM priority or higher
        // Ignore the given violations in the given classes / methods
        ViolationCollector collector = new ViolationCollector().minPriority(RulePriority.MEDIUM)
                .because("It's not severe and occurs very often",
                        In.everywhere().ignore("MethodArgumentCouldBeFinal"),
                        In.locs("JavaClassBuilder#build", "FindBugsMatchers$*").ignore("AvoidInstantiatingObjectsInLoops"))
                .because("it'a an enum",
                        In.loc("SignatureParser").ignore("SwitchStmtsShouldHaveDefault"))
                .just(In.loc("*Test").ignore("TooManyStaticImports"));

        // Define and configure the rule sets to be used
        PmdAnalyzer analyzer = new PmdAnalyzer(config, collector).withRuleSets(
                basic(), braces(), design(), empty(), optimizations(),
                codesize().excessiveMethodLength(40).tooManyMethods(30));

        assertThat(analyzer.analyze(), hasNoPmdViolations());
    }

    @Test
    public void cpd() {
        // Ignore duplications in the given classes
        MatchCollector collector = new MatchCollector().just(
                In.classes(DependencyMap.class, RuleResult.class).ignoreAll(),
                In.classes(JavaClass.class, JavaPackage.class).ignoreAll(),
                In.loc("SignatureParser").ignoreAll(),
                In.clazz(DependencyMap.class).ignoreAll());

        // Only treat duplications with at least 20 tokens
        CpdAnalyzer analyzer = new CpdAnalyzer(config, 20, collector);

        assertThat(analyzer.analyze(), hasNoDuplications());
    }
}
```
[//]: # (end)

