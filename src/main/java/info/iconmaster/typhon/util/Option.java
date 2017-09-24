package info.iconmaster.typhon.util;

public class Option<A,B> {
	protected Object value;
	protected boolean isA;
	
	public static final boolean IS_A = true;
	public static final boolean IS_B = false;
	
	public Option(Object value, boolean isA) {
		this.isA = isA;
		this.value = value;
	}
	
	public A getA() {
		return (A) value;
	}
	
	public B getB() {
		return (B) value;
	}
	
	public boolean isA() {
		return isA;
	}
	
	public boolean isB() {
		return !isA;
	}
}
