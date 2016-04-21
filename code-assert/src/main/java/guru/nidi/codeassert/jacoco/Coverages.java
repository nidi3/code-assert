package guru.nidi.codeassert.jacoco;

import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 *
 */
public class Coverages {
    final Set<Coverage> coverages = new TreeSet<>();
    final Map<String, Coverage> perPackage = new TreeMap<>();
    Coverage global;

    public void add(Coverage coverage) {
        coverages.add(coverage);
        final Coverage pc = perPackage.get(coverage.pack);
        perPackage.put(coverage.pack, pc == null ? coverage.withClazz("") : pc.combined(coverage));
        global = global == null ? coverage : global.combined(coverage);
    }
}
