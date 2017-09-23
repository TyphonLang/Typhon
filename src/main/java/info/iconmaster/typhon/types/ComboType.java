package info.iconmaster.typhon.types;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import info.iconmaster.typhon.TyphonInput;
import info.iconmaster.typhon.model.MemberAccess;
import info.iconmaster.typhon.util.SourceInfo;

public class ComboType extends Type {
	/**
	 * The list of types that this type must conform to.
	 */
	private List<TypeRef> types = new ArrayList<>();
	
	public ComboType(TyphonInput input, SourceInfo source) {
		super(input, source);
	}

	public ComboType(TyphonInput input) {
		super(input);
	}

	/**
	 * @return The list of types that this type must conform to.
	 */
	public List<TypeRef> getTypes() {
		return types;
	}
	
	@Override
	public boolean equals(Object o) {
		if (!(o instanceof ComboType)) return false;
		ComboType other = (ComboType) o;
		
		if (getTypes().size() != other.getTypes().size()) return false;
		
		int i = 0;
		for (TypeRef type : getTypes()) {
			if (!type.equals(other.getTypes().get(i))) return false;
			i++;
		}
		
		return true;
	}
	
	@Override
	public List<MemberAccess> getMembers(Map<TemplateType, TypeRef> templateMap) {
		return getTypes().stream().flatMap(t->t.getMembers(templateMap).stream()).collect(Collectors.toList());
	}
	
	@Override
	public Map<TemplateType, TypeRef> getTemplateMap(Map<TemplateType, TypeRef> templateMap) {
		return getTypes().stream().map(t->t.getTemplateMap(templateMap)).reduce(new HashMap<>(), (a,b)->{
			a.putAll(b);
			return a;
		});
	}
	
	@Override
	public boolean canCastTo(TypeRef a, TypeRef b) {
		for (TypeRef type : getTypes()) {
			if (type.canCastTo(b)) {
				return true;
			}
		}
		
		return super.canCastTo(a, b);
	}
}
