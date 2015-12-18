package guru.nidi.codeassert;

/**
 *
 */
public class Bugs {
    public void bugs() {
        Object a = new Integer(5);
    }

    public void more() {
        Object a = new Integer(5);
    }

    public static class InnerBugs {
        public void bugs() {
            Object a = new Integer(5);
        }
    }
}
