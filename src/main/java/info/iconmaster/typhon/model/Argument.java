package info.iconmaster.typhon.model;

import info.iconmaster.typhon.TyphonInput;
import info.iconmaster.typhon.antlr.TyphonParser.ExprContext;
import info.iconmaster.typhon.compiler.CodeBlock;
import info.iconmaster.typhon.util.SourceInfo;

/**
 * This represents an argument given to a function or annotation.
 * It is a Typhon expression, with an optional label (used for setting named parameters).
 * 
 * @author iconmaster
 *
 */
public class Argument extends TyphonModelEntity {
	/**
	 * The label of the named parameter this argument corresponds to.
	 * Must be a valid Typhon identifier.
	 * This is null if the argument is positional.
	 */
	private String label;
	
	/**
	 * The value of the argument.
	 */
	private CodeBlock value;

	/**
	 * The ANTLR rule corresponding to the value.
	 */
	private ExprContext rawValue;

	public Argument(TyphonInput input) {
		super(input);
	}

	public Argument(TyphonInput input, SourceInfo source) {
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
	public CodeBlock getValue() {
		return value;
	}

	/**
	 * @param value The new value of the argument.
	 */
	public void setValue(CodeBlock value) {
		this.value = value;
	}

	/**
	 * Sets the raw ANTLR data for this annotation.
	 * 
	 * @param rawValue The ANTLR rule corresponding to the value.
	 */
	public void setRawData(ExprContext rawValue) {
		super.setRawData();
		this.rawValue = rawValue;
	}

	/**
	 * @return The ANTLR rule corresponding to the value.
	 */
	public ExprContext getRawValue() {
		return rawValue;
	}
}
