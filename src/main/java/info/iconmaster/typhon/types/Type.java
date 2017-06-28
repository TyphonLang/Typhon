package info.iconmaster.typhon.types;

import info.iconmaster.typhon.TyphonInput;
import info.iconmaster.typhon.language.Package;
import info.iconmaster.typhon.language.TyphonLanguageEntity;

public abstract class Type extends TyphonLanguageEntity {
	Package typePackage;

	public Type(TyphonInput input) {
		super(input);
	}
}
