package guru.nidi.codeassert.model;

import guru.nidi.codeassert.config.AnalyzerConfig;
import org.junit.jupiter.api.Test;

import static org.junit.Assert.*;

public class ModelTest {
    @Test
    void simple() {
        final AnalyzerConfig config = AnalyzerConfig.maven().mainAndTest();
        final Model model = new ModelBuilder().files(config.getClasses()).files(config.getSources()).build();
        assertEquals(82, model.getPackages().size());
        assertEquals(441, model.getClasses().size());
        assertEquals(169, model.getSources().size());
        for (final CodeClass clazz : model.getClasses()) {
            System.out.println(clazz);
            if (clazz.getPackage().getName().startsWith("guru.nidi")) {
                assertTrue(clazz.analyzed);
                assertNotNull(clazz.getSource());
            } else {
                assertFalse(clazz.analyzed);
                assertNull(clazz.getSource());
            }
        }
    }

}
