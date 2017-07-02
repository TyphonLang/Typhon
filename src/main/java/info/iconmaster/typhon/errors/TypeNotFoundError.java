package info.iconmaster.typhon.errors;

import java.util.List;

import info.iconmaster.typhon.antlr.TyphonParser.TypeMemberItemContext;
import info.iconmaster.typhon.types.TyphonTypeResolver;
import info.iconmaster.typhon.util.SourceInfo;

/**
 * This is an error for when the {@link TyphonTypeResolver} cannot resolve a type.
 * 
 * @author iconmaster
 *
 */
public class TypeNotFoundError extends TyphonError {
	public List<TypeMemberItemContext> failedLookup;
	
	public TypeNotFoundError(List<TypeMemberItemContext> failedLookup) {
		super(new SourceInfo(failedLookup));
		this.failedLookup = failedLookup;
	}
	
	@Override
	public String getMessage() {
		return "could not locate type " + failedLookup.stream().map((rule)->rule.tnName.getText()).reduce("", (a,b)->a+"."+b).substring(1);
	}
}
