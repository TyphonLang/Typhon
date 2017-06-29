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
	private List<Type> retType = new ArrayList<>();
	private List<TemplateType> template = new ArrayList<>();
	private List<Parameter> params = new ArrayList<>();
	private CodeBlock code;
	
	private List<TypeContext> rawRetType;
	
	private Form form;
	private List<StatContext> rawCodeBlockForm;
	private ExprContext rawCodeExprForm;
	
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

	public List<Type> getRetType() {
		return retType;
	}

	public void setRetType(List<Type> retType) {
		this.retType = retType;
	}

	public List<TemplateType> getTemplate() {
		return template;
	}

	public void setTemplate(List<TemplateType> template) {
		this.template = template;
	}

	public List<Parameter> getParams() {
		return params;
	}

	public void setParams(List<Parameter> params) {
		this.params = params;
	}

	public CodeBlock getCode() {
		return code;
	}

	public void setCode(CodeBlock code) {
		this.code = code;
	}

	public List<TypeContext> getRawRetType() {
		return rawRetType;
	}

	public Form getForm() {
		return form;
	}

	public List<StatContext> getRawCodeBlockForm() {
		return rawCodeBlockForm;
	}

	public ExprContext getRawCodeExprForm() {
		return rawCodeExprForm;
	}
	
	public void setRawData(List<TypeContext> rawRetType, Form form, Object rawCode) {
		this.rawRetType = rawRetType;
		this.form = form;
		
		if (rawCode instanceof List) {
			this.rawCodeBlockForm = ((List)rawCode);
		} else if (rawCode instanceof ExprContext) {
			this.rawCodeExprForm = ((ExprContext)rawCode);
		} else {
			throw new IllegalArgumentException("rawCode must be either List<StatContext> or ExprContext");
		}
	}
}
