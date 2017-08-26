package info.iconmaster.typhon.errors;

import java.util.List;

import info.iconmaster.typhon.antlr.TyphonParser.TypeMemberItemContext;
import info.iconmaster.typhon.types.TypeRef;
import info.iconmaster.typhon.types.TyphonTypeResolver;
import info.iconmaster.typhon.util.SourceInfo;

/**
 * This is an error for when the {@link TyphonTypeResolver} resolves a type, but finds multiple candidates.
 * 
 * @author iconmaster
 *
 */
public class AmbiguousTypeError extends TyphonError {
	public List<TypeMemberItemContext> failedLookup;
	public List<TypeRef> types;
	
	public AmbiguousTypeError(List<TypeMemberItemContext> failedLookup, List<TypeRef> types) {
		super(new SourceInfo(failedLookup));
		this.failedLookup = failedLookup;
		this.types = types;
	}
	
	@Override
	public String getMessage() {
		return "Reference to type " + failedLookup.stream().map((rule)->rule.tnName.getText()).reduce("", (a,b)->a+"."+b).substring(1) + " is ambiguous; "+types.size()+" candidates found";
	}
}
