package info.iconmaster.typhon.model;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import info.iconmaster.typhon.TyphonInput;
import info.iconmaster.typhon.antlr.TyphonParser.ExprContext;
import info.iconmaster.typhon.antlr.TyphonParser.TypeContext;
import info.iconmaster.typhon.compiler.CodeBlock;
import info.iconmaster.typhon.types.TemplateType;
import info.iconmaster.typhon.types.Type;
import info.iconmaster.typhon.types.TypeRef;
import info.iconmaster.typhon.util.SourceInfo;
import info.iconmaster.typhon.util.TemplateUtils;

/**
 * This represents a Typhon field- Either a global variable, or a member of a class.
 * 
 * @author iconmaster
 *
 */
public class Field extends TyphonModelEntity implements MemberAccess {
	/**
	 * The name of this field. Must be a valid Typhon identifier.
	 */
	public String name;
	
	/**
	 * The type of this field.
	 */
	public TypeRef type;
	
	/**
	 * This code is run before static initializers, and returns the initial value of this field.
	 */
	public CodeBlock value;
	
	/**
	 * The ANTLR rule representing the type of this field.
	 */
	public TypeContext rawType;
	
	/**
	 * The ANTLR rule representing the initial value of this field.
	 */
	public ExprContext rawValue;
	
	public Field(TyphonInput input, String name) {
		super(input);
		this.name = name;
	}
	
	public Field(TyphonInput input, SourceInfo source, String name) {
		super(input, source);
		this.name = name;
	}
	
	/**
	 * @return The name of this field.
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return The type of this field.
	 */
	public TypeRef getType() {
		return type;
	}

	/**
	 * @param type The new type of this field.
	 */
	public void setType(TypeRef type) {
		this.type = type;
	}

	/**
	 * This {@link CodeBlock} is run before static initializers, and returns the initial value of this field.
	 * 
	 * @return The initial value of this field.
	 */
	public CodeBlock getValue() {
		return value;
	}

	/**
	 * This {@link CodeBlock} is run before static initializers, and returns the initial value of this field.
	 * 
	 * @return The new initial value of this field.
	 */
	public void setValue(CodeBlock value) {
		this.value = value;
	}
	
	/**
	 * Sets the raw ANTLR data for this field.
	 * 
	 * @param rawType The ANTLR rule representing the type of this field.
	 * @param rawValue The ANTLR rule representing the initial value of this field.
	 */
	public void setRawData(TypeContext rawType, ExprContext rawValue) {
		super.setRawData();
		this.rawType = rawType;
		this.rawValue = rawValue;
	}

	/**
	 * @return The ANTLR rule representing the type of this field.
	 */
	public TypeContext getRawType() {
		return rawType;
	}

	/**
	 * @return The ANTLR rule representing the initial value of this field.
	 */
	public ExprContext getRawValue() {
		return rawValue;
	}
	
	/**
	 * The package this field belongs to.
	 */
	private Package parent;

	/**
	 * @return The package this field belongs to.
	 */
	public Package getParent() {
		return parent;
	}

	/**
	 * NOTE: Don't call this, call <tt>{@link Package}.addField()</tt> instead.
	 * 
	 * @param parent The new package this field belongs to.
	 */
	public void setParent(Package parent) {
		this.parent = parent;
	}
	
	@Override
	public List<MemberAccess> getMembers(Map<TemplateType, TypeRef> templateMap) {
		if (type == null) {
			return Arrays.asList();
		}
		
		return TemplateUtils.replaceTemplates(getType(), templateMap).getMembers(templateMap);
	}

	@Override
	public MemberAccess getMemberParent() {
		return getParent();
	}
	
	/**
	 * The getter function for this field.
	 */
	private Function getter;
	
	/**
	 * The library function that is the default getter function for this field.
	 */
	private Function defaultGetter;
	
	/**
	 * False if this field is write-only.
	 */
	private boolean hasGetter = true;
	
	/**
	 * @return The getter function for this field. May be null if this field is write-only.
	 */
	public Function getGetter() {
		if (!hasGetter) return null;
		
		return getter == null ? defaultGetter() : getter;
	}
	
	/**
	 * @param f The getter function for this field. May be null if this field is write-only.
	 */
	public void setGetter(Function f) {
		getter = f;
		hasGetter = (f != null);
	}
	
	/**
	 * @return The library function that is the default getter function for this field.
	 */
	public Function defaultGetter() {
		if (defaultGetter == null) {
			// make the default getter function
			defaultGetter = new Function(tni, getName(), new TemplateType[0], new Parameter[0], new TypeRef[] {type});
		}
		
		return defaultGetter;
	}
	
	/**
	 * The setter function for this field.
	 */
	private Function setter;
	
	/**
	 * The library function that is the default setter function for this field.
	 */
	private Function defaultSetter;
	
	/**
	 * False if this field is read-only.
	 */
	private boolean hasSetter = true;
	
	/**
	 * @return The setter function for this field. May be null if this field is read-only.
	 */
	public Function getSetter() {
		if (!hasSetter) return null;
		
		return setter == null ? defaultGetter() : setter;
	}
	
	/**
	 * @param f The setter function for this field. May be null if this field is read-only.
	 */
	public void setSetter(Function f) {
		setter = f;
		hasSetter = (f != null);
	}
	
	/**
	 * @return The library function that is the default setter function for this field.
	 */
	public Function defaultSetter() {
		if (defaultSetter == null) {
			// make the default setter function
			defaultSetter = new Function(tni, getName(), new TemplateType[0], new Parameter[] {new Parameter(tni, name, type, false)}, new TypeRef[0]);
		}
		
		return defaultSetter;
	}
	
	/**
	 * @return If this is an instance field: The type this field is part of. If this is a static field: Null.
	 */
	public Type getFieldOf() {
		MemberAccess access = this;
		while (access != null) {
			if (access instanceof Type) {
				return (Type) access;
			}
			access = access.getMemberParent();
		}
		return null;
	}
}
