package info.iconmaster.typhon.compiler;

import java.util.List;
import java.util.Map;

import info.iconmaster.typhon.model.MemberAccess;
import info.iconmaster.typhon.types.TemplateType;
import info.iconmaster.typhon.types.TypeRef;
import info.iconmaster.typhon.util.SourceInfo;
import info.iconmaster.typhon.util.TemplateUtils;

/**
 * A local variable.
 * 
 * @author iconmaster
 *
 */
public class Variable implements MemberAccess {
	/**
	 * The name. May be null.
	 */
	public String name;
	
	/**
	 * The scope where this variable is from.
	 */
	public Scope scope;
	
	/**
	 * The type.
	 */
	public TypeRef type;
	
	/**
	 * The location in the soruce where this variable was declared.
	 */
	public SourceInfo declaredAt;
	
	/**
	 * Construct a new variable.
	 * Do not use this directly; use <tt>Scope.addVar()</tt> instead!
	 * 
	 * @param scope
	 * @param name
	 * @param type
	 * @param declaredAt
	 */
	Variable(Scope scope, String name, TypeRef type, SourceInfo declaredAt) {
		this.name = name;
		this.scope = scope;
		this.type = type;
		this.declaredAt = declaredAt;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public MemberAccess getMemberParent() {
		return scope;
	}
	
	@Override
	public List<MemberAccess> getMembers(Map<TemplateType, TypeRef> templateMap) {
		return TemplateUtils.replaceTemplates(type, templateMap).getMembers(templateMap);
	}
	
	@Override
	public Map<TemplateType, TypeRef> getTemplateMap(Map<TemplateType, TypeRef> templateMap) {
		return type.getTemplateMap(templateMap);
	}
}
