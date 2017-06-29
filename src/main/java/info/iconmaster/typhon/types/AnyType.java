package info.iconmaster.typhon.types;

import info.iconmaster.typhon.TyphonInput;

public class AnyType extends Type {
	public AnyType(TyphonInput input) {
		super(input);
	}
	
	@Override
	public String getName() {
		return "Any";
	}
}
