package info.iconmaster.typhon.types;

import info.iconmaster.typhon.TyphonInput;
import info.iconmaster.typhon.antlr.TyphonParser.TypeContext;
import info.iconmaster.typhon.util.SourceInfo;

public class TemplateType extends Type {
	private String name;
	private Type baseType;
	private Type defaultValue;
	
	private TypeContext rawBaseType;
	private TypeContext rawDefaultValue;
	
	public TemplateType(TyphonInput input) {
		super(input);
	}
	
	public TemplateType(TyphonInput input, SourceInfo source) {
		super(input, source);
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Type getBaseType() {
		return baseType;
	}

	public void setBaseType(Type baseType) {
		this.baseType = baseType;
	}

	public Type getDefaultValue() {
		return defaultValue;
	}

	public void setDefaultValue(Type defaultValue) {
		this.defaultValue = defaultValue;
	}

	public TypeContext getRawBaseType() {
		return rawBaseType;
	}

	public TypeContext getRawDefaultValue() {
		return rawDefaultValue;
	}
	
	public void setRawData(TypeContext rawBaseType, TypeContext rawDefaultValue) {
		this.rawBaseType = rawBaseType;
		this.rawDefaultValue = rawDefaultValue;
	}
}
