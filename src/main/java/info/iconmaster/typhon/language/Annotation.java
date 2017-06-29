package info.iconmaster.typhon.language;

import java.util.ArrayList;
import java.util.List;

import info.iconmaster.typhon.TyphonInput;
import info.iconmaster.typhon.util.SourceInfo;

public class Annotation extends TyphonLanguageEntity {
	private AnnotationDefinition definition;
	private List<Argument> args = new ArrayList<>();
	
	public Annotation(TyphonInput input) {
		super(input);
	}
	
	public Annotation(TyphonInput input, SourceInfo source) {
		super(input, source);
	}

	public AnnotationDefinition getDefinition() {
		return definition;
	}

	public void setDefinition(AnnotationDefinition definition) {
		this.definition = definition;
	}

	public List<Argument> getArgs() {
		return args;
	}

	public void setArgs(List<Argument> args) {
		this.args = args;
	}
}
