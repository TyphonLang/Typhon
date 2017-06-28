package info.iconmaster.typhon.language;

import info.iconmaster.typhon.TyphonInput;
import info.iconmaster.typhon.types.AnyType;
import info.iconmaster.typhon.types.SystemType;
import info.iconmaster.typhon.types.Type;
import info.iconmaster.typhon.types.UserType;

public class CorePackage extends Package {
	public Type TYPE_ANY, TYPE_NUMBER, TYPE_INT;
	
	public CorePackage(TyphonInput tni) {
		super(tni);
		
		TYPE_ANY = new AnyType(tni);
		TYPE_NUMBER = new UserType("Number", TYPE_ANY);
		TYPE_INT = new SystemType("int", TYPE_NUMBER);
	}
}
