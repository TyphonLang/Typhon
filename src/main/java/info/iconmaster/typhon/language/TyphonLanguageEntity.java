package info.iconmaster.typhon.language;

import java.util.ArrayList;
import java.util.List;

import info.iconmaster.typhon.TyphonInput;
import info.iconmaster.typhon.util.SourceInfo;

public class TyphonLanguageEntity {
	public TyphonInput tni;
	public SourceInfo source;
	public List<Annotation> annots = new ArrayList<>();
	
	public TyphonLanguageEntity(TyphonInput input) {
		this.tni = input;
	}
	
	public TyphonLanguageEntity(TyphonInput input, SourceInfo source) {
		this.tni = input;
		this.source = source;
	}
}
