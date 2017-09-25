package info.iconmaster.typhon.model.libs;

import info.iconmaster.typhon.TyphonInput;
import info.iconmaster.typhon.model.Constructor;
import info.iconmaster.typhon.model.Constructor.ConstructorParameter;
import info.iconmaster.typhon.types.UserType;

public class CoreTypeError extends UserType {
	public Constructor FUNC_NEW;
	public Constructor FUNC_NEW_S;
	public Constructor FUNC_NEW_C;
	public Constructor FUNC_NEW_S_C;
	
	public CoreTypeError(TyphonInput input, String name) {
		super(name, input.corePackage.TYPE_ERROR);
	}
	
	/**
	 * Called by CorePackage.
	 * This is done because all the types need to be made before these can be made.
	 */
	public void addMembers() {
		getTypePackage().addFunction(FUNC_NEW = new Constructor(this));
		getTypePackage().addFunction(FUNC_NEW_S = new Constructor(this, ConstructorParameter.nonFieldParam("message", tni.corePackage.TYPE_STRING)));
		getTypePackage().addFunction(FUNC_NEW_C = new Constructor(this, ConstructorParameter.nonFieldParam("cause", tni.corePackage.TYPE_ERROR)));
		getTypePackage().addFunction(FUNC_NEW_S_C = new Constructor(this, ConstructorParameter.nonFieldParam("message", tni.corePackage.TYPE_STRING), ConstructorParameter.nonFieldParam("cause", tni.corePackage.TYPE_ERROR)));
	}
}
