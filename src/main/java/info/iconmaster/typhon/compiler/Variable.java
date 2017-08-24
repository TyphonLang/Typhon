package info.iconmaster.typhon.compiler;

import info.iconmaster.typhon.types.TypeRef;
import info.iconmaster.typhon.util.SourceInfo;

public class Variable {
	public String name;
	public Scope scope;
	public TypeRef type;
	public SourceInfo declaredAt;
	public int slot;
	
	Variable(Scope scope, String name, TypeRef type, SourceInfo declaredAt) {
		this.name = name;
		this.scope = scope;
		this.type = type;
		this.declaredAt = declaredAt;
		
		slot = scope.getCodeBlock().slotsUsed;
		scope.getCodeBlock().slotsUsed++;
	}
}
