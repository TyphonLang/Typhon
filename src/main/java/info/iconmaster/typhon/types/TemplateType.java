package info.iconmaster.typhon.types;

import info.iconmaster.typhon.TyphonInput;
import info.iconmaster.typhon.antlr.TyphonParser.TypeContext;
import info.iconmaster.typhon.util.SourceInfo;

public class TemplateType extends Type {
	public String name;
	public Type baseType;
	public Type defaultValue;
	
	public TypeContext rawBaseType;
	public TypeContext rawDefaultValue;
	
	public TemplateType(TyphonInput input) {
		super(input);
	}
	
	public TemplateType(TyphonInput input, SourceInfo source) {
		super(input, source);
	}
}
