package info.iconmaster.typhon.language;

import info.iconmaster.typhon.TyphonInput;
import info.iconmaster.typhon.antlr.TyphonParser.ExprContext;
import info.iconmaster.typhon.antlr.TyphonParser.TypeContext;
import info.iconmaster.typhon.tnil.CodeBlock;
import info.iconmaster.typhon.types.TypeRef;
import info.iconmaster.typhon.util.SourceInfo;

public class Parameter extends TyphonLanguageEntity {
	/**
	 * The name of this parameter. Must be a valid Typhon identifier.
	 */
	private String name;
	
	/**
	 * The type of this parameter.
	 */
	private TypeRef type;
	
	/**
	 * The code to execute when an argument is not supplied by a caller.
	 */
	private CodeBlock defaultValue;
	
	/**
	 * The ANTLR rule representing the type of this parameter.
	 */
	private TypeContext rawType;
	
	/**
	 * The ANTLR rule representing the default value of this parameter.
	 */
	private ExprContext rawDefaultValue;
	
	public Parameter(TyphonInput input) {
		super(input);
	}

	public Parameter(TyphonInput input, SourceInfo source) {
		super(input, source);
	}

	/**
	 * @return The name of this parameter.
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param The new name of this parameter. Must be a valid Typhon identifier.
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return The type of this parameter.
	 */
	public TypeRef getType() {
		return type;
	}

	/**
	 * @param type The new type of this parameter.
	 */
	public void setType(TypeRef type) {
		this.type = type;
	}

	/**
	 * @return The code to execute when an argument is not supplied by a caller.
	 */
	public CodeBlock getDefaultValue() {
		return defaultValue;
	}

	/**
	 * @return The new code to execute when an argument is not supplied by a caller.
	 */
	public void setDefaultValue(CodeBlock defaultValue) {
		this.defaultValue = defaultValue;
	}

	/**
	 * @return The ANTLR rule representing the type of this parameter.
	 */
	public TypeContext getRawType() {
		return rawType;
	}

	/**
	 * @return The ANTLR rule representing the default value of this parameter.
	 */
	public ExprContext getRawDefaultValue() {
		return rawDefaultValue;
	}
	
	/**
	 * Sets the raw ANTLR data for this parameter.
	 * 
	 * @param rawType The ANTLR rule representing the type of this parameter.
	 * @param rawDefaultValue The ANTLR rule representing the default value of this parameter.
	 */
	public void setRawData(TypeContext rawType, ExprContext rawDefaultValue) {
		super.setRawData();
		this.rawType = rawType;
		this.rawDefaultValue = rawDefaultValue;
	}
}
