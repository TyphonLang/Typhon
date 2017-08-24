package info.iconmaster.typhon.model;

import info.iconmaster.typhon.TyphonInput;
import info.iconmaster.typhon.types.Type;
import info.iconmaster.typhon.types.TypeRef;
import info.iconmaster.typhon.util.SourceInfo;

/**
 * This represents an template argument given to a {@link TypeRef}.
 * It is a Typhon type, with an optional label (used for setting named parameters).
 * 
 * @author iconmaster
 *
 */
public class TemplateArgument extends TyphonModelEntity {
	/**
	 * The label of the named parameter this argument corresponds to.
	 * Must be a valid Typhon identifier.
	 * This is null if the argument is positional.
	 */
	private String label;
	
	/**
	 * The value of the argument.
	 */
	private TypeRef value;

	public TemplateArgument(TyphonInput input) {
		super(input);
	}

	public TemplateArgument(TyphonInput input, SourceInfo source) {
		super(input, source);
	}
	
	public TemplateArgument(Type value) {
		super(value.tni);
		this.value = new TypeRef(value);
	}
	
	public TemplateArgument(TypeRef value) {
		super(value.tni);
		this.value = value;
	}
	
	public TemplateArgument(SourceInfo source, TypeRef value) {
		super(value.tni, source);
		this.value = value;
	}

	/**
	 * @return The label of the named parameter this argument corresponds to. This is null if the argument is positional.
	 */
	public String getLabel() {
		return label;
	}

	/**
	 * @return The new label of the named parameter this argument corresponds to, or null if the argument is positional.
	 */
	public void setLabel(String label) {
		this.label = label;
	}

	/**
	 * @return The value of the argument.
	 */
	public TypeRef getValue() {
		return value;
	}

	/**
	 * @param value The new value of the argument.
	 */
	public void setValue(TypeRef value) {
		this.value = value;
	}
	
	@Override
	public String toString() {
		return getLabel()+"->"+getValue();
	}
	
	/**
	 * @return A copy of this template argument and its ecnlosing type.
	 */
	public TemplateArgument copy() {
		TemplateArgument t = new TemplateArgument(tni, source);
		t.label = label;
		t.value = value.copy();
		return t;
	}
}
