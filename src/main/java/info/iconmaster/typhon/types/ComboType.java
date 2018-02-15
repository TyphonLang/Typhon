package info.iconmaster.typhon.types;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import info.iconmaster.typhon.TyphonInput;
import info.iconmaster.typhon.model.MemberAccess;
import info.iconmaster.typhon.util.SourceInfo;
import info.iconmaster.typhon.util.TemplateUtils;

public class ComboType extends ExtendableType {
	public ComboType(TyphonInput input, SourceInfo source) {
		super(input, source);
	}

	public ComboType(TyphonInput input) {
		super(input);
	}
	
	public ComboType(TyphonInput input, Type... types) {
		super(input);
		
		getParentTypes().addAll(Arrays.asList(types).stream().map(t->new TypeRef(t)).collect(Collectors.toList()));
	}
	
	public ComboType(TyphonInput input, TypeRef... types) {
		super(input);
		
		getParentTypes().addAll(Arrays.asList(types));
	}
	
	@Override
	public boolean equals(Object o) {
		if (!(o instanceof ComboType)) return false;
		ComboType other = (ComboType) o;
		
		if (getParentTypes().size() != other.getParentTypes().size()) return false;
		
		int i = 0;
		for (TypeRef type : getParentTypes()) {
			if (!type.equals(other.getParentTypes().get(i))) return false;
			i++;
		}
		
		return true;
	}
	
	@Override
	public List<MemberAccess> getMembers(Map<TemplateType, TypeRef> templateMap) {
		return getParentTypes().stream().flatMap(t->t.getMembers(templateMap).stream()).collect(Collectors.toList());
	}
	
	@Override
	public Map<TemplateType, TypeRef> getTemplateMap(Map<TemplateType, TypeRef> templateMap) {
		return getParentTypes().stream().map(t->t.getTemplateMap(templateMap)).reduce(new HashMap<>(), (a,b)->{
			a.putAll(b);
			return a;
		});
	}
	
	@Override
	public boolean canCastTo(TypeRef a, TypeRef b) {
		for (TypeRef type : getParentTypes()) {
			if (type.canCastTo(b)) {
				return true;
			}
		}
		
		return super.canCastTo(a, b);
	}
	
	@Override
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
		List<TypeRef> commons = new ArrayList<>();
		for (TypeRef parent : ((ComboType)a.getType()).getParentTypes()) {
			TypeRef trueParent = TemplateUtils.replaceTemplates(parent, TemplateUtils.matchAllTemplateArgs(a));
			
			if (trueParent.equals(b)) {
				return trueParent;
			}
			
			commons.add(trueParent.commonType(b));
		}
		
		List<TypeRef> commons2 = commons.stream().filter(t1->
			commons.stream().allMatch(t2->(t1 == t2 || !t1.canCastTo(t2)))
		).collect(Collectors.toList());
		
		if (commons2.isEmpty()) {
			return super.commonType(a, b);
		} else if (commons2.size() == 1) {
			return commons2.get(0);
		} else {
			ComboType combo = new ComboType(tni);
			combo.getParentTypes().addAll(commons2);
			return new TypeRef(combo);
		}
	}
	
	@Override
	public String prettyPrint() {
		StringBuilder sb = new StringBuilder();
		
		sb.append('(');
		if (!getParentTypes().isEmpty()) {
			for (TypeRef arg : getParentTypes()) {
				sb.append(arg.prettyPrint());
				sb.append(" && ");
			}
			
			sb.deleteCharAt(sb.length()-1);
			sb.deleteCharAt(sb.length()-1);
			sb.deleteCharAt(sb.length()-1);
			sb.deleteCharAt(sb.length()-1);
		}
		sb.append(')');
		
		return sb.toString();
	}
}
