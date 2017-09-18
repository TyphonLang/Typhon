package info.iconmaster.typhon.model.libs;

import info.iconmaster.typhon.TyphonInput;
import info.iconmaster.typhon.model.Function;
import info.iconmaster.typhon.model.Package;
import info.iconmaster.typhon.model.Parameter;
import info.iconmaster.typhon.types.TemplateType;
import info.iconmaster.typhon.types.Type;

/**
 * This library contains common mathematical functions.
 * 
 * @author iconmaster
 *
 */
public class CoreLibraryMath extends Package {
	public CoreLibraryMath(TyphonInput tni) {
		super(tni, "math");
		
		addFunction(new Function(tni, "sqrt", new TemplateType[] {
				
		}, new Parameter[] {
				new Parameter(tni, "x", tni.corePackage.TYPE_FLOAT, false),
		}, new Type[] {
				tni.corePackage.TYPE_FLOAT,
		}));
	}
}
