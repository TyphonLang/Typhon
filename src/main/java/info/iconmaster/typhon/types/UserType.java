package info.iconmaster.typhon.types;

import java.util.ArrayList;
import java.util.List;

import info.iconmaster.typhon.TyphonInput;
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
	private TypeRef parent;
	
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
		this.parent = parent;
		this.name = name;
	}

	/**
	 * @param name The name of this type.
	 * @param parent The parent type. Cannot be null.
	 */
	public UserType(SourceInfo source, String name, TypeRef parent) {
		super(parent.tni, source);
		this.parent = parent;
		this.name = name;
	}
	
	/**
	 * @param name The name of this type.
	 * @param parent The parent type. Cannot be null.
	 */
	public UserType(String name, Type parent) {
		super(parent.tni, parent.source);
		this.parent = new TypeRef(parent);
		this.name = name;
	}

	/**
	 * @param name The name of this type.
	 * @param parent The parent type. Cannot be null.
	 */
	public UserType(SourceInfo source, String name, Type parent) {
		super(parent.tni, source);
		this.parent = new TypeRef(parent);
		this.name = name;
	}
	
	@Override
	public String getName() {
		return name;
	}
	
	/**
	 * @param name The new name of this type.
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return The parent type. Cannot be null.
	 */
	public TypeRef getParent() {
		return parent;
	}

	/**
	 * @param parent The new parent type. Cannot be null.
	 */
	public void setParent(TypeRef parent) {
		this.parent = parent;
	}

	/**
	 * @return The template parameters this type has.
	 */
	public List<TemplateType> getTemplates() {
		return templates;
	}
}
