package info.iconmaster.typhon.types;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Stack;

import info.iconmaster.typhon.TyphonInput;
import info.iconmaster.typhon.model.AnnotationDefinition;
import info.iconmaster.typhon.model.Field;
import info.iconmaster.typhon.model.Function;
import info.iconmaster.typhon.model.MemberAccess;
import info.iconmaster.typhon.model.Package;
import info.iconmaster.typhon.model.TyphonModelEntity;
import info.iconmaster.typhon.util.SourceInfo;
import info.iconmaster.typhon.util.TemplateUtils;

/**
 * This is a type in Typhon's type system.
 * 
 * @author iconmaster
 *
 */
public abstract class Type extends TyphonModelEntity implements MemberAccess {
	/**
	 * The package that contains methods, fields, etc. for this type.
	 */
	protected Package typePackage;
	
	public Type(TyphonInput input) {
		super(input);
	}
	
	public Type(TyphonInput input, SourceInfo source) {
		super(input, source);
	}
	
	/**
	 * Returns the identifying name for this type.
	 * This string is used to look up this type from a package, among other thing.
	 * May be null; if null, this type cannot be placed in a package.
	 * 
	 * @return The name of this type.
	 */
	public String getName() {
		return null;
	}

	/**
	 * @return The package that contains methods, fields, etc. for this type.
	 */
	public Package getTypePackage() {
		if (typePackage == null) {
			typePackage = new Package(source, null, parent == null ? tni.corePackage : parent) {
				@Override
				public MemberAccess getMemberParent() {
					return Type.this;
				}
			};
		}
		
		return typePackage;
	}
	
	/**
	 * The package this type belongs to.
	 */
	private Package parent;

	/**
	 * @return The package this type belongs to.
	 */
	public Package getParent() {
		return parent;
	}
	
	@Override
	public MemberAccess getMemberParent() {
		return getParent();
	}

	/**
	 * NOTE: Don't call this, call <tt>{@link Package}.addType()</tt> instead.
	 * 
	 * @param parent The new package this type belongs to.
	 */
	public void setParent(Package parent) {
		this.parent = parent;
		getTypePackage().setParent(parent);
	}
	
	@Override
	public void markAsLibrary() {
		super.markAsLibrary();
		
		getTypePackage().markAsLibrary();
	}
	
	@Override
	public List<MemberAccess> getMembers(Map<TemplateType, TypeRef> templateMap) {
		return getTypePackage().getMembers(templateMap);
	}
	
	/**
	 * This is called by {@link TypeRef} to check for casting.
	 * Returns true if <tt>a</tt> can be safely converted to <tt>b</tt>.
	 * If canCastTo(a, b), then a is a subtype of b.
	 * 
	 * @param a A type. <tt>a.getType()</tt> must be equal to <tt>this</tt>.
	 * @param b Another type.
	 * @return True if <tt>a</tt> can be safely converted to <tt>b</tt>.
	 */
	public boolean canCastTo(TypeRef a, TypeRef b) {
		if (b.getType() instanceof TemplateType) {
			return a.canCastTo(((TemplateType)b.getType()).getBaseType());
		}
		
		if (b.getType() instanceof ComboType) {
			for (TypeRef type : ((ComboType)b.getType()).getParentTypes()) {
				if (!a.canCastTo(type)) {
					return false;
				}
			}
			
			return true;
		}
		
		// compare templates
		Map<TemplateType, TypeRef> map1 = TemplateUtils.matchAllTemplateArgs(a);
		Map<TemplateType, TypeRef> map2 = TemplateUtils.matchAllTemplateArgs(b);
		
		for (Entry<TemplateType, TypeRef> entry : map1.entrySet()) {
			if (!map2.containsKey(entry.getKey())) return false;
		}
		
		for (Entry<TemplateType, TypeRef> entry : map2.entrySet()) {
			if (!map1.containsKey(entry.getKey())) return false;
		}
		
		for (Entry<TemplateType, TypeRef> entry : map1.entrySet()) {
			TypeRef value1 = entry.getValue();
			TypeRef value2 = map2.get(entry.getKey());
			
			if (!value1.canCastTo(value2)) {
				return false;
			}
		}
		
		// compare types
		return a.getType().equals(b.getType());
	}
	
