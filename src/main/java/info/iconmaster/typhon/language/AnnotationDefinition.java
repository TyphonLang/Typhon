package info.iconmaster.typhon.language;

import info.iconmaster.typhon.TyphonInput;
import info.iconmaster.typhon.util.SourceInfo;

public class AnnotationDefinition extends TyphonLanguageEntity {
	private String name;
	
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
