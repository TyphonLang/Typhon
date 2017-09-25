package info.iconmaster.typhon.compiler;

import info.iconmaster.typhon.types.TypeRef;

public class CatchInfo {
	public Variable var;
	public TypeRef toCatch;
	public Label label;
	
	public CatchInfo(Variable var, TypeRef toCatch, Label label) {
		this.var = var;
		this.toCatch = toCatch;
		this.label = label;
	}
}
