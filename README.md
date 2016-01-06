code-assert [![Build Status](https://travis-ci.org/nidi3/code-assert.svg?branch=master)](https://travis-ci.org/nidi3/code-assert)
===========

Assert that the java code of a project satisfies certain checks.

Nobody follows rules that are not checked. 
If they are only checked periodically / manually by an "architect", it's often too late because there are already too many violations.   
A better way is to define coding rules in JUnit tests. 
This way, they are asserted automatically and regularly. 
Violations of rules break the build and therefore, one is forced to either 
adjust the code to comply with the rules or to adapt the rules in a reasonable way.

Built on the base of [JDepend](https://github.com/clarkware/jdepend).

## Dependency checks

```java
public class DependencyTest {

    private AnalyzerConfig config;

    @Before
    public void setup() throws IOException {
        // Analyze all sources in src/main/java
        // Ignore dependencies from/to java.*, org.*, net.*
        config = AnalyzerConfig.mavenMainClasses().collecting(all().excluding("java.*", "org.*", "net.*"));
    }

    @Test
    public void noCycles() {
        assertThat(new ModelAnalyzer(config), DependencyMatchers.hasNoCycles());
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
        
        assertThat(new ModelAnalyzer(config), DependencyMatchers.matchesExactly());
    }
}
```

## FindBugs checks

```java
public class FindBugsTest {
    
    private AnalyzerConfig config;

    @Before
    public void setup() throws IOException {
        // Analyze all sources in src/main/java
        config = AnalyzerConfig.mavenMainClasses();
    }

    @Test
    public void findBugs() {
        // Only treat bugs with rank < 17 and with NORMAL_PRIORITY or higher
        // Ignore the given bug types in the given classes / methods.
        BugCollector collector = BugCollector.simple(17, Priorities.NORMAL_PRIORITY)
                .ignore("UWF_FIELD_NOT_INITIALIZED_IN_CONSTRUCTOR")
                .ignore("DP_DO_INSIDE_DO_PRIVILEGED").in(DependencyRules.class, Ruleset.class)
                .ignore("URF_UNREAD_FIELD").in("ClassFileParser#parse", "*Test", "Rulesets$*");
                
        assertThat(new FindBugsAnalyzer(config, collector), FindBugsMatchers.findsNoBugs());
    }
}
```

## PmdChecks

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
        ViolationCollector collector = ViolationCollector.simple(RulePriority.MEDIUM)
            .ignore("MethodArgumentCouldBeFinal", "GodClass").generally()
            .ignore("AvoidInstantiatingObjectsInLoops").in("JavaClassBuilder#build", "FindBugsMatchers$*")
            .ignore("SwitchStmtsShouldHaveDefault").in(SignatureParser.class)
            .ignore("TooManyStaticImports").in("*Test")
            
        // Define and configure the rule sets to be used
        PmdAnalyzer analyzer = new PmdAnalyzer(config, collector).withRuleSets(
            basic(), braces(), design(), empty(), optimizations(),
            codesize().excessiveMethodLength(40).tooManyMethods(30));
        
        assertThat(analyzer, PmdMatchers.hasNoPmdViolations());
    }
    
    @Test
    public void cpd() {
        // Ignore duplications in the given classes
        MatchCollector collector = new MatchCollector()
            .ignore(DependencyMap.class, RuleResult.class)
            .ignore(JavaClass.class, JavaPackage.class)
            .ignore("SignatureParser")
            .ignore(DependencyMap.class);
            
        // Only treat duplications with at least 20 tokens
        CpdAnalyzer analyzer = new CpdAnalyzer(config, 20, collector);
            
        assertThat(analyzer, PmdMatchers.hasNoDuplications());
    }
}
```

