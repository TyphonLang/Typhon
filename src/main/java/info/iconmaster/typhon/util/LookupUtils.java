package info.iconmaster.typhon.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import info.iconmaster.typhon.compiler.Instruction;
import info.iconmaster.typhon.compiler.Instruction.OpCode;
import info.iconmaster.typhon.compiler.Label;
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
import info.iconmaster.typhon.util.LookupUtils.LookupElement.AccessType;
import info.iconmaster.typhon.util.LookupUtils.LookupPath.Subject;

public class LookupUtils {
	private LookupUtils() {}
	
	public static class LookupElement {
		String name;
		List<TemplateArgument> template = new ArrayList<>();
		SourceInfo source;
		AccessType prefix;
		
		public static enum AccessType {
			DOT,
			NULLABLE_DOT,
			DOUBLE_DOT;
			
			public static AccessType get(String s) {
				switch (s) {
				case ".":
					return DOT;
				case "?.":
					return NULLABLE_DOT;
				case "..":
					return DOUBLE_DOT;
				default:
					throw new IllegalArgumentException("Unknown access type: "+s);
				}
			}
		}
		
		public LookupElement(String name, SourceInfo source, AccessType prefix) {
			this.name = name;
			this.source = source;
			this.prefix = prefix;
		}
		
		public LookupElement(String name, SourceInfo source, AccessType prefix, List<TemplateArgument> template) {
			this.name = name;
			this.template.addAll(template);
			this.source = source;
			this.prefix = prefix;
		}
		
