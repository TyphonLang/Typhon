package info.iconmaster.typhon.model;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import info.iconmaster.typhon.TyphonInput;
import info.iconmaster.typhon.util.SourceInfo;

/**
 * The base class for parts of the Typhon model.
 * Contains helper methods and common storage for source info, compilation options, and annotations.
 * 
 * @author iconmaster
 *
 */
public class TyphonModelEntity {
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
	 */
	private boolean needsCompiled = false;
	
	public TyphonModelEntity(TyphonInput tni) {
		this.tni = tni;
	}
	
	public TyphonModelEntity(TyphonInput tni, SourceInfo source) {
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
		return needsCompiled;
	}

	/**
	 * @param hasRawData True if we have raw data, and need to be compiled.
	 * May be set to false even if there is raw data (if a user-supplied type has been compiled already, for example).
	 */
	public void needsCompiled(boolean hasRawData) {
		this.needsCompiled = hasRawData;
	}
	
	/**
	 * Sets the raw ANTLR data for this entity.
	 * Subclasses should call this in thier own setRawData; this function updates important flags.
	 */
	public void setRawData() {
		this.needsCompiled = true;
		this.needsTypesResolved = true;
		this.needsAnnotsChecked = true;
	}
	
	/**
	 * True if this entity comes from a library.
	 */
	private boolean isLibrary;
	
	/**
	 * @return True if this entity comes from a library.
	 */
	public boolean isLibrary() {
		return isLibrary;
	}
	
	/**
	 * Marks this, and all children, as a library.
	 */
	public void markAsLibrary() {
		this.isLibrary = true;
	}

	/**
	 * True if we have raw data, and need our types resolved.
	 */
	private boolean needsTypesResolved = false;
	
	/**
	 * @return True if we have raw data, and need our types resolved.
	 */
	public boolean needsTypesResolved() {
		return needsTypesResolved;
	}

	/**
	 * @param needsTypesResolved True if we have raw data, and need our types resolved.
	 */
	public void needsTypesResolved(boolean needsTypesResolved) {
		this.needsTypesResolved = needsTypesResolved;
	}
	
	/**
	 * @param def The annotation you are searching for.
	 * @return All the annotations with that definition.
	 */
	public List<Annotation> getAnnots(AnnotationDefinition def) {
		return getAnnots().stream().filter((a)->def.equals(a.getDefinition())).collect(Collectors.toList());
	}
	
	/**
	 * @param def The annotation you are searching for.
	 * @return Whether or not this object is annotated with that annotation.
	 */
	public boolean hasAnnot(AnnotationDefinition def) {
		return getAnnots().stream().anyMatch((a)->def.equals(a.getDefinition()));
	}
	
	/**
	 * True if we have raw data, and need our annotations checked.
	 */
	private boolean needsAnnotsChecked = false;
	
	/**
	 * @return True if we have raw data, and need our our annotations checked.
	 */
	public boolean needsAnnotsChecked() {
		return needsAnnotsChecked;
	}

	/**
	 * @param needsTypesResolved True if we have raw data, and need our our annotations checked.
	 */
	public void needsAnnotsChecked(boolean needsTypesResolved) {
		this.needsAnnotsChecked = needsTypesResolved;
	}
}
