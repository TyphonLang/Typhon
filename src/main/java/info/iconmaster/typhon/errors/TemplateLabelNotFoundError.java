package info.iconmaster.typhon.errors;

import info.iconmaster.typhon.model.TemplateArgument;
import info.iconmaster.typhon.types.TypeRef;
import info.iconmaster.typhon.util.TemplateUtils;

/**
 * This is an error for when {@link TemplateUtils} finds an argument with a label that does not refernece any parameter.
 * 
 * @author iconmaster
 *
 */
public class TemplateLabelNotFoundError extends TyphonError {
	public TemplateArgument arg;
	public TypeRef typeToMap;
	
	public TemplateLabelNotFoundError(TypeRef typeToMap, TemplateArgument arg) {
		super(typeToMap.source);
		this.typeToMap = typeToMap;
		this.arg = arg;
	}
	
	@Override
	public String getMessage() {
		return "incorrect template arguments to "+typeToMap.getName()+": template parameter with name "+arg.getLabel()+" not found";
	}
}
