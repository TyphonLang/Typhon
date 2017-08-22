package info.iconmaster.typhon.errors;

import info.iconmaster.typhon.types.TemplateUtils;
import info.iconmaster.typhon.types.TypeRef;

/**
 * This is an error for when {@link TemplateUtils} finds that there are more template arguments than parameters.
 * 
 * @author iconmaster
 *
 */
public class TemplateNumberError extends TyphonError {
	public TypeRef typeToMap;
	
	public TemplateNumberError(TypeRef typeToMap) {
		super(typeToMap.source);
		this.typeToMap = typeToMap;
	}
	
	@Override
	public String getMessage() {
		return "incorrect template arguments to "+typeToMap.getName()+": too many template arguments";
	}
}
