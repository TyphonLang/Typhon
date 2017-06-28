package info.iconmaster.typhon.language;

import info.iconmaster.typhon.TyphonInput;
import info.iconmaster.typhon.util.SourceInfo;

public class Field extends TyphonLanguageEntity {
	String name;

	public Field(TyphonInput input) {
		super(input);
	}

	public Field(TyphonInput input, SourceInfo source) {
		super(input, source);
	}
}
