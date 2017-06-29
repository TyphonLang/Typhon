package info.iconmaster.typhon.language;

import info.iconmaster.typhon.TyphonInput;
import info.iconmaster.typhon.antlr.TyphonParser.ExprContext;
import info.iconmaster.typhon.tnil.CodeBlock;
import info.iconmaster.typhon.util.SourceInfo;

public class Argument extends TyphonLanguageEntity {
	private String label;
	private CodeBlock value;

	private ExprContext rawValue;

	public Argument(TyphonInput input) {
		super(input);
	}

	public Argument(TyphonInput input, SourceInfo source) {
		super(input, source);
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public CodeBlock getValue() {
		return value;
	}

	public void setValue(CodeBlock value) {
		this.value = value;
	}

	public void setRawData(ExprContext rawValue) {
		this.rawValue = rawValue;
	}

	public ExprContext getRawValue() {
		return rawValue;
	}
}
