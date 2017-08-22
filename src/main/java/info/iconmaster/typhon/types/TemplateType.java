package info.iconmaster.typhon.types;

import java.util.List;

import info.iconmaster.typhon.TyphonInput;
import info.iconmaster.typhon.antlr.TyphonParser.TypeContext;
import info.iconmaster.typhon.model.MemberAccess;
import info.iconmaster.typhon.model.Package;
import info.iconmaster.typhon.model.TemplateArgument;
import info.iconmaster.typhon.util.SourceInfo;

/**
 * These types are template parameters.
 * They erase to their base type in code, but are retained in type data.
 * 
 * @author iconmaster
 *
 */
public class TemplateType extends Type {
	/**
	 * The name of the type.
	 */
	private String name;
	
	/**
	 * The most broad type that this template will accept.
	 */
	private TypeRef baseType;
	
	/**
	 * If no value is supplied at instantiation, this value is used.
	 * Must be castable to this.baseType.
	 */
	private TypeRef defaultValue;
	
	/**
	 * The ANTLR rule representing the base type.
	 */
	private TypeContext rawBaseType;
	
	/**
	 * The ANTLR rule representing the default type.
	 */
	private TypeContext rawDefaultValue;
	
	public TemplateType(TyphonInput input, String name) {
		super(input);
		this.name = name;
		
		baseType = new TypeRef(input.corePackage.TYPE_ANY);
	}
	
	public TemplateType(TyphonInput input, SourceInfo source, String name) {
		super(input, source);
		this.name = name;
		
		baseType = new TypeRef(input.corePackage.TYPE_ANY);
	}

	@Override
	public String getName() {
		return name;
	}

	/**
	 * @return The most broad type that this template will accept.
	 */
	public TypeRef getBaseType() {
		return baseType;
	}

	/**
	 * @param baseType The new most broad type that this template will accept.
	 */
	public void setBaseType(TypeRef baseType) {
		this.baseType = baseType;
	}

	/**
	 * If no value is supplied at instantiation, this value is used.
	 * Must be castable to this.baseType.
	 * May be null if no default value is specified. In that case, use the base type.
	 * @return The default type.
	 */
	public TypeRef getDefaultValue() {
		return defaultValue;
	}

	/**
	 * If no value is supplied at instantiation, this value is used.
	 * Must be castable to this.baseType.
	 * 
	 * @param defaultValue The new default type.
	 */
	public void setDefaultValue(TypeRef defaultValue) {
		this.defaultValue = defaultValue;
	}

	/**
	 * @return The ANTLR rule representing the base type.
	 */
	public TypeContext getRawBaseType() {
		return rawBaseType;
	}

	/**
	 * @return The ANTLR rule representing the default type.
	 */
	public TypeContext getRawDefaultValue() {
		return rawDefaultValue;
	}
	
	/**
	 * Sets the raw ANTLR data for this type.
	 * 
	 * @param rawBaseType The ANTLR rule representing the base type.
	 * @param rawDefaultValue The ANTLR rule representing the default type.
	 */
	public void setRawData(TypeContext rawBaseType, TypeContext rawDefaultValue) {
		super.setRawData();
		this.rawBaseType = rawBaseType;
		this.rawDefaultValue = rawDefaultValue;
	}
	
	@Override
	public List<MemberAccess> getMembers() {
		return baseType.getMembers();
	}
	
	@Override
	public Package getTypePackage() {
		return baseType.getType().getTypePackage();
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("temp:");
		sb.append(getName());
		return sb.toString();
	}
	
	@Override
	public boolean canCastTo(TypeRef a, TypeRef b) {
		return getBaseType().canCastTo(b);
	}
}
