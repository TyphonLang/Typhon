package info.iconmaster.typhon.types;

import info.iconmaster.typhon.TyphonInput;
import info.iconmaster.typhon.util.SourceInfo;

public class UserType extends Type {
	Type parent;
	String name;

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
}