	/**
	 * @param op The operator to look for.
	 * @return All the instance functions that are annotated as operator handlers for this type.
	 */
	public List<Function> getOperatorHandlers(AnnotationDefinition op) {
		List<MemberAccess> members = getMembers(new HashMap<>());
		List<Function> result = new ArrayList<>();
		
		while (!members.isEmpty()) {
			MemberAccess member = members.remove(0);
			
			if (member instanceof Function) {
				Function f = (Function) member;
				if (this.canCastTo(new TypeRef(this), new TypeRef(f.getFieldOf())) && f.hasAnnot(op)) {
					result.add(f);
				}
 			} else {
				members.addAll(member.getMembers(new HashMap<>()));
			}
		}
		
		return result;
	}
	
	/**
	 * This is called by {@link TypeRef} to find a common ancestor type.
	 * 
	 * @param a A type. <tt>a.getType()</tt> must be equal to <tt>this</tt>.
	 * @param b Another type.
	 * @return The closest common ancestor type. May be a ComboType in case of parent ambiguity. May not be null.
	 */
	public TypeRef commonType(TypeRef a, TypeRef b) {
		// test if the types are direct suptypes of one another
		if (a.equals(b)) {
			return a.copy();
		}
		
		if (b.canCastTo(a) && !a.canCastTo(b)) {
			return a.copy();
		}
		
		if (a.canCastTo(b) && !b.canCastTo(a)) {
			return b.copy();
		}
		
		// the only possibility left: Neither a nor b can cast to each other directly
		if (b.getType() instanceof TemplateType) {
			return a.commonType(((TemplateType)b.getType()).getBaseType());
		}
		
		return new TypeRef(tni.corePackage.TYPE_ANY);
	}
	
	@Override
	public String prettyPrint() {
		StringBuilder sb = new StringBuilder();
		
		String path = getPathString();
		if (!path.isEmpty()) {
			sb.append(path);
			sb.append('.');
		}
		sb.append(getName());
		
		return sb.toString();
	}
	
	public Set<Field> getAllFields() {
		Set<Field> result = new HashSet<>();
		
		Stack<MemberAccess> mems = new Stack<>();
		mems.add(this);
		
		while (!mems.isEmpty()) {
			MemberAccess mem = mems.pop();
			
			if (mem instanceof Field && ((Field)mem).getFieldOf() != null && new TypeRef(this).canCastTo(new TypeRef((((Field)mem).getFieldOf())))) {
				result.add(((Field)mem));
			} else if (mem instanceof Package || mem instanceof TypeRef || mem instanceof Type) {
				for (MemberAccess child : mem.getMembers(new HashMap<>())) {
					mems.push(child);
				}
			}
		}
		
		return result;
	}
	
	public Set<Function> getAllMethods(boolean includeVirtualBases) {
		Set<Function> result = new HashSet<>();
		
		Stack<MemberAccess> mems = new Stack<>();
		mems.add(this);
		
		while (!mems.isEmpty()) {
			MemberAccess mem = mems.pop();
			
			if (mem instanceof Function && ((Function)mem).getFieldOf() != null && new TypeRef(this).canCastTo(new TypeRef(((Function)mem).getFieldOf()))) {
				if (includeVirtualBases) {
					result.add(((Function)mem));
				} else {
					result.add(((Function)mem).getVirtualOverride(this));
				}
			} else if (mem instanceof Package || mem instanceof TypeRef || mem instanceof Type) {
				for (MemberAccess child : mem.getMembers(new HashMap<>())) {
					mems.push(child);
				}
			}
		}
		
		return result;
	}
}
