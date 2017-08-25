package info.iconmaster.typhon.types;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

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
	
	public TypeRef(SourceInfo source, Type type) {
		super(type.tni, source);
		this.type = type;
	}
	
	public TypeRef(Type type, TemplateArgument... args) {
		super(type.tni, type.source);
		this.type = type;
		getTemplateArgs().addAll(Arrays.asList(args));
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
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((templateArgs == null) ? 0 : templateArgs.hashCode());
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		return result;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		TypeRef other = (TypeRef) obj;
		
		Map<TemplateType, TypeRef> map1 = TemplateUtils.matchTemplateArgs(this, getType().getMemberTemplate(), getTemplateArgs());
		Map<TemplateType, TypeRef> map2 = TemplateUtils.matchTemplateArgs(this, other.getType().getMemberTemplate(), getTemplateArgs());
		if (!map1.equals(map2)) {
			return false;
		}
		
		if (type == null) {
			if (other.type != null)
				return false;
		} else if (!type.equals(other.type))
			return false;
		return true;
	}

	/**
	 * Returns true if this type can be safely converted to the specified type.
	 * If a.canCastTo(b), then a is a subtype of b.
	 * 
	 * @param other The type to check.
	 * @return True if this type can be safely converted to the specified type.
	 */
	public boolean canCastTo(TypeRef other) {
		return this.type.canCastTo(this, other);
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("ref(");
		sb.append(getType().toString());
		sb.append(')');
		if (!getTemplateArgs().isEmpty()) {
			sb.append('<');
			for (TemplateArgument arg : getTemplateArgs()) {
				sb.append(arg);
				sb.append(',');
			}
			sb.deleteCharAt(sb.length()-1);
			sb.append('>');
		}
		return sb.toString();
	}
	
	@Override
	public MemberAccess getMemberParent() {
		return type.getParent();
	}
	
	/**
	 * @return A copy of this type.
	 */
	public TypeRef copy() {
		TypeRef t = new TypeRef(tni, source);
		t.setType(type);
		t.isVar(isVar);
		t.isConst(isConst);
		for (TemplateArgument arg : getTemplateArgs()) {
			t.getTemplateArgs().add(arg.copy());
		}
		return t;
	}
	
	/**
	 * @param tni
	 * @return The typeref universally understood as 'var'.
	 */
	public static TypeRef var(TyphonInput tni) {
		TypeRef t = new TypeRef(tni);
		t.setType(tni.corePackage.TYPE_ANY);
		t.isVar(true);
		return t;
	}
}
