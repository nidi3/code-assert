package guru.nidi.codeassert.dependency;

import guru.nidi.codeassert.model.FileManager;
import guru.nidi.codeassert.model.JavaPackage;
import guru.nidi.codeassert.model.PackageFilter;
import guru.nidi.codeassert.model.Project;
import org.hamcrest.Matcher;
import org.hamcrest.StringDescription;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import static guru.nidi.codeassert.dependency.RuleMatchers.*;
import static org.junit.Assert.*;

/**
 *
 */
public class DependencyRulesTest {
    private static final String BASE = "guru.nidi.codeassert.dependency.";
    private static final Set<String> WILDCARD_UNDEFINED = set("guru.nidi.codeassert.model", "guru.nidi.codeassert.dependency", base("a"), base("b"), base("c"));
    private static final Set<String> UNDEFINED = set("guru.nidi.codeassert.model", "guru.nidi.codeassert.dependency", base("a.a"), base("a.b"), base("b.a"), base("b.b"), base("c.a"), base("c.b"));
    private Project project;
    private Collection<JavaPackage> packages;

    @Before
    public void analyze() throws IOException {
        project = new Project(
                new FileManager().withDirectory("target/test-classes/guru/nidi/codeassert/dependency"),
                PackageFilter.all().excluding("java.").excluding("org"));
        packages = project.read();
    }

    @Test(expected = IllegalArgumentException.class)
    public void wildcardNotAtEnd() {
        PackageRule.allowAll("a*b");
    }

    @Test(expected = IllegalArgumentException.class)
    public void wildcardWithoutPriorDot() {
        PackageRule.allowAll("a*");
    }

    @Test
    public void matcherFlags() {
        final DependencyRules rules = DependencyRules.allowAll();
        final PackageRule a = rules.addRule(base("a"));
        final PackageRule d = rules.addRule(base("d"));
        final Set<String> undefined = new HashSet<String>(UNDEFINED);
        undefined.addAll(set(base("b"), base("c")));

        final RuleResult result = rules.analyzeRules(packages);
        assertEquals(new RuleResult(
                        new DependencyMap(),
                        new DependencyMap(),
                        new DependencyMap(),
                        set(base("d")),
                        undefined),
                result);

        assertThat(project, matchesRules(rules));

        assertMatcher("\nDefined, but not existing packages:\n" +
                        "guru.nidi.codeassert.dependency.d\n" +
                        "\nFound packages which are not defined:\n" +
                        "guru.nidi.codeassert.dependency, guru.nidi.codeassert.dependency.a.a, guru.nidi.codeassert.dependency.a.b, guru.nidi.codeassert.dependency.b, guru.nidi.codeassert.dependency.b.a, guru.nidi.codeassert.dependency.b.b, guru.nidi.codeassert.dependency.c, guru.nidi.codeassert.dependency.c.a, guru.nidi.codeassert.dependency.c.b, guru.nidi.codeassert.model\n",
                matchesExactly(rules));

        assertMatcher("\nFound packages which are not defined:\n" +
                        "guru.nidi.codeassert.dependency, guru.nidi.codeassert.dependency.a.a, guru.nidi.codeassert.dependency.a.b, guru.nidi.codeassert.dependency.b, guru.nidi.codeassert.dependency.b.a, guru.nidi.codeassert.dependency.b.b, guru.nidi.codeassert.dependency.c, guru.nidi.codeassert.dependency.c.a, guru.nidi.codeassert.dependency.c.b, guru.nidi.codeassert.model\n",
                matchesIgnoringNonExisting(rules));

        assertMatcher("\nDefined, but not existing packages:\n" +
                        "guru.nidi.codeassert.dependency.d\n",
                matchesIgnoringUndefined(rules));
    }

