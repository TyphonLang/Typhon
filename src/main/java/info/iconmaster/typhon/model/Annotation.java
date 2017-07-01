package info.iconmaster.typhon.model;

import java.util.ArrayList;
import java.util.List;

import info.iconmaster.typhon.TyphonInput;
import info.iconmaster.typhon.antlr.TyphonParser.PackageNameContext;
import info.iconmaster.typhon.util.SourceInfo;

/**
 * This represents an instance of an {@link AnnotationDefinition}, with arguments supplied.
 * 
 * @see AnnotationDefinition
 * @author iconmaster
 *
 */
public class Annotation extends TyphonModelEntity {
	/**
	 * The annotation this is an instance of.
	 */
	private AnnotationDefinition definition;
	
	/**
	 * The arguments supplied to this annotation.
	 */
	private List<Argument> args = new ArrayList<>();
	
	/**
	 * The ANTLR rule representing the potentially-qualified name of the annotation we want to be.
	 */
	private PackageNameContext rawDefinition;
	
	public Annotation(TyphonInput input) {
		super(input);
	}
	
	public Annotation(TyphonInput input, SourceInfo source) {
		super(input, source);
	}
	
	/**
	 * @return The annotation this is an instance of.
	 */
	public AnnotationDefinition getDefinition() {
		return definition;
	}

	/**
	 * @param definition The new annotation this is an instance of.
	 */
	public void setDefinition(AnnotationDefinition definition) {
		this.definition = definition;
	}

	/**
	 * @return The arguments supplied to this annotation.
	 */
	public List<Argument> getArgs() {
		return args;
	}

	/**
	 * @return The ANTLR rule representing the potentially-qualified name of the annotation we want to be.
	 */
	public PackageNameContext getRawDefinition() {
		return rawDefinition;
	}

	/**
	 * Sets the raw ANTLR data for this annotation.
	 * 
	 * @param rawDefinition The ANTLR rule representing the potentially-qualified name of the annotation we want to be.
	 */
	public void setRawData(PackageNameContext rawDefinition) {
		super.setRawData();
		this.rawDefinition = rawDefinition;
	}
}
