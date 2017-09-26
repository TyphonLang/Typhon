package info.iconmaster.typhon.errors;

import info.iconmaster.typhon.model.TemplateArgument;
import info.iconmaster.typhon.model.TyphonModelEntity;
import info.iconmaster.typhon.util.TemplateUtils;

/**
 * This is an error for when {@link TemplateUtils} finds an argument with a label that does not refernece any parameter.
 * 
 * @author iconmaster
 *
 */
public class TemplateLabelNotFoundError extends TyphonError {
	public TemplateArgument arg;
	public TyphonModelEntity toMap;
	
	public TemplateLabelNotFoundError(TyphonModelEntity toMap, TemplateArgument arg) {
		super(toMap.source);
		this.toMap = toMap;
		this.arg = arg;
	}
	
	@Override
	public String getMessage() {
		return "incorrect template arguments to "+toMap.prettyPrint()+": template parameter with name "+arg.getLabel()+" not found";
	}
}