    @Test
    public void allow() {
        final DependencyRules rules = DependencyRules.allowAll();
        final PackageRule a = rules.addRule(base("a"));
        final PackageRule b = rules.addRule(base("b"));
        final PackageRule c = rules.addRule(base("c"));

        a.mustDependUpon(b);
        b.mustNotDependUpon(a, c).mayDependUpon(a);

        class GuruNidiCodeassertDependency implements RuleDefiner {
            PackageRule a, b, c;

            public void defineRules() {
                a.mustDependUpon(b);
                b.mustNotDependUpon(a, c).mayDependUpon(a);
            }
        }
        final DependencyRules rules2 = DependencyRules.allowAll().withRules(new GuruNidiCodeassertDependency());

        final RuleResult result = rules.analyzeRules(packages);
        assertEquals(result, rules2.analyzeRules(packages));
        assertEquals(new RuleResult(
                        new DependencyMap(),
                        new DependencyMap().with(base("a"), set(), base("b")),
                        new DependencyMap().with(base("b"), set(base("b.B1")), base("c")),
                        set(),
                        UNDEFINED),
                result);
        assertMatcher("\n" +
                        "Found missing dependencies:\n" +
                        "guru.nidi.codeassert.dependency.a ->\n" +
                        "  guru.nidi.codeassert.dependency.b\n" +
                        "\n" +
                        "Found forbidden dependencies:\n" +
                        "guru.nidi.codeassert.dependency.b ->\n" +
                        "  guru.nidi.codeassert.dependency.c (by guru.nidi.codeassert.dependency.b.B1)\n",
                matchesRules(rules));
    }

    @Test
    public void deny() {
        final DependencyRules rules = DependencyRules.denyAll();
        final PackageRule a = rules.addRule(base("a"));
        final PackageRule b = rules.addRule(base("b"));
        final PackageRule c = rules.addRule(base("c"));

        a.mustDependUpon(b);
        b.mayDependUpon(a, c).mustNotDependUpon(a);

        final RuleResult result = rules.analyzeRules(packages);
        assertEquals(new RuleResult(
                        new DependencyMap(),
                        new DependencyMap().with(base("a"), set(), base("b")),
                        new DependencyMap()
                                .with(base("a"), set(base("a.A1")), base("c"))
                                .with(base("b"), set(base("b.B1")), base("a"))
                                .with(base("c"), set(base("c.C1")), base("a"))
                                .with(base("c"), set(base("c.C1"), base("c.C2")), base("b")),
                        set(),
                        UNDEFINED),
                result);
        assertMatcher("\n" +
                        "Found missing dependencies:\n" +
                        "guru.nidi.codeassert.dependency.a ->\n" +
                        "  guru.nidi.codeassert.dependency.b\n" +
                        "\n" +
                        "Found forbidden dependencies:\n" +
                        "guru.nidi.codeassert.dependency.a ->\n" +
                        "  guru.nidi.codeassert.dependency.c (by guru.nidi.codeassert.dependency.a.A1)\n" +
                        "guru.nidi.codeassert.dependency.b ->\n" +
                        "  guru.nidi.codeassert.dependency.a (by guru.nidi.codeassert.dependency.b.B1)\n" +
                        "guru.nidi.codeassert.dependency.c ->\n" +
                        "  guru.nidi.codeassert.dependency.a (by guru.nidi.codeassert.dependency.c.C1)\n" +
                        "  guru.nidi.codeassert.dependency.b (by guru.nidi.codeassert.dependency.c.C1, guru.nidi.codeassert.dependency.c.C2)\n",
                matchesRules(rules));
    }

