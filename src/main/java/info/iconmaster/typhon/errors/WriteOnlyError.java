package info.iconmaster.typhon.errors;

import info.iconmaster.typhon.model.Field;
import info.iconmaster.typhon.util.SourceInfo;

/**
 * This is an error for when ANTLR reports a parse error.
 * 
 * @author iconmaster
 *
 */
public class WriteOnlyError extends TyphonError {
	/**
	 * The message ANTLR returned.
	 */
	public Field field;
	
	public WriteOnlyError(SourceInfo source, Field field) {
		super(source);
		this.field = field;
	}
	
	@Override
	public String getMessage() {
		return "field "+field.getName()+" is write-only";
	}
}
