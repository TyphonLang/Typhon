package info.iconmaster.typhon.compiler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import info.iconmaster.typhon.model.MemberAccess;
import info.iconmaster.typhon.types.TemplateType;
import info.iconmaster.typhon.types.TypeRef;
import info.iconmaster.typhon.util.SourceInfo;

/**
 * This is a local scope, containing local variables.
 * Used in the compiler to look up what local variables correspond to.
 * 
 * @author iconmaster
 *
 */
public class Scope implements MemberAccess {
	/**
	 * User variables in this scope.
	 */
	private Map<String, Variable> vars = new HashMap<>();
	
	/**
	 * Compiler-created variables in this scope.
	 */
	private List<Variable> tempVars = new ArrayList<>();
	
	/**
	 * The parent scope.
	 * May be null if this is the root scope.
	 */
	private Scope parent;
	
	/**
	 * The code block which this scope comes from.
	 */
	private CodeBlock codeBlock;
	
	/**
	 * The labels in this scope.
	 */
	private List<Label> labels = new ArrayList<>();
	
	/**
	 * Construct a root scope.
	 * 
	 * @param codeBlock
	 */
	public Scope(CodeBlock codeBlock) {
		this(codeBlock, null);
	}
	
	/**
	 * Construct a scope.
	 * 
	 * @param codeBlock
	 * @param parent
	 */
	public Scope(CodeBlock codeBlock, Scope parent) {
		this.codeBlock = codeBlock;
		this.parent = parent;
	}
	
	/**
	 * Adds a user variable to the scope.
	 * 
	 * @param name The name as entered by the user.
	 * @param type The variable's type.
	 * @param declaredAt Where the variable was declared.
	 * @return
	 */
	public Variable addVar(String name, TypeRef type, SourceInfo declaredAt) {
		Variable var = new Variable(this, name, type, declaredAt);
		vars.put(name, var);
		codeBlock.vars.add(var);
		return var;
	}
	
	/**
	 * Adds a compiler-created variable to the scope.
	 * 
	 * @param type The variable's type.
	 * @param declaredAt Where the variable was first in need from. May be null.
	 * @return
	 */
	public Variable addTempVar(TypeRef type, SourceInfo declaredAt) {
		Variable var = new Variable(this, null, type, declaredAt);
		codeBlock.vars.add(var);
		tempVars.add(var);
		return var;
	}
	
	/**
	 * Looks up a name and returns a local vairable if possible.
	 * This function also looks in parent scopes; it returns the variable with the given name in the closest scope.
	 * 
	 * @param name The name.
	 * @return The variable in this or any parent scope, or null if no variable with that name is found.
	 */
	public Variable getVar(String name) {
		if (vars.containsKey(name)) {
			return vars.get(name);
		} else if (parent != null) {
			return parent.getVar(name);
		} else {
			return null;
		}
	}
	
	/**
	 * @return The parent scope.
	 */
	public Scope getParent() {
		return parent;
	}
	
	/**
	 * @return The code block which this scope comes from.
	 */
	public CodeBlock getCodeBlock() {
		return codeBlock;
	}
	
	/**
	 * @return The variables in this scope (but NOT the variables in parent scopes).
	 */
	public List<Variable> getVars() {
		ArrayList<Variable> a = new ArrayList<>(vars.values());
		a.addAll(tempVars);
		return a;
	}
	
	/**
	 * Returns true if there is a variable with a given name in this scope. It does not check the parent scope. 
	 * 
	 * @param name
	 * @return
	 */
	public boolean inThisScope(String name) {
		return vars.containsKey(name);
	}

	@Override
	public String getName() {
		return null;
	}

	@Override
	public MemberAccess getMemberParent() {
		return parent == null ? codeBlock.lookup : parent;
	}
	
	@Override
	public List<MemberAccess> getMembers(Map<TemplateType, TypeRef> templateMap) {
		return (List) getVars();
	}
	
	public List<Label> getLabels() {
		return labels;
	}
	
	public Label getLabel(String name) {
		for (Label label : labels) {
			if (name.equals(label.name)) {
				return label;
			}
		}
		
		if (parent == null) {
			return null;
		}
		
		return parent.getLabel(name);
	}
	
	public Label addLabel(String name) {
		Label label = new Label(this, name);
		labels.add(label);
		getCodeBlock().labels.add(label);
		return label;
	}
	
	public Label addTempLabel() {
		return addLabel(null);
	}
}
