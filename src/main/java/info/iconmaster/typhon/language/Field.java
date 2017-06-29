package info.iconmaster.typhon.language;

import info.iconmaster.typhon.TyphonInput;
import info.iconmaster.typhon.antlr.TyphonParser.ExprContext;
import info.iconmaster.typhon.antlr.TyphonParser.TypeContext;
import info.iconmaster.typhon.tnil.CodeBlock;
import info.iconmaster.typhon.types.Type;
import info.iconmaster.typhon.util.SourceInfo;

public class Field extends TyphonLanguageEntity {
	public String name;
	public Type type;
	public CodeBlock value;
	
	public TypeContext rawType;
	public ExprContext rawValue;
	
	public Field(TyphonInput input) {
		super(input);
	}
	
	public Field(TyphonInput input, SourceInfo source) {
		super(input, source);
	}
}
