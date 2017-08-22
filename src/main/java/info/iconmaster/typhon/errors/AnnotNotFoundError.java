package info.iconmaster.typhon.errors;

import java.util.List;

import org.antlr.v4.runtime.Token;

import info.iconmaster.typhon.types.TyphonTypeResolver;
import info.iconmaster.typhon.util.SourceInfo;

/**
 * This is an error for when the {@link TyphonTypeResolver} cannot resolve an annotation to a definition.
 * 
 * @author iconmaster
 *
 */
public class AnnotNotFoundError extends TyphonError {
	public List<Token> failedLookup;
	
	public AnnotNotFoundError(List<Token> failedLookup) {
		super(new SourceInfo(failedLookup.get(0), failedLookup.get(failedLookup.size()-1)));
		this.failedLookup = failedLookup;
	}
	
	@Override
	public String getMessage() {
		return "could not locate annotation " + failedLookup.stream().map((rule)->rule.getText()).reduce("", (a,b)->a+"."+b).substring(1);
	}
}
