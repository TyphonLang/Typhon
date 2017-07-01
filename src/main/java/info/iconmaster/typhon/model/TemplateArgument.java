package info.iconmaster.typhon.model;

import info.iconmaster.typhon.TyphonInput;
import info.iconmaster.typhon.antlr.TyphonParser.TypeContext;
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

	/**
	 * The ANTLR rule corresponding to the value.
	 */
	private TypeContext rawValue;

	public TemplateArgument(TyphonInput input) {
		super(input);
	}

	public TemplateArgument(TyphonInput input, SourceInfo source) {
		super(input, source);
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

	/**
	 * Sets the raw ANTLR data for this annotation.
	 * 
	 * @param rawValue The ANTLR rule corresponding to the value.
	 */
	public void setRawData(TypeContext rawValue) {
		super.setRawData();
		this.rawValue = rawValue;
	}

	/**
	 * @return The ANTLR rule corresponding to the value.
	 */
	public TypeContext getRawValue() {
		return rawValue;
	}
}
