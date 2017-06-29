package info.iconmaster.typhon.language;

import info.iconmaster.typhon.TyphonInput;
import info.iconmaster.typhon.util.SourceInfo;

public class Function extends TyphonLanguageEntity {
	private String name;
	
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