    @Test
    public void allowWithWildcard() {
        final DependencyRules rules = DependencyRules.allowAll();
        final PackageRule a1 = rules.addRule(base("a.a"));
        final PackageRule a = rules.addRule(base("a.*"));
        final PackageRule b = rules.addRule(base("b.*"));
        final PackageRule c = rules.addRule(base("c.*"));

        a.mustDependUpon(b);
        b.mustNotDependUpon(a, c).mayDependUpon(a1);

        final RuleResult result = rules.analyzeRules(packages);
        final DependencyRules rules2 = DependencyRules.allowAll().withRules("guru.nidi.codeassert.dependency", new RuleDefiner() {
            PackageRule aA, a_, b_, c_;

            @Override
            public void defineRules() {
                a_.mustDependUpon(b_);
                b_.mustNotDependUpon(a_, c_).mayDependUpon(aA);
            }
        });
        assertEquals(result, rules2.analyzeRules(packages));
        assertEquals(new RuleResult(
                        new DependencyMap(),
                        new DependencyMap()
                                .with(base("a.a"), set(), base("b.b"))
                                .with(base("a.b"), set(), base("b.a"))
                                .with(base("a.b"), set(), base("b.b")),
                        new DependencyMap()
                                .with(base("b.a"), set(base("b.a.Ba1")), base("a.b"))
                                .with(base("b.a"), set(base("b.a.Ba2")), base("c.b"))
                                .with(base("b.a"), set(base("b.a.Ba2")), base("c.a"))
                                .with(base("b.b"), set(base("b.b.Bb1")), base("c.a"))
                                .with(base("b.b"), set(base("b.b.Bb1")), base("c.b")),
                        set(),
                        WILDCARD_UNDEFINED),
                result);
        assertMatcher("\n" +
                        "Found missing dependencies:\n" +
                        "guru.nidi.codeassert.dependency.a.a ->\n" +
                        "  guru.nidi.codeassert.dependency.b.b\n" +
                        "guru.nidi.codeassert.dependency.a.b ->\n" +
                        "  guru.nidi.codeassert.dependency.b.a\n" +
                        "  guru.nidi.codeassert.dependency.b.b\n" +
                        "\n" +
                        "Found forbidden dependencies:\n" +
                        "guru.nidi.codeassert.dependency.b.a ->\n" +
                        "  guru.nidi.codeassert.dependency.a.b (by guru.nidi.codeassert.dependency.b.a.Ba1)\n" +
                        "  guru.nidi.codeassert.dependency.c.a (by guru.nidi.codeassert.dependency.b.a.Ba2)\n" +
                        "  guru.nidi.codeassert.dependency.c.b (by guru.nidi.codeassert.dependency.b.a.Ba2)\n" +
                        "guru.nidi.codeassert.dependency.b.b ->\n" +
                        "  guru.nidi.codeassert.dependency.c.a (by guru.nidi.codeassert.dependency.b.b.Bb1)\n" +
                        "  guru.nidi.codeassert.dependency.c.b (by guru.nidi.codeassert.dependency.b.b.Bb1)\n",
                matchesRules(rules));
    }

    @Test
    public void denyWithWildcard() {
        final DependencyRules rules = DependencyRules.denyAll();
        final PackageRule a1 = rules.addRule(base("a.a"));
        final PackageRule a = rules.addRule(base("a.*"));
        final PackageRule b = rules.addRule(base("b.*"));
        final PackageRule c = rules.addRule(base("c.*"));

        a.mustDependUpon(b);
        b.mayDependUpon(a, c).mustNotDependUpon(a1);

        final RuleResult result = rules.analyzeRules(packages);
        assertEquals(new RuleResult(
                        new DependencyMap(),
                        new DependencyMap()
                                .with(base("a.a"), set(), base("b.b"))
                                .with(base("a.b"), set(), base("b.a"))
                                .with(base("a.b"), set(), base("b.b")),
                        new DependencyMap()
                                .with(base("b.a"), set(base("b.a.Ba1")), base("a.a"))
                                .with(base("c.a"), set(base("c.a.Ca1")), base("a.a"))
                                .with(base("c.a"), set(base("c.a.Ca1")), base("b.a")),
                        set(),
                        WILDCARD_UNDEFINED),
                result);
        assertMatcher("\n" +
                        "Found missing dependencies:\n" +
                        "guru.nidi.codeassert.dependency.a.a ->\n" +
                        "  guru.nidi.codeassert.dependency.b.b\n" +
                        "guru.nidi.codeassert.dependency.a.b ->\n" +
                        "  guru.nidi.codeassert.dependency.b.a\n" +
                        "  guru.nidi.codeassert.dependency.b.b\n" +
                        "\n" +
                        "Found forbidden dependencies:\n" +
                        "guru.nidi.codeassert.dependency.b.a ->\n" +
                        "  guru.nidi.codeassert.dependency.a.a (by guru.nidi.codeassert.dependency.b.a.Ba1)\n" +
                        "guru.nidi.codeassert.dependency.c.a ->\n" +
                        "  guru.nidi.codeassert.dependency.a.a (by guru.nidi.codeassert.dependency.c.a.Ca1)\n" +
                        "  guru.nidi.codeassert.dependency.b.a (by guru.nidi.codeassert.dependency.c.a.Ca1)\n",
                matchesRules(rules));
    }

    private static String base(String s) {
        return BASE + s;
    }

    private static Set<String> set(String... ss) {
        final Set<String> res = new HashSet<String>();
        for (String s : ss) {
            res.add(s);
        }
        return res;
    }

    private void assertMatcher(String message, Matcher<Project> matcher) {
        assertFalse(matcher.matches(project));
        final StringDescription sd = new StringDescription();
        matcher.describeMismatch(project, sd);
        assertEquals(message, sd.toString());
    }
}
