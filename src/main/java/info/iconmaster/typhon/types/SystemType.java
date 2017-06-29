package info.iconmaster.typhon.types;

public class SystemType extends Type {
	private Type parent;
	private String name;

	public SystemType(String name, Type parent) {
		super(parent.tni, parent.source);
		this.name = name;
		this.parent = parent;
	}
	
	@Override
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public Type getParent() {
		return parent;
	}
	
	public void setParent(Type parent) {
		this.parent = parent;
	}
}
