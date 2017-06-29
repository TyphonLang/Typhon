package info.iconmaster.typhon.language;

import java.util.ArrayList;
import java.util.List;

import info.iconmaster.typhon.TyphonInput;
import info.iconmaster.typhon.util.SourceInfo;

/**
 * The base class for parts of the Typhon model.
 * Contains helper methods and common storage for source info, compilation options, and annotations.
 * 
 * @author iconmaster
 *
 */
public class TyphonLanguageEntity {
	/**
	 * The compilation options used to produce this object.
	 */
	public TyphonInput tni;
	
	/**
	 * The source information for this object.
	 */
	public SourceInfo source;
	
	/**
	 * The annotations attached to this object.
	 */
	private List<Annotation> annots = new ArrayList<>();
	
	public TyphonLanguageEntity(TyphonInput tni) {
		this.tni = tni;
	}
	
	public TyphonLanguageEntity(TyphonInput tni, SourceInfo source) {
		this.tni = tni;
		this.source = source;
	}

	/**
	 * @return The annotations attached to this object.
	 */
	public List<Annotation> getAnnots() {
		return annots;
	}
}
