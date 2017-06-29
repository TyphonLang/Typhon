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
	private Type parent;
	
	/**
	 * The name of this type.
	 */
	private String name;

	/**
	 * @param name The name of this type.
	 * @param parent The parent type. Cannot be null.
	 */
	public SystemType(String name, Type parent) {
		super(parent.tni, parent.source);
		this.name = name;
		this.parent = parent;
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
	public Type getParent() {
		return parent;
	}
	
	/**
	 * @param parent The parent type. Cannot be null.
	 */
	public void setParent(Type parent) {
		this.parent = parent;
	}
}
