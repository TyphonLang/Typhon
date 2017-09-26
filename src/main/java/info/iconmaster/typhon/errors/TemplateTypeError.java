package info.iconmaster.typhon.errors;

import info.iconmaster.typhon.model.TyphonModelEntity;
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
	public TyphonModelEntity toMap;
	public TypeRef badType;
	
	public TemplateTypeError(TyphonModelEntity toMap, TemplateType arg, TypeRef badType) {
		super(toMap.source);
		this.toMap = toMap;
		this.arg = arg;
		this.badType = badType;
	}
	
	@Override
	public String getMessage() {
		return "incorrect template arguments to "+toMap.prettyPrint()+": Template " + arg.prettyPrint() + " expects argument castable to " + arg.getBaseType().prettyPrint() + "; got type " + badType.prettyPrint();
	}
}
