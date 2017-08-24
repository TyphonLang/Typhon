package info.iconmaster.typhon.compiler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import info.iconmaster.typhon.tnil.CodeBlock;
import info.iconmaster.typhon.types.TypeRef;
import info.iconmaster.typhon.util.SourceInfo;

public class Scope {
	private Map<String, Variable> vars = new HashMap<>();
	private List<Variable> tempVars = new ArrayList<>();
	private Scope parent;
	private CodeBlock codeBlock;
	
	public Scope(CodeBlock codeBlock) {
		this(codeBlock, null);
	}
	
	public Scope(CodeBlock codeBlock, Scope parent) {
		this.codeBlock = codeBlock;
		this.parent = parent;
	}
	
	public Variable addVar(String name, TypeRef type, SourceInfo declaredAt) {
		Variable var = new Variable(this, name, type, declaredAt);
		vars.put(name, var);
		codeBlock.vars.add(var);
		return var;
	}
	
	public Variable addTempVar(TypeRef type, SourceInfo declaredAt) {
		Variable var = new Variable(this, null, type, declaredAt);
		codeBlock.vars.add(var);
		tempVars.add(var);
		return var;
	}
	
	public Variable getVar(String name) {
		if (vars.containsKey(name)) {
			return vars.get(name);
		} else if (parent != null) {
			return parent.getVar(name);
		} else {
			return null;
		}
	}
	
	public Scope getParent() {
		return parent;
	}
	
	public CodeBlock getCodeBlock() {
		return codeBlock;
	}
	
	public List<Variable> getVars() {
		ArrayList<Variable> a = new ArrayList<>(vars.values());
		a.addAll(tempVars);
		return a;
	}
}
