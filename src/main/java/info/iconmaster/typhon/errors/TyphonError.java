package info.iconmaster.typhon.errors;

import info.iconmaster.typhon.util.SourceInfo;

public abstract class TyphonError {
	public SourceInfo source;
	
	public TyphonError(SourceInfo source) {
		this.source = source;
	}
	
	public abstract String getMessage();
	
	@Override
	public String toString() {
		return "error: "+(source == null?"<unknown>":source)+": "+getMessage();
	}
}
