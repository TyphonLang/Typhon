package info.iconmaster.typhon.model.libs;

import info.iconmaster.typhon.TyphonInput;
import info.iconmaster.typhon.model.Constructor;
import info.iconmaster.typhon.model.Constructor.ConstructorParameter;
import info.iconmaster.typhon.types.SystemType;
import info.iconmaster.typhon.types.Type;

public class CoreTypeNumber extends SystemType {
	public Constructor FUNC_NEW_FROM_NUM;
	public Constructor FUNC_NEW_FROM_STRING;
	
	public CoreTypeNumber(TyphonInput input, String name, Type parent) {
		super(name, parent);
	}
	
	/**
	 * Called by CorePackage.
	 * This is done because all the types need to be made before these can be made.
	 */
	public void addMembers() {
		getTypePackage().addFunction(FUNC_NEW_FROM_NUM = new Constructor(this, ConstructorParameter.nonFieldParam("n", tni.corePackage.TYPE_NUMBER)));
		getTypePackage().addFunction(FUNC_NEW_FROM_STRING = new Constructor(this, ConstructorParameter.nonFieldParam("s", tni.corePackage.TYPE_STRING)));
	}
}
