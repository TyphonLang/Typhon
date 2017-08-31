package info.iconmaster.typhon.types;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import info.iconmaster.typhon.TyphonInput;
import info.iconmaster.typhon.model.MemberAccess;

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
}
