package info.iconmaster.typhon.errors;

import info.iconmaster.typhon.model.Annotation;
import info.iconmaster.typhon.util.SourceInfo;

/**
 * This is an error for when there is a general issue with how the uses has used annotations.
 * 
 * @author iconmaster
 *
 */
public class AnnotFormatError extends TyphonError {
	Annotation a;
	String s;
	
	public AnnotFormatError(SourceInfo source, Annotation a, String s) {
		super(source);
		this.a = a;
		this.s = s;
	}
	
	@Override
	public String getMessage() {
		return "Invalid use of "+a.getDefinition().prettyPrint()+": "+s;
	}
}
