package info.iconmaster.typhon.types;

import info.iconmaster.typhon.TyphonInput;
import info.iconmaster.typhon.antlr.TyphonParser.TypeContext;
import info.iconmaster.typhon.util.SourceInfo;

/**
 * These types are template parameters.
 * They erase to their base type in code, but are retained in type data.
 * 
 * @author iconmaster
 *
 */
public class TemplateType extends Type {
	/**
	 * The name of the type.
	 */
	private String name;
	
	/**
	 * The most broad type that this template will accept.
	 */
	private Type baseType;
	
	/**
	 * If no value is supplied at instantiation, this value is used.
	 * Must be castable to this.baseType.
	 */
	private Type defaultValue;
	
	/**
	 * The ANTLR rule representing the base type.
	 */
	private TypeContext rawBaseType;
	
	/**
	 * The ANTLR rule representing the default type.
	 */
	private TypeContext rawDefaultValue;
	
	public TemplateType(TyphonInput input) {
		super(input);
	}
	
	public TemplateType(TyphonInput input, SourceInfo source) {
		super(input, source);
	}

	@Override
	public String getName() {
		return name;
	}

	/**
	 * @param name The new name of the type.
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return The most broad type that this template will accept.
	 */
	public Type getBaseType() {
		return baseType;
	}

	/**
	 * @param baseType The new most broad type that this template will accept.
	 */
	public void setBaseType(Type baseType) {
		this.baseType = baseType;
	}

	/**
	 * If no value is supplied at instantiation, this value is used.
	 * Must be castable to this.baseType.
	 * @return The default type.
	 */
	public Type getDefaultValue() {
		return defaultValue;
	}

	/**
	 * If no value is supplied at instantiation, this value is used.
	 * Must be castable to this.baseType.
	 * 
	 * @param defaultValue The new default type.
	 */
	public void setDefaultValue(Type defaultValue) {
		this.defaultValue = defaultValue;
	}

	/**
	 * @return The ANTLR rule representing the base type.
	 */
	public TypeContext getRawBaseType() {
		return rawBaseType;
	}

	/**
	 * @return The ANTLR rule representing the default type.
	 */
	public TypeContext getRawDefaultValue() {
		return rawDefaultValue;
	}
	
	/**
	 * Sets the raw ANTLR data for this type.
	 * 
	 * @param rawBaseType The ANTLR rule representing the base type.
	 * @param rawDefaultValue The ANTLR rule representing the default type.
	 */
	public void setRawData(TypeContext rawBaseType, TypeContext rawDefaultValue) {
		this.rawBaseType = rawBaseType;
		this.rawDefaultValue = rawDefaultValue;
	}
}
