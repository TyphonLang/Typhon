package info.iconmaster.typhon.compiler;

/**
 * A label is used in LABEL and JUMPIF instructions, to implement conditional jumps.
 * 
 * @author iconmaster
 *
 */
public class Label {
	/**
	 * The scope this label is from.
	 */
	public Scope scope;
	
	/**
	 * The name of this label, if provided by the user. May be null.
	 */
	String name;
	
	Label(Scope scope, String name) {
		this.scope = scope;
		this.name = name;
	}
	
	@Override
	public String toString() {
		return "label("+(name == null? super.toString().split("@")[1] : name)+")";
	}
}
