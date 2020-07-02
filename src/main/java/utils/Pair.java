package utils;

public class Pair<A, B> {
	public final A a;
	public final B b;

	public Pair(A a, B b) {
		this.a = a;
		this.b = b;
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Pair) {
			Pair<A, B> p = (Pair<A, B>) obj;
			return (this.a.equals(p.a) && this.b.equals(p.b));
		}
		return super.equals(obj);
	}
}
