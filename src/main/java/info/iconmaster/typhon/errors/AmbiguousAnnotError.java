package info.iconmaster.typhon.errors;

import java.util.List;

import org.antlr.v4.runtime.Token;

import info.iconmaster.typhon.model.AnnotationDefinition;
import info.iconmaster.typhon.types.TyphonTypeResolver;
import info.iconmaster.typhon.util.SourceInfo;

/**
 * This is an error for when the {@link TyphonTypeResolver} resolves an annotation, but finds multiple candidates.
 * 
 * @author iconmaster
 *
 */
public class AmbiguousAnnotError extends TyphonError {
	public List<Token> failedLookup;
	public List<AnnotationDefinition> defs;
	
	public AmbiguousAnnotError(List<Token> failedLookup, List<AnnotationDefinition> defs) {
		super(new SourceInfo(failedLookup.get(0), failedLookup.get(failedLookup.size()-1)));
		this.failedLookup = failedLookup;
		this.defs = defs;
	}
	
	@Override
	public String getMessage() {
		return "Reference to annotation " + failedLookup.stream().map((rule)->rule.getText()).reduce("", (a,b)->a+"."+b).substring(1) + " is ambiguous; "+defs.size()+" candidates found";
	}
}
