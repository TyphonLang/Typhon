package info.iconmaster.typhon.types;

import info.iconmaster.typhon.TyphonInput;
import info.iconmaster.typhon.model.Constructor;

/**
 * 'Any' is the type all other types derive from.
 * 
 * @author iconmaster
 *
 */
public class AnyType extends Type {
	public Constructor FUNC_NEW;
	
	public AnyType(TyphonInput input) {
		super(input);
		
		getTypePackage().addFunction(FUNC_NEW = new Constructor(input));
	}
	
	@Override
	public String getName() {
		return "Any";
	}
	
	@Override
	public String toString() {
		return "Any";
	}
}
