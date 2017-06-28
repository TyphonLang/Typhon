package info.iconmaster.typhon.types;

public class SystemType extends Type {
	Type parent;
	String name;

	public SystemType(String name, Type parent) {
		super(parent.tni);
		this.name = name;
		this.parent = parent;
	}
}
