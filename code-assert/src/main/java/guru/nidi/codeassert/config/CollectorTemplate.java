package guru.nidi.codeassert.config;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public final class CollectorTemplate<A extends Action> extends BaseCollector<Object, A, CollectorTemplate<A>> implements Iterable<CollectorConfig<A>> {
    private final List<CollectorConfig<A>> configs = new ArrayList<>();

    private CollectorTemplate() {
    }

    public static <A extends Action> CollectorTemplate<A> forA(Class<? extends BaseCollector<?, A, ?>> coll) {
        return new CollectorTemplate<>();
    }

    @Override
    public Iterator<CollectorConfig<A>> iterator() {
        return configs.iterator();
    }

    @Override
    protected CollectorTemplate<A> config(CollectorConfig<A>... configs) {
        for (final CollectorConfig<A> config : configs) {
            this.configs.add(config.ignoringUnused());
        }
        return this;
    }

    @Override
    public ActionResult accept(Object issue) {
        throw new UnsupportedOperationException("This is just a template. Apply it to a real Collector using .configs()");
    }

    @Override
    protected ActionResult doAccept(Object issue, A action) {
        throw new UnsupportedOperationException("This is just a template. Apply it to a real Collector using .configs()");
    }

    @Override
    protected List<A> unused(UsageCounter counter) {
        throw new UnsupportedOperationException("This is just a template. Apply it to a real Collector using .configs()");
    }

    @Override
    public String toString() {
        return configs.toString();
    }

}
