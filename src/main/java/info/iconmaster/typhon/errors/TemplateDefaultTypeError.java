package info.iconmaster.typhon.errors;

import info.iconmaster.typhon.types.TemplateType;
import info.iconmaster.typhon.types.TyphonTypeResolver;

/**
 * This is an error for when the {@link TyphonTypeResolver} finds that the base and default types of a template type mismatch.
 * 
 * @author iconmaster
 *
 */
public class TemplateDefaultTypeError extends TyphonError {
	public TemplateType arg;
	
	public TemplateDefaultTypeError(TemplateType arg) {
		super(arg.source);
		this.arg = arg;
	}
	
	@Override
	public String getMessage() {
		return "template " + arg.prettyPrint() + " cannot cast default type " + arg.getDefaultValue().prettyPrint() + " to base type " + arg.getBaseType().prettyPrint();
	}
}
