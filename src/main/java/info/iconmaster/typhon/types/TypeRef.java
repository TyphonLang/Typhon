package info.iconmaster.typhon.types;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import info.iconmaster.typhon.TyphonInput;
import info.iconmaster.typhon.model.MemberAccess;
import info.iconmaster.typhon.model.TemplateArgument;
import info.iconmaster.typhon.model.TyphonModelEntity;
import info.iconmaster.typhon.util.SourceInfo;

/**
 * This represents a reference to a type, possibly with templates instantiated.
 * 
 * @author iconmaster
 *
 */
public class TypeRef extends TyphonModelEntity implements MemberAccess {
	/**
	 * The type definition of this reference.
	 */
	private Type type;
	
	/**
	 * The templates this type has been instantiated with.
	 * Must be empty if this type does not support templates.
	 */
	private List<TemplateArgument> templateArgs = new ArrayList<>();
	
	/**
	 * True if this was declared with 'var'.
	 */
	private boolean isVar;
	
	/**
	 * True if this was declared with 'const'.
	 */
	private boolean isConst;

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
	
	@Override
	public List<MemberAccess> getMembers() {
		if (type == null) {
			return Arrays.asList();
		}
		
		return type.getMembers();
	}
	
	@Override
	public String getName() {
		if (type == null) {
			return null;
		}
		
		return type.getName();
	}

	/**
	 * @return True if this was declared with 'var'.
	 */
	public boolean isVar() {
		return isVar;
	}

	/**
	 * @param isVar True if this was declared with 'var'.
	 */
	public void isVar(boolean isVar) {
		this.isVar = isVar;
	}

	/**
	 * @return True if this was declared with 'const'.
	 */
	public boolean isConst() {
		return isConst;
	}

	/**
	 * @param isConst True if this was declared with 'const'.
	 */
	public void isConst(boolean isConst) {
		this.isConst = isConst;
	}
}
