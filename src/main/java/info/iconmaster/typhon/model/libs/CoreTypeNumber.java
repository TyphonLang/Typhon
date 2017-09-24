package info.iconmaster.typhon.model.libs;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import info.iconmaster.typhon.TyphonInput;
import info.iconmaster.typhon.model.Constructor;
import info.iconmaster.typhon.model.Constructor.ConstructorParameter;
import info.iconmaster.typhon.types.SystemType;
import info.iconmaster.typhon.types.Type;
import info.iconmaster.typhon.types.TypeRef;

public class CoreTypeNumber extends SystemType {
	public Map<Type, Constructor> FUNC_NEW = new HashMap<>();
	
	public CoreTypeNumber(TyphonInput input, String name, Type parent) {
		super(name, parent);
	}
	
	/**
	 * Called by CorePackage.
	 * This isn't in the constructor because it depends on all the numeric types being in the type tree.
	 */
	public void addMembers() {
		List<CoreTypeNumber> nums = new ArrayList<>(Arrays.asList(new CoreTypeNumber[] {tni.corePackage.TYPE_DOUBLE, tni.corePackage.TYPE_FLOAT, tni.corePackage.TYPE_ULONG, tni.corePackage.TYPE_UINT, tni.corePackage.TYPE_USHORT, tni.corePackage.TYPE_UBYTE, tni.corePackage.TYPE_LONG, tni.corePackage.TYPE_INT, tni.corePackage.TYPE_SHORT, tni.corePackage.TYPE_BYTE}));
		nums.remove(this);
		nums.add(0, this);
		
		for (CoreTypeNumber num : nums) {
			Constructor c = new Constructor(tni);
			c.getConstParams().add(ConstructorParameter.nonFieldParam("n", new TypeRef(num)));
			FUNC_NEW.put(num, c);
			getTypePackage().addFunction(c);
		}
	}
}
