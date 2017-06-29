package info.iconmaster.typhon.language;

import java.util.ArrayList;
import java.util.List;

import info.iconmaster.typhon.TyphonInput;
import info.iconmaster.typhon.antlr.TyphonParser.TypeContext;
import info.iconmaster.typhon.util.SourceInfo;

public class AnnotationDefinition extends TyphonLanguageEntity {
	private String name;
	private List<Parameter> params = new ArrayList<>();
	
	private List<TypeContext> rawParams;
	
	public AnnotationDefinition(TyphonInput input, String name) {
		super(input);
		this.name = name;
	}
	
	public AnnotationDefinition(TyphonInput input, SourceInfo source, String name) {
		super(input, source);
		this.name = name;
	}
	
	public String getName() {
		return name;
	}

	public List<Parameter> getParams() {
		return params;
	}

	public void setParams(List<Parameter> params) {
		this.params = params;
	}
	
	public void setRawData(List<TypeContext> rawParams) {
		this.rawParams = rawParams;
	}
	
	public List<TypeContext> getRawParams() {
		return rawParams;
	}
}
