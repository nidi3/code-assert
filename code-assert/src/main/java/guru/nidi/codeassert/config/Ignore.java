package guru.nidi.codeassert.config;

import java.util.List;

/**
 *
 */
public class Ignore implements Action<NamedLocation> {
    private final LocationNameMatcher matcher;

    Ignore(List<String> locs, List<String> names) {
        matcher = new LocationNameMatcher(locs, names);
    }

    @Override
    public boolean accept(NamedLocation namedLocation) {
        return matcher.matches(namedLocation.name, namedLocation.className, namedLocation.method, namedLocation.strictNameMatch);
    }

    @Override
    public String toString() {
        return "    ignore " + matcher.toString();
    }
}
