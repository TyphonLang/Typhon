package info.iconmaster.typhon.model;

import info.iconmaster.typhon.TyphonInput;
import info.iconmaster.typhon.antlr.TyphonParser.ExprContext;
import info.iconmaster.typhon.antlr.TyphonParser.TypeContext;
import info.iconmaster.typhon.compiler.CodeBlock;
import info.iconmaster.typhon.compiler.Variable;
import info.iconmaster.typhon.types.Type;
import info.iconmaster.typhon.types.TypeRef;
import info.iconmaster.typhon.util.SourceInfo;

public class Parameter extends TyphonModelEntity {
	/**
	 * The name of this parameter. Must be a valid Typhon identifier.
	 */
	private String name;
	
	/**
	 * The type of this parameter.
	 */
	private TypeRef type;
	
	/**
	 * The code to execute when an argument is not supplied by a caller.
	 */
	private CodeBlock defaultValue;
	
	/**
	 * The ANTLR rule representing the type of this parameter.
	 */
	private TypeContext rawType;
	
	/**
	 * The ANTLR rule representing the default value of this parameter.
	 */
	private ExprContext rawDefaultValue;
	
	public Parameter(TyphonInput input) {
		super(input);
	}

	public Parameter(TyphonInput input, SourceInfo source) {
		super(input, source);
	}

	/**
	 * @return The name of this parameter.
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param The new name of this parameter. Must be a valid Typhon identifier.
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return The type of this parameter.
	 */
	public TypeRef getType() {
		return type;
	}

	/**
	 * @param type The new type of this parameter.
	 */
	public void setType(TypeRef type) {
		this.type = type;
	}

	/**
	 * @return The code to execute when an argument is not supplied by a caller.
	 */
	public CodeBlock getDefaultValue() {
		return defaultValue;
	}

	/**
	 * @return The new code to execute when an argument is not supplied by a caller.
	 */
	public void setDefaultValue(CodeBlock defaultValue) {
		this.defaultValue = defaultValue;
	}

	/**
	 * @return The ANTLR rule representing the type of this parameter.
	 */
	public TypeContext getRawType() {
		return rawType;
	}

	/**
	 * @return The ANTLR rule representing the default value of this parameter.
	 */
	public ExprContext getRawDefaultValue() {
		return rawDefaultValue;
	}
	
	/**
	 * Sets the raw ANTLR data for this parameter.
	 * 
	 * @param rawType The ANTLR rule representing the type of this parameter.
	 * @param rawDefaultValue The ANTLR rule representing the default value of this parameter.
	 */
	public void setRawData(TypeContext rawType, ExprContext rawDefaultValue) {
		super.setRawData();
		this.rawType = rawType;
		this.rawDefaultValue = rawDefaultValue;
	}
	
	/**
	 * If this is a library function, optionalOverride determines this parameter's status as optional.
	 */
	private boolean optionalOverride;
	
	/**
	 * Constructs a parameter for a library function.
	 * 
	 * @param tni
	 * @param name
	 * @param type
	 * @param optional
	 */
	public Parameter(TyphonInput tni, String name, TypeRef type, boolean optional) {
		super(tni);
		
		setName(name);
		setType(type);
		optionalOverride = optional;
		
		markAsLibrary();
	}
	
	/**
	 * Constructs a parameter for a library function.
	 * 
	 * @param tni
	 * @param name
	 * @param type
	 * @param optional
	 */
	public Parameter(TyphonInput tni, String name, Type type, boolean optional) {
		super(tni);
		
		setName(name);
		setType(new TypeRef(type));
		optionalOverride = optional;
		
		markAsLibrary();
	}
	
	/**
	 * @return If this argument can be omitted; that is, if it has a default value.
	 */
	public boolean isOptional() {
		return rawDefaultValue != null || defaultValue != null || (isLibrary() && optionalOverride);
	}
	
	@Override
	public String toString() {
		return type+" "+name;
	}
	
	/**
	 * The Variable this parameter represents. Will be null if compilation has not yet occured.
	 */
	private Variable var;

	/**
	 * @return The Variable this parameter represents. Will be null if compilation has not yet occured.
	 */
	public Variable getVar() {
		return var;
	}

	/**
	 * @param var The Variable this parameter represents.
	 */
	public void setVar(Variable var) {
		this.var = var;
	}
	
}
