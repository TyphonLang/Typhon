package info.iconmaster.typhon.types;

import info.iconmaster.typhon.TyphonInput;
import info.iconmaster.typhon.util.SourceInfo;

public class UserType extends Type {
	private Type parent;
	private String name;

	public UserType(String name, Type parent) {
		super(parent.tni, parent.source);
		this.name = name;
		this.parent = parent;
	}
	
	public UserType(TyphonInput input, Type parent, String name) {
		super(input, parent.source);
		this.parent = parent;
		this.name = name;
	}

	public UserType(TyphonInput input, SourceInfo source, Type parent, String name) {
		super(input, source);
		this.parent = parent;
		this.name = name;
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
