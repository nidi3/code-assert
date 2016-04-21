package guru.nidi.codeassert.config;

/**
 *
 */
public class NamedLocation {
    final String name;
    final String className;
    final String method;
    final boolean strictNameMatch;

    public NamedLocation(String name, String className, String method, boolean strictNameMatch) {
        this.name = name;
        this.className = className;
        this.method = method;
        this.strictNameMatch = strictNameMatch;
    }
}
