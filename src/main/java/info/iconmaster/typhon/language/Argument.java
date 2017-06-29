package info.iconmaster.typhon.language;

import info.iconmaster.typhon.TyphonInput;
import info.iconmaster.typhon.antlr.TyphonParser.ExprContext;
import info.iconmaster.typhon.tnil.CodeBlock;
import info.iconmaster.typhon.util.SourceInfo;

public class Argument extends TyphonLanguageEntity {
	String label;
	CodeBlock value;
	
	ExprContext rawValue;

	public Argument(TyphonInput input) {
		super(input);
	}

	public Argument(TyphonInput input, SourceInfo source) {
		super(input, source);
	}
}
