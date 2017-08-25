package info.iconmaster.typhon.compiler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import info.iconmaster.typhon.compiler.Instruction.OpCode;
import info.iconmaster.typhon.errors.WriteOnlyError;
import info.iconmaster.typhon.model.Field;
import info.iconmaster.typhon.model.MemberAccess;
import info.iconmaster.typhon.model.TemplateArgument;
import info.iconmaster.typhon.types.Type;
import info.iconmaster.typhon.types.TypeRef;
import info.iconmaster.typhon.util.SourceInfo;

public class LookupUtils {
	private LookupUtils() {}

	/**
	 * Look up all possible paths of a list of names.
	 * 
	 * @param base The scope.
	 * @param names The list of successive names.
	 * @return All the possible paths of members.
	 */
	public static List<List<MemberAccess>> findPaths(Scope scope, MemberAccess base, List<LookupElement> names) {
		List<List<MemberAccess>> result = new ArrayList<>();
		
		// populate the list of possible paths (based on name alone)
		while (base != null) {
			List<List<MemberAccess>> options = new ArrayList<>();
			final MemberAccess finalBase = base;
			options.add(new ArrayList<MemberAccess>() {{add(finalBase);}});
			
			for (LookupElement name : names) {
				List<List<MemberAccess>> newOptions = new ArrayList<>();
				
				for (List<MemberAccess> members : options) {
					MemberAccess lastMember = members.get(members.size()-1);
					List<MemberAccess> matches = lastMember.getMembers(name.name);
					
					for (MemberAccess match : matches) {
						List<MemberAccess> newMembers = new ArrayList<>(members);
						newMembers.add(match);
						newOptions.add(newMembers);
					}
				}
				
				options = newOptions;
			}
			
			result.addAll(options);
			base = base.getMemberParent();
		}
		
		// remove incorrect accesses to instance fields
		result.removeIf((path)->{
			TypeRef type = scope.getCodeBlock().instance != null ? scope.getCodeBlock().instance.type : null;
			
			for (MemberAccess access : path) {
				if (access instanceof Variable) {
					type = ((Variable) access).type;
				}
				
				if (access instanceof Field) {
					Field f = (Field) access;
					Type fieldOf = f.getFieldOf();
					
					if (fieldOf != null && (type == null || !fieldOf.equals(type.getType()))) {
						return true;
					}
					
					type = f.type;
				}
			}
			
			return false;
		});
		
		return result;
	}
	
	public static class LookupElement {
		String name;
		List<TemplateArgument> template = new ArrayList<>();
		SourceInfo source;
		
		public LookupElement(String name, SourceInfo source) {
			this.name = name;
			this.source = source;
		}
		
		public LookupElement(String name, SourceInfo source, List<TemplateArgument> template) {
			this.name = name;
			this.template.addAll(template);
			this.source = source;
		}
		
		public LookupElement(String name, SourceInfo source, TemplateArgument... template) {
			this.name = name;
			this.template.addAll(Arrays.asList(template));
			this.source = source;
		}
	}
	
	public static Variable getSubjectOfPath(Scope scope, List<MemberAccess> path, List<LookupElement> names) {
		Variable var = scope.getCodeBlock().instance;
		
		int i = -1;
		for (MemberAccess access : path) {
			SourceInfo source = i < 0 ? null : names.get(i).source;
			
			if (access instanceof Variable) {
				var = (Variable) access;
			}
			
			if (access instanceof Field) {
				Field f = (Field) access;
				Type fieldOf = f.getFieldOf();
				Variable newVar = scope.addTempVar(f.type, source);
				
				if (f.getGetter() == null) {
					// error; field is write-only
					scope.getCodeBlock().tni.errors.add(new WriteOnlyError(source, f));
					return newVar;
				}
				
				if (fieldOf == null) {
					// static
					
					scope.getCodeBlock().ops.add(new Instruction(scope.getCodeBlock().tni, source, OpCode.CALLSTATIC, new Object[] {Arrays.asList(newVar), f.getGetter(), new ArrayList<>()}));
				} else {
					// instance
					
					scope.getCodeBlock().ops.add(new Instruction(scope.getCodeBlock().tni, source, OpCode.CALL, new Object[] {Arrays.asList(newVar), var, f.getGetter(), new ArrayList<>()}));
				}
				
				var = newVar;
			}
			
			i++;
		}
		
		return var;
	}
}
