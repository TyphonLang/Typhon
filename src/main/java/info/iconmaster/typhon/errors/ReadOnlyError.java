package info.iconmaster.typhon.errors;

import info.iconmaster.typhon.model.Field;
import info.iconmaster.typhon.util.SourceInfo;

/**
 * This is an error for when you attempt to write a field without a setter.
 * 
 * @author iconmaster
 *
 */
public class ReadOnlyError extends TyphonError {
	/**
	 * The field in question.
	 */
	public Field field;
	
	public ReadOnlyError(SourceInfo source, Field field) {
		super(source);
		this.field = field;
	}
	
	@Override
	public String getMessage() {
		return "field "+field.getName()+" is read-only";
	}
}
