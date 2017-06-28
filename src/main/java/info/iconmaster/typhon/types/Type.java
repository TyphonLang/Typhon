package info.iconmaster.typhon.types;

import info.iconmaster.typhon.TyphonInput;
import info.iconmaster.typhon.language.Package;
import info.iconmaster.typhon.language.TyphonLanguageEntity;
import info.iconmaster.typhon.util.SourceInfo;

public abstract class Type extends TyphonLanguageEntity {
	Package typePackage;

	public Type(TyphonInput input) {
		super(input);
	}

	public Type(TyphonInput input, SourceInfo source) {
		super(input, source);
	}
}
