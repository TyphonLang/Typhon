package info.iconmaster.typhon.language;

import info.iconmaster.typhon.TyphonInput;
import info.iconmaster.typhon.antlr.TyphonParser.ExprContext;
import info.iconmaster.typhon.antlr.TyphonParser.TypeContext;
import info.iconmaster.typhon.tnil.CodeBlock;
import info.iconmaster.typhon.types.Type;
import info.iconmaster.typhon.util.SourceInfo;

public class Parameter extends TyphonLanguageEntity {
	private String name;
	private Type type;
	private CodeBlock defaultValue;
	
	private TypeContext rawType;
	private ExprContext rawDefaultValue;
	
	public Parameter(TyphonInput input) {
		super(input);
	}

	public Parameter(TyphonInput input, SourceInfo source) {
		super(input, source);
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Type getType() {
		return type;
	}

	public void setType(Type type) {
		this.type = type;
	}

	public CodeBlock getDefaultValue() {
		return defaultValue;
	}

	public void setDefaultValue(CodeBlock defaultValue) {
		this.defaultValue = defaultValue;
	}

	public TypeContext getRawType() {
		return rawType;
	}

	public ExprContext getRawDefaultValue() {
		return rawDefaultValue;
	}
	
	public void setRawData(TypeContext rawType, ExprContext rawDefaultValue) {
		this.rawType = rawType;
		this.rawDefaultValue = rawDefaultValue;
	}
}
