package info.iconmaster.typhon.types;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import info.iconmaster.typhon.TyphonInput;
import info.iconmaster.typhon.antlr.TyphonParser.TypeContext;
import info.iconmaster.typhon.model.MemberAccess;
import info.iconmaster.typhon.model.Package;
import info.iconmaster.typhon.model.TemplateArgument;
import info.iconmaster.typhon.util.SourceInfo;

/**
 * This is a type that can be defined by the user (through class declarations).
 * It has a parent type, optionally templates, and can be derived by other types.
 * 
 * @author iconmaster
 *
 */
public class UserType extends Type {
	/**
	 * The parent type. Cannot be null.
	 */
	private List<TypeRef> parentTypes = new ArrayList<>();
	
	/**
	 * The name of this type.
	 */
	private String name;
	
	/**
	 * The ANTLR rule representing the parent types of this type.
	 */
	private List<TypeContext> rawParentTypes;

	/**
	 * The template parameters this type has.
	 */
	private List<TemplateType> templates = new ArrayList<>();
	
	public UserType(TyphonInput input, SourceInfo source, String name) {
		super(input, source);
		this.name = name;
	}

	public UserType(TyphonInput input, String name) {
		super(input);
		this.name = name;
	}

	/**
	 * @param name The name of this type.
	 * @param parentType One or more parent types.
	 */
	public UserType(String name, Type parentType, Type... otherParentTypes) {
		super(parentType.tni);
		this.parentTypes.add(new TypeRef(parentType));
		this.parentTypes.addAll(Arrays.asList(otherParentTypes).stream().map((t)->new TypeRef(t)).collect(Collectors.toList()));
		this.name = name;
	}
	
	/**
	 * @param name The name of this type.
	 * @param parentType One or more parent types.
	 */
	public UserType(String name, TypeRef parentType, TypeRef... otherParentTypes) {
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
	 * @return The parent types of this type.
	 */
	public List<TypeRef> getParentTypes() {
		return parentTypes;
	}

	/**
	 * @return The template parameters this type has.
	 */
	public List<TemplateType> getTemplates() {
		return templates;
	}
	
	/**
	 * @return The ANTLR rule representing the parent types of this type.
	 */
	public List<TypeContext> getRawParentTypes() {
		return rawParentTypes;
	}

	/**
	 * Sets the raw ANTLR data for this type.
	 * 
	 * @param rawParentTypes The ANTLR rule representing the parent types of this type.
	 */
	public void setRawData(List<TypeContext> rawParentTypes) {
		super.setRawData();
		getTypePackage().setRawData();
		this.rawParentTypes = rawParentTypes;
	}
	
	@Override
	public List<MemberAccess> getMembers() {
		List<MemberAccess> a = super.getMembers();
		
		for (TypeRef t : getParentTypes()) {
			a.addAll(t.getMembers());
		}
		
		return a;
	}
	
	@Override
	public List<TemplateType> getMemberTemplate() {
		return getTemplates();
	}
	
	@Override
	public Package getTypePackage() {
		if (typePackage == null) {
			typePackage = new Package(source, null, getParent() == null ? tni.corePackage : getParent());
			for (TemplateType t : templates) {
				typePackage.addType(t);
			}
		}
		
		return typePackage;
	}
	
	@Override
	public boolean canCastTo(TypeRef a, TypeRef b) {
		for (TypeRef parent : ((UserType)a.getType()).getParentTypes()) {
			TypeRef trueParent = TemplateUtils.replaceTemplates(parent, TemplateUtils.matchTemplateArgs(a, getTemplates(), a.getTemplateArgs()));
			if (trueParent.canCastTo(b)) {
				return true;
			}
		}
		
		return super.canCastTo(a, b);
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("user:");
		sb.append(getName());
		if (!getTemplates().isEmpty()) {
			sb.append('<');
			for (TemplateType arg : getTemplates()) {
				sb.append(arg);
				sb.append(',');
			}
			sb.deleteCharAt(sb.length()-1);
			sb.append('>');
		}
		return sb.toString();
	}
}
