package info.iconmaster.typhon.language;

import info.iconmaster.typhon.TyphonInput;
import info.iconmaster.typhon.antlr.TyphonParser.ExprContext;
import info.iconmaster.typhon.antlr.TyphonParser.TypeContext;
import info.iconmaster.typhon.tnil.CodeBlock;
import info.iconmaster.typhon.types.TypeRef;
import info.iconmaster.typhon.util.SourceInfo;

/**
 * This represents a Typhon field- Either a global variable, or a member of a class.
 * 
 * @author iconmaster
 *
 */
public class Field extends TyphonLanguageEntity {
	/**
	 * The name of this field. Must be a valid Typhon identifier.
	 */
	public String name;
	
	/**
	 * The type of this field.
	 */
	public TypeRef type;
	
	/**
	 * This code is run before static initializers, and returns the initial value of this field.
	 */
	public CodeBlock value;
	
	/**
	 * The ANTLR rule representing the type of this field.
	 */
	public TypeContext rawType;
	
	/**
	 * The ANTLR rule representing the initial value of this field.
	 */
	public ExprContext rawValue;
	
	public Field(TyphonInput input) {
		super(input);
	}
	
	public Field(TyphonInput input, SourceInfo source) {
		super(input, source);
	}
	
	/**
	 * @return The name of this field.
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name The new name of this field. Must be a valid Typhon identifier.
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return The type of this field.
	 */
	public TypeRef getType() {
		return type;
	}

	/**
	 * @param type The new type of this field.
	 */
	public void setType(TypeRef type) {
		this.type = type;
	}

	/**
	 * This {@link CodeBlock} is run before static initializers, and returns the initial value of this field.
	 * 
	 * @return The initial value of this field.
	 */
	public CodeBlock getValue() {
		return value;
	}

	/**
	 * This {@link CodeBlock} is run before static initializers, and returns the initial value of this field.
	 * 
	 * @return The new initial value of this field.
	 */
	public void setValue(CodeBlock value) {
		this.value = value;
	}
	
	/**
	 * Sets the raw ANTLR data for this field.
	 * 
	 * @param rawType The ANTLR rule representing the type of this field.
	 * @param rawValue The ANTLR rule representing the initial value of this field.
	 */
	public void setRawData(TypeContext rawType, ExprContext rawValue) {
		this.rawType = rawType;
		this.rawValue = rawValue;
	}

	/**
	 * @return The ANTLR rule representing the type of this field.
	 */
	public TypeContext getRawType() {
		return rawType;
	}

	/**
	 * @return The ANTLR rule representing the initial value of this field.
	 */
	public ExprContext getRawValue() {
		return rawValue;
	}
}
