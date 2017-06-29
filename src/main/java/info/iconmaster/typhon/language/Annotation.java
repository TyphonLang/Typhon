package info.iconmaster.typhon.language;

import java.util.List;

import info.iconmaster.typhon.TyphonInput;
import info.iconmaster.typhon.util.SourceInfo;

public class Annotation extends TyphonLanguageEntity {
	public AnnotationDefinition definition;
	public List<Argument> args;
	
	public Annotation(TyphonInput input) {
		super(input);
	}
	
	public Annotation(TyphonInput input, SourceInfo source) {
		super(input, source);
	}
}
