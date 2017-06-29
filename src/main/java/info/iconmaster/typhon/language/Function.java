package info.iconmaster.typhon.language;

import java.util.ArrayList;
import java.util.List;

import info.iconmaster.typhon.TyphonInput;
import info.iconmaster.typhon.antlr.TyphonParser.ExprContext;
import info.iconmaster.typhon.antlr.TyphonParser.StatContext;
import info.iconmaster.typhon.antlr.TyphonParser.TypeContext;
import info.iconmaster.typhon.tnil.CodeBlock;
import info.iconmaster.typhon.types.TemplateType;
import info.iconmaster.typhon.types.Type;
import info.iconmaster.typhon.util.SourceInfo;

public class Function extends TyphonLanguageEntity {
	private String name;
	public List<Type> retType = new ArrayList<>();
	public List<TemplateType> template = new ArrayList<>();
	public List<Parameter> params = new ArrayList<>();
	public CodeBlock code;
	
	public List<TypeContext> rawRetType;
	
	public Form form;
	public List<StatContext> rawCodeBlockForm;
	public ExprContext rawCodeExprForm;
	
	public static enum Form {
		BLOCK,
		EXPR,
	}
	
	public Function(TyphonInput input) {
		super(input);
	}
	
	public Function(TyphonInput input, SourceInfo source) {
		super(input, source);
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}
