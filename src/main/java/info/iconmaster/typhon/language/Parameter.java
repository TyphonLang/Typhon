package info.iconmaster.typhon.language;

import info.iconmaster.typhon.TyphonInput;
import info.iconmaster.typhon.antlr.TyphonParser.ExprContext;
import info.iconmaster.typhon.antlr.TyphonParser.TypeContext;
import info.iconmaster.typhon.tnil.CodeBlock;
import info.iconmaster.typhon.types.Type;
import info.iconmaster.typhon.util.SourceInfo;

public class Parameter extends TyphonLanguageEntity {
	public String name;
	public Type type;
	public CodeBlock defaultValue;
	
	public TypeContext rawType;
	public ExprContext rawDefaultValue;
	
	public Parameter(TyphonInput input) {
		super(input);
	}

	public Parameter(TyphonInput input, SourceInfo source) {
		super(input, source);
	}
}
