package info.iconmaster.typhon.types;

import java.util.ArrayList;
import java.util.List;

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
	private TypeRef parentType;
	
	/**
	 * The name of this type.
	 */
	private String name;
	
	/**
	 * The template parameters this type has.
	 */
	private List<TemplateType> templates = new ArrayList<>();
	
	/**
	 * @param name The name of this type.
	 * @param parent The parent type. Cannot be null.
	 */
	public UserType(String name, TypeRef parent) {
		super(parent.tni, parent.source);
		this.parentType = parent;
		this.name = name;
	}

	/**
	 * @param name The name of this type.
	 * @param parent The parent type. Cannot be null.
	 */
	public UserType(SourceInfo source, String name, TypeRef parent) {
		super(parent.tni, source);
		this.parentType = parent;
		this.name = name;
	}
	
	/**
	 * @param name The name of this type.
	 * @param parentType The parent type. Cannot be null.
	 */
	public UserType(String name, Type parentType) {
		super(parentType.tni, parentType.source);
		this.parentType = new TypeRef(parentType);
		this.name = name;
	}

	/**
	 * @param name The name of this type.
	 * @param parentType The parent type. Cannot be null.
	 */
	public UserType(SourceInfo source, String name, Type parentType) {
		super(parentType.tni, source);
		this.parentType = new TypeRef(parentType);
		this.name = name;
	}
	
	@Override
	public String getName() {
		return name;
	}

	/**
	 * @return The parent type. Cannot be null.
	 */
	public TypeRef getParentType() {
		return parentType;
	}

	/**
	 * @param parentType The new parent type. Cannot be null.
	 */
	public void setParentType(TypeRef parentType) {
		this.parentType = parentType;
	}

	/**
	 * @return The template parameters this type has.
	 */
	public List<TemplateType> getTemplates() {
		return templates;
	}
}
