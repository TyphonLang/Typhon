package info.iconmaster.typhon.types;

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
	private TypeRef parentType;
	
	/**
	 * The name of this type.
	 */
	private String name;

	/**
	 * @param name The name of this type.
	 * @param parentType The parent type. Cannot be null.
	 */
	public SystemType(String name, TypeRef parentType) {
		super(parentType.tni, parentType.source);
		this.name = name;
		this.parentType = parentType;
	}
	
	/**
	 * @param name The name of this type.
	 * @param parentType The parent type. Cannot be null.
	 */
	public SystemType(String name, Type parentType) {
		super(parentType.tni, parentType.source);
		this.name = name;
		this.parentType = new TypeRef(parentType);
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
	public TypeRef getParentType() {
		return parentType;
	}
	
	/**
	 * @param parentType The parent type. Cannot be null.
	 */
	public void setParentType(TypeRef parentType) {
		this.parentType = parentType;
	}
}
