package info.iconmaster.typhon.errors;

import info.iconmaster.typhon.model.Field;
import info.iconmaster.typhon.util.SourceInfo;

/**
 * This is an error for when you attempt to read a field without a getter.
 * 
 * @author iconmaster
 *
 */
public class WriteOnlyError extends TyphonError {
	/**
	 * The field in question.
	 */
	public Field field;
	
	public WriteOnlyError(SourceInfo source, Field field) {
		super(source);
		this.field = field;
	}
	
	@Override
	public String getMessage() {
		return "field "+field.prettyPrint()+" is write-only";
	}
}
