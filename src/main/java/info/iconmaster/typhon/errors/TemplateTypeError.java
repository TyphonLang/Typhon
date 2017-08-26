package info.iconmaster.typhon.errors;

import info.iconmaster.typhon.types.TemplateType;
import info.iconmaster.typhon.types.TypeRef;
import info.iconmaster.typhon.util.TemplateUtils;

/**
 * This is an error for when {@link TemplateUtils} finds a mismatched template parameter/argument.
 * 
 * @author iconmaster
 *
 */
public class TemplateTypeError extends TyphonError {
	public TemplateType arg;
	public TypeRef typeToMap;
	public TypeRef badType;
	
	public TemplateTypeError(TypeRef typeToMap, TemplateType arg, TypeRef badType) {
		super(typeToMap.source);
		this.typeToMap = typeToMap;
		this.arg = arg;
		this.badType = badType;
	}
	
	@Override
	public String getMessage() {
		return "incorrect template arguments to "+typeToMap.getName()+": Template " + arg.getName() + " expects argument castable to " + arg.getBaseType().getName() + "; got type " + badType.getName();
	}
}
