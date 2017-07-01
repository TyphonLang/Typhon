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
	 * The compilation options used to produce this object. Cannot be null.
	 */
	public TyphonInput tni;
	
	/**
	 * The source information for this object. May be null.
	 */
	public SourceInfo source;
	
	/**
	 * The annotations attached to this object.
	 */
	private List<Annotation> annots = new ArrayList<>();
	
	/**
	 * True if we have raw data, and need to be compiled.
	 * May be set to false even if there is raw data (if a user-supplied type has been compiled already, for example).
	 */
	private boolean hasRawData = false;
	
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

	/**
	 * @return True if we have raw data, and need to be compiled.
	 * May be set to false even if there is raw data (if a user-supplied type has been compiled already, for example).
	 */
	public boolean needsCompiled() {
		return hasRawData;
	}

	/**
	 * @param hasRawData True if we have raw data, and need to be compiled.
	 * May be set to false even if there is raw data (if a user-supplied type has been compiled already, for example).
	 */
	public void needsCompiled(boolean hasRawData) {
		this.hasRawData = hasRawData;
	}
	
	/**
	 * Sets the raw ANTLR data for this entity.
	 * Subclasses should call this in thier own setRawData; this function updates important flags.
	 */
	public void setRawData() {
		this.hasRawData = true;
	}
}
