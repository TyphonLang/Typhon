package info.iconmaster.typhon.language;

import java.util.ArrayList;
import java.util.List;

import info.iconmaster.typhon.TyphonInput;
import info.iconmaster.typhon.antlr.TyphonParser.TypeContext;
import info.iconmaster.typhon.types.Type;
import info.iconmaster.typhon.util.SourceInfo;

public class AnnotationDefinition extends TyphonLanguageEntity {
	private String name;
	public List<Parameter> params = new ArrayList<>();
	
	public List<TypeContext> rawParams;
	
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
}
