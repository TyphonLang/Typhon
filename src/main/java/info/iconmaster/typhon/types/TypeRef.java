package info.iconmaster.typhon.types;

import java.util.ArrayList;
import java.util.List;

import info.iconmaster.typhon.TyphonInput;
import info.iconmaster.typhon.language.TemplateArgument;
import info.iconmaster.typhon.language.TyphonLanguageEntity;
import info.iconmaster.typhon.util.SourceInfo;

/**
 * This represents a reference to a type, possibly with templates instantiated.
 * 
 * @author iconmaster
 *
 */
public class TypeRef extends TyphonLanguageEntity {
	/**
	 * The type definition of this reference.
	 */
	private Type type;
	
	/**
	 * The templates this type has been instantiated with.
	 * Must be empty if this type does not support templates.
	 */
	private List<TemplateArgument> templateArgs = new ArrayList<>();

	public TypeRef(TyphonInput tni) {
		super(tni);
	}

	public TypeRef(TyphonInput tni, SourceInfo source) {
		super(tni, source);
	}
	
	public TypeRef(Type type) {
		super(type.tni, type.source);
		this.type = type;
	}
	
	public TypeRef(SourceInfo source, Type type) {
		super(type.tni, source);
		this.type = type;
	}

	/**
	 * @return The type definition of this reference.
	 */
	public Type getType() {
		return type;
	}

	/**
	 * @param type The new type definition of this reference.
	 */
	public void setType(Type type) {
		this.type = type;
	}

	/**
	 * @return The templates this type has been instantiated with. Must be empty if this type does not support templates.
	 */
	public List<TemplateArgument> getTemplateArgs() {
		return templateArgs;
	}
}
