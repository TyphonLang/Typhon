package info.iconmaster.typhon.types;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import info.iconmaster.typhon.TyphonInput;
import info.iconmaster.typhon.model.MemberAccess;
import info.iconmaster.typhon.util.TemplateUtils;

/**
 * These types are created by the system.
 * They usually have special means of compilation, and cannot be created by the user.
 * These types do not have templates, and cannot be the parent type to any other type.
 * 
 * @author iconmaster
 *
 */
public class SystemType extends Type {
	/**
	 * The parent type. Cannot be null.
	 */
	private List<TypeRef> parentTypes = new ArrayList<>();
	
	/**
	 * The name of this type.
	 */
	private String name;

	public SystemType(TyphonInput input, String name) {
		super(input);
		this.name = name;
	}

	/**
	 * @param name The name of this type.
	 * @param parentType One or more parent types.
	 */
	public SystemType(String name, Type parentType, Type... otherParentTypes) {
		super(parentType.tni);
		this.parentTypes.add(new TypeRef(parentType));
		this.parentTypes.addAll(Arrays.asList(otherParentTypes).stream().map((t)->new TypeRef(t)).collect(Collectors.toList()));
		this.name = name;
	}
	
	/**
	 * @param name The name of this type.
	 * @param parentType One or more parent types.
	 */
	public SystemType(String name, TypeRef parentType, TypeRef... otherParentTypes) {
		super(parentType.tni);
		this.parentTypes.add(parentType);
		this.parentTypes.addAll(Arrays.asList(otherParentTypes));
		this.name = name;
	}
	
	@Override
	public String getName() {
		return name;
	}
	
	/**
	 * @return The parent type. Cannot be null.
	 */
	public List<TypeRef> getParentTypes() {
		return parentTypes;
	}
	
	@Override
	public List<MemberAccess> getMembers(Map<TemplateType, TypeRef> templateMap) {
		List<MemberAccess> a = super.getMembers(templateMap);
		
		for (TypeRef t : getParentTypes()) {
			a.addAll(t.getMembers(templateMap));
		}
		
		return a;
	}
	
	@Override
	public boolean canCastTo(TypeRef a, TypeRef b) {
		for (TypeRef parent : ((SystemType)a.getType()).getParentTypes()) {
			if (parent.canCastTo(b)) {
				return true;
			}
		}
		
		return super.canCastTo(a, b);
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("sys:");
		sb.append(getName());
		return sb.toString();
	}
	
	@Override
	public TypeRef commonType(TypeRef a, TypeRef b) {
		// test if the types are direct suptypes of one another
		if (a.equals(b)) {
			return a.copy();
		}
		
		if (b.canCastTo(a) && !a.canCastTo(b)) {
			return a.copy();
		}
		
		if (a.canCastTo(b) && !b.canCastTo(a)) {
			return b.copy();
		}
		
		// the only possibility left: Neither a nor b can cast to each other directly
		List<TypeRef> commons = new ArrayList<>();
		for (TypeRef parent : ((SystemType)a.getType()).getParentTypes()) {
			TypeRef trueParent = TemplateUtils.replaceTemplates(parent, TemplateUtils.matchAllTemplateArgs(a));
			
			if (trueParent.equals(b)) {
				return trueParent;
			}
			
			commons.add(trueParent.commonType(b));
		}
		
		List<TypeRef> commons2 = commons.stream().filter(t1->
			commons.stream().allMatch(t2->(t1 == t2 || !t1.canCastTo(t2)))
		).collect(Collectors.toList());
		
		if (commons2.isEmpty()) {
			return super.commonType(a, b);
		} else if (commons2.size() == 1) {
			return commons2.get(0);
		} else {
			ComboType combo = new ComboType(tni);
			combo.getTypes().addAll(commons2);
			return new TypeRef(combo);
		}
	}
}
