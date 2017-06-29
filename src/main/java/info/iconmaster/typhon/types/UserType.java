package info.iconmaster.typhon.types;

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
	private Type parent;
	
	/**
	 * The name of this type.
	 */
	private String name;

	/**
	 * @param parent The parent type. Cannot be null.
	 */
	public UserType(String name, Type parent) {
		super(parent.tni, parent.source);
		this.name = name;
		this.parent = parent;
	}
	
	/**
	 * @param name The name of this type.
	 * @param parent The parent type. Cannot be null.
	 */
	public UserType(TyphonInput input, Type parent, String name) {
		super(input, parent.source);
		this.parent = parent;
		this.name = name;
	}

	/**
	 * @param name The name of this type.
	 * @param parent The parent type. Cannot be null.
	 */
	public UserType(TyphonInput input, SourceInfo source, Type parent, String name) {
		super(input, source);
		this.parent = parent;
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
	public Type getParent() {
		return parent;
	}

	/**
	 * @param parent The new parent type. Cannot be null.
	 */
	public void setParent(Type parent) {
		this.parent = parent;
	}
}