		public LookupElement(String name, SourceInfo source, AccessType prefix, TemplateArgument... template) {
			this.name = name;
			this.template.addAll(Arrays.asList(template));
			this.source = source;
			this.prefix = prefix;
		}
		
		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder("LookupElement(");
			sb.append(name);
			if (!template.isEmpty()) {
				sb.append('<');
				for (TemplateArgument arg : template) {
					sb.append(arg);
					sb.append(',');
				}
				sb.deleteCharAt(sb.length()-1);
				sb.append('>');
			}
			sb.append(':');
			sb.append(prefix);
			sb.append(')');
			return sb.toString();
		}
	}
	
	public static class LookupPath {
		public static class Subject {
			public int loc;
			public Subject previous;
			public MemberAccess member;
			public AccessType infix;
			public SourceInfo source;
			
			public TypeRef type;
			public Map<TemplateType, TypeRef> typeMap = new HashMap<>();
			
			public Subject(int loc, MemberAccess member) {
				this.loc = loc;
				this.member = member;
			}
			
			@Override
			public String toString() {
				return "Subject("+member+")";
			}
		}
		
		public List<LookupElement> names;
		public List<MemberAccess> members = new ArrayList<>();
		public List<Subject> subjects = new ArrayList<>();
		
		public List<Map<TemplateType, TypeRef>> typeMaps = new ArrayList<>();
		
		public LookupPath(List<LookupElement> names) {
			this.names = new ArrayList<>();
			this.names.add(null);
			this.names.addAll(names);
		}
		
		public LookupPath(LookupPath other) {
			this.names = other.names;
			this.members = new ArrayList<>(other.members);
			this.subjects = new ArrayList<>(other.subjects);
			this.typeMaps = new ArrayList<>(other.typeMaps);
		}
		
		public LookupPath add(MemberAccess member) {
			Map<TemplateType, TypeRef> typeMap = lastTypeMap();
			LookupElement name = names.get(members.size());
			
			if (member instanceof Type) {
				TypeRef ref = new TypeRef(name == null ? null : name.source, (Type)member);
				if (name != null) ref.getTemplateArgs().addAll(name.template);
				member = TemplateUtils.replaceTemplates(ref, typeMap);
			}
			
			Map<TemplateType, TypeRef> newMap = member.getTemplateMap(typeMap);
			this.members.add(member);
			
			if (member instanceof Field || member instanceof Variable || member instanceof Function) {
				Subject sub = new Subject(members.size()-1, member);
				if (name != null) sub.source = name.source;
				
				if (!subjects.isEmpty()) {
					sub.previous = lastSubject();
					sub.infix = names.get(subjects.get(subjects.size()-1).loc+1).prefix;
				}
				
				TypeRef type = null;
				
				if (member instanceof Variable) {
					type = TemplateUtils.replaceTemplates(((Variable) member).type, typeMap);
				}
				
				if (member instanceof Field) {
					Field f = (Field) member;
					type = TemplateUtils.replaceTemplates(f.getType(), typeMap);
				}
				
				sub.type = type;
				if (type != null) {
					Map<TemplateType, TypeRef> subTypeMap = type.getTemplateMap(typeMap);
					sub.typeMap = newMap = subTypeMap == null ? (newMap == null ? typeMap : newMap) : subTypeMap;
				} else {
					sub.typeMap = newMap == null ? typeMap : newMap;
				}
				
				subjects.add(sub);
			}
			
			if (newMap != null) {
				typeMap = newMap;
			}
			typeMaps.add(typeMap);
			
			return this;
		}
		
		public LookupPath addAll(Collection<MemberAccess> members) {
			for (MemberAccess member : members) {
				add(member);
			}
			
			return this;
		}
		
		public boolean isValidPath() {
			if (subjects.isEmpty() || subjects.get(subjects.size()-1).loc != names.size()-1) return false;
			
			for (Subject sub : subjects) {
				if (sub.member instanceof Field) {
					Field f = (Field) sub.member;
					Type fieldOf = f.getFieldOf();
					
					TypeRef type = sub.previous == null ? null : sub.previous.type;
					Map<TemplateType, TypeRef> typeMap = sub.previous == null ? new HashMap<>() : sub.previous.typeMap;
					
					if (fieldOf != null && (type == null || !type.canCastTo(TemplateUtils.replaceTemplates(new TypeRef(null, fieldOf), typeMap)))) {
						return false;
					}
				}
			}
			
			return true;
		}
		
		public List<LookupPath> branch(List<MemberAccess> newMembers) {
			return newMembers.stream().map((member)->new LookupPath(this).add(member)).collect(Collectors.toList());
		}
		
		public MemberAccess lastMember() {
			if (members.isEmpty()) return null;
			
			int i = members.size()-1;
			MemberAccess e = members.get(i);
			Subject sub = subjects.isEmpty() ? null : subjects.get(subjects.size()-1);
			
			while (sub != null && sub.loc == i && sub.infix == AccessType.DOUBLE_DOT) {
				sub = sub.previous;
				i = sub.loc;
				e = members.get(i);
			}
			
			return e;
		}
		
		public Subject lastSubject() {
			if (subjects.isEmpty()) return null;
			
			Subject e = subjects.get(subjects.size()-1);
			
			while (e.infix == AccessType.DOUBLE_DOT) {
				e = e.previous;
			}
			
			return e;
		}
		
		public Map<TemplateType, TypeRef> lastTypeMap() {
			if (typeMaps.isEmpty()) return new HashMap<>();
			
			int i = typeMaps.size()-1;
			Map<TemplateType, TypeRef> e = typeMaps.get(i);
			Subject sub = subjects.isEmpty() ? null : subjects.get(subjects.size()-1);
			
			while (sub != null && sub.loc == i && sub.infix == AccessType.DOUBLE_DOT) {
				sub = sub.previous;
				i = sub.loc;
				e = typeMaps.get(i);
			}
			
			return e;
		}
		
		public Subject popSubject() {
			Subject sub = subjects.remove(subjects.size()-1);
			// TODO: remove non-subject members
			return sub;
		}
		
		@Override
		public String toString() {
			return "path"+members;
		}
		
		public MemberAccess getLookup() {
			Subject sub = lastSubject();
			MemberAccess member = lastMember();
			if (sub == null || sub.type == null || sub.member != member) {
				return member;
			} else {
				return sub.type;
			}
		}
	}
	
	/**
	 * Look up all possible paths of a list of names.
	 * 
	 * @param base The scope.
	 * @param names The list of successive names.
	 * @return All the possible paths of members.
	 */
	public static List<LookupPath> findPaths(Scope scope, MemberAccess base, List<LookupElement> names) {
		List<LookupPath> result = new ArrayList<>();
		
		// populate the list of possible paths (based on name alone)
		while (base != null) {
			List<LookupPath> options = new ArrayList<>();
			options.add(new LookupPath(names).add(base));
			
			for (LookupElement name : names) {
				if (options.isEmpty()) break;
				
				options = options.stream()
						.map((path)->path.branch(path.getLookup().getMembers(name.name, path.lastTypeMap())))
						.reduce(new ArrayList<>(), (a,b)->{a.addAll(b); return a;});
			}
			
			result.addAll(options);
			base = base.getMemberParent();
		}
		
		// remove incorrect accesses to instance fields
		result.removeIf((path)->!path.isValidPath());
		
		// return the result
		return result;
	}
	
	public static Variable getSubjectOfPath(Scope scope, LookupPath path) {
		Variable var = scope.getCodeBlock().instance;
		Label label = null;
		
		for (Subject sub : path.subjects) {
			LookupElement name = path.names.get(sub.loc);
			SourceInfo source = name == null ? null : name.source;
			Variable lastVar = var;
			
			if (sub.infix == AccessType.NULLABLE_DOT) {
				if (var == null) {
					// TODO: error?
					return null;
				}
				
				if (label == null) {
					label = scope.addTempLabel();
				}
				
				Variable tempVar = scope.addTempVar(new TypeRef(scope.getCodeBlock().tni.corePackage.TYPE_BOOL), name.source);
				scope.getCodeBlock().ops.add(new Instruction(scope.getCodeBlock().tni, source, OpCode.ISNULL, new Object[] {tempVar, var}));
				scope.getCodeBlock().ops.add(new Instruction(scope.getCodeBlock().tni, source, OpCode.JUMPIF, new Object[] {tempVar, label}));
			}
			
			if (sub.member instanceof Variable) {
				var = (Variable) sub.member;
			}
			
			if (sub.member instanceof Field) {
				Field f = (Field) sub.member;
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
			
			if (sub.infix == AccessType.DOUBLE_DOT) {
				if (var == null) {
					// TODO: error?
					return null;
				}
				
				var = lastVar;
			}
		}
		
		if (label != null) {
			scope.getCodeBlock().ops.add(new Instruction(scope.getCodeBlock().tni, null, OpCode.LABEL, new Object[] {label}));
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
