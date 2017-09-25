package info.iconmaster.typhon.model.libs;

import info.iconmaster.typhon.TyphonInput;
import info.iconmaster.typhon.model.Constructor;
import info.iconmaster.typhon.model.Constructor.ConstructorParameter;
import info.iconmaster.typhon.model.Field;
import info.iconmaster.typhon.types.TypeRef;
import info.iconmaster.typhon.types.UserType;

public class CoreTypeErrorBase extends UserType {
	public Constructor FUNC_NEW;
	public Constructor FUNC_NEW_S;
	public Constructor FUNC_NEW_C;
	public Constructor FUNC_NEW_S_C;
	
	public Field FIELD_MESSAGE;
	public Field FIELD_CAUSE;
	
	public CoreTypeErrorBase(TyphonInput input) {
		super("Error", input.corePackage.TYPE_ANY);
	}
	
	/**
	 * Called by CorePackage.
	 * This is done because all the types need to be made before these can be made.
	 */
	public void addMembers() {
		getTypePackage().addFunction(FUNC_NEW = new Constructor(this));
		
		getTypePackage().addField(FIELD_MESSAGE = new Field("message", new TypeRef(tni.corePackage.TYPE_STRING)));
		getTypePackage().addField(FIELD_CAUSE = new Field("cause", new TypeRef(this)));
		
		getTypePackage().addFunction(FUNC_NEW_S = new Constructor(this, ConstructorParameter.fieldParam(FIELD_MESSAGE)));
		getTypePackage().addFunction(FUNC_NEW_C = new Constructor(this, ConstructorParameter.fieldParam(FIELD_CAUSE)));
		getTypePackage().addFunction(FUNC_NEW_S_C = new Constructor(this, ConstructorParameter.fieldParam(FIELD_MESSAGE), ConstructorParameter.fieldParam(FIELD_CAUSE)));
	}
}
