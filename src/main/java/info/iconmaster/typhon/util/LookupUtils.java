package info.iconmaster.typhon.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import info.iconmaster.typhon.compiler.Instruction;
import info.iconmaster.typhon.compiler.Instruction.OpCode;
import info.iconmaster.typhon.compiler.Scope;
import info.iconmaster.typhon.compiler.Variable;
import info.iconmaster.typhon.errors.WriteOnlyError;
import info.iconmaster.typhon.model.Field;
import info.iconmaster.typhon.model.Function;
import info.iconmaster.typhon.model.MemberAccess;
import info.iconmaster.typhon.model.Parameter;
import info.iconmaster.typhon.model.TemplateArgument;
import info.iconmaster.typhon.types.TemplateType;
import info.iconmaster.typhon.types.Type;
import info.iconmaster.typhon.types.TypeRef;

public class LookupUtils {
	private LookupUtils() {}
	
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
					Map<TemplateType, TypeRef> map = new HashMap<>();
					
					int i = 0;
					for (MemberAccess member : members) {
						if (i == members.size()-1) break;
						Map<TemplateType, TypeRef> newMap = member.getTemplateMap(map);
						if (newMap != null) {
							map = newMap;
						}
						i++;
					}
					
					MemberAccess lastMember = members.get(members.size()-1);
					List<MemberAccess> matches = lastMember.getMembers(name.name, map);
					
					for (MemberAccess match : matches) {
						List<MemberAccess> newMembers = new ArrayList<>(members);
						
						if (match instanceof Type) {
							TypeRef ref = new TypeRef(name.source, (Type) match);
							ref.getTemplateArgs().addAll(name.template);
							match = ref;
						}
						
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
			Map<TemplateType, TypeRef> map = new HashMap<>();
			
			for (MemberAccess access : path) {
				if (access instanceof Variable) {
					type = TemplateUtils.replaceTemplates(((Variable) access).type, map);
				}
				
				if (access instanceof Field) {
					Field f = (Field) access;
					Type fieldOf = f.getFieldOf();
					
					if (fieldOf != null && (type == null || !type.canCastTo(TemplateUtils.replaceTemplates(new TypeRef(null, fieldOf), map)))) {
						return true;
					}
					
					type = TemplateUtils.replaceTemplates(f.getType(), map);
				}
				
				Map<TemplateType, TypeRef> newMap = access.getTemplateMap(map);
				if (newMap != null) {
					map = newMap;
				}
			}
			
			return false;
		});
		
		return result;
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
	
	public static TypeRef getTypeOfPath(Scope scope, List<MemberAccess> path) {
		TypeRef var = scope.getCodeBlock().instance == null ? null : scope.getCodeBlock().instance.type;
		
		for (MemberAccess access : path) {
			if (access instanceof Variable) {
				var = ((Variable) access).type;
			}
			
			if (access instanceof Field) {
				Field f = (Field) access;
				
				var = f.getType();
			}
		}
		
		return var;
	}
	
	public static class LookupArgument {
		public Variable var;
		public String label;
		
		public LookupArgument(Variable var) {
			super();
			this.var = var;
		}
		
		public LookupArgument(Variable var, String label) {
			super();
			this.var = var;
			this.label = label;
		}
	}
	
	public static Map<Parameter, Variable> getFuncArgMap(Function f, List<LookupArgument> args) {
		Map<Parameter, Variable> result = new HashMap<>();
		
		// populate map
		for (LookupArgument arg : args) {
			if (arg.label == null) {
				// positional argument; find the first unallocated parameter
				boolean found = false;
				for (Parameter param : f.getParams()) {
					if (!result.containsKey(param)) {
						result.put(param, arg.var);
						found = true;
						break;
					}
				}
				
				if (!found) {
					// error; too many arguments
					return null;
				}
			} else {
				// keyword argument; find the corresponding parameter
				boolean found = false;
				for (Parameter param : f.getParams()) {
					if (param.getName().equals(arg.label)) {
						if (result.containsKey(param)) {
							// error; duplicate argument
							return null;
						}
						
						result.put(param, arg.var);
						found = true;
						break;
					}
				}
				
				if (!found) {
					// error; too many arguments
					return null;
				}
			}
		}
		
		// check to make sure no required parameters are left out
		for (Parameter param : f.getParams()) {
			if (!param.isOptional() && !result.containsKey(param)) {
				// error; parameter required
				return null;
			}
		}
		
		return result;
	}
}
