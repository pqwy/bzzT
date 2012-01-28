package oposum;

public class X {

    public X () {}

    public static void main (String[] args) throws Exception {
        System.out.println ( new X ().apply () );
    }

    public String rondom () throws Exception {
    	return apply ();
    }

    public String apply () throws Exception {
        Z z = new Z (Y.class);
        Y y = (Y) z.meth ();
        return y.message ();
    }

    class Z {
        private Class<?> k = null;
        public Z (Class<?> klazz) {
            k = klazz;
        }
        public Object meth () throws Exception {
            return k.newInstance ();
        }
    }
}

class Y {
    public Y () {}
    private static String m = "hello, java";
    public String message () {
        return m;
    }
}

