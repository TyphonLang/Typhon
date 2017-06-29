package info.iconmaster.typhon.errors;

import info.iconmaster.typhon.util.SourceInfo;

/**
 * The base class for errors, warnings, etc. that can occur during compilation.
 * @author iconmaster
 *
 */
public abstract class TyphonError {
	/**
	 * Where this error occured.
	 */
	public SourceInfo source;
	
	public TyphonError(SourceInfo source) {
		this.source = source;
	}
	
	/**
	 * Returns a human-readable error message.
	 * Note that data such as the source is prepended automatically for you.
	 * 
	 * @return A human-readable error message.
	 */
	public abstract String getMessage();
	
	/**
	 * Returns a human-readable error message, prepended with source location data.
	 * 
	 * @return A human-readable error message.
	 */
	@Override
	public String toString() {
		return "error: "+(source == null?"<unknown>":source)+": "+getMessage();
	}
}
