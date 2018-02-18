package info.iconmaster.typhon.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import info.iconmaster.typhon.antlr.TyphonParser.ExprContext;
import info.iconmaster.typhon.compiler.Instruction;
import info.iconmaster.typhon.compiler.Instruction.OpCode;
import info.iconmaster.typhon.compiler.Label;
import info.iconmaster.typhon.compiler.Scope;
import info.iconmaster.typhon.compiler.TyphonCompiler;
import info.iconmaster.typhon.compiler.Variable;
import info.iconmaster.typhon.errors.WriteOnlyError;
import info.iconmaster.typhon.model.Field;
import info.iconmaster.typhon.model.Function;
import info.iconmaster.typhon.model.MemberAccess;
import info.iconmaster.typhon.model.Parameter;
import info.iconmaster.typhon.model.TemplateArgument;
import info.iconmaster.typhon.model.libs.CorePackage;
import info.iconmaster.typhon.types.TemplateType;
import info.iconmaster.typhon.types.Type;
import info.iconmaster.typhon.types.TypeRef;
import info.iconmaster.typhon.util.LookupUtils.LookupElement.AccessType;
import info.iconmaster.typhon.util.LookupUtils.LookupPath.Subject;

/**
 * This class contains functions suitable for doing lookup for expressions in dot-linked form.
 * @author iconmaster
 *
 */
public class LookupUtils {
	private LookupUtils() {}
	
	/**
	 * A single element in a chain of names to look up.
	 * Contains data such as the source and raw template.
	 * 
	 * @author iconmaster
	 *
	 */
	public static class LookupElement {
		/**
		 * The name that will be looked up.
		 */
		String name;
		
		/**
		 * The template arguments that was supplied with this name, if any.
		 */
		List<TemplateArgument> template = new ArrayList<>();
		
		/**
		 * The source this portion of lookup came from.
		 */
		SourceInfo source;
		
		/**
		 * What connector joins this element to the last one. May be null.
		 */
		AccessType prefix;
		
		/**
		 * The type of connector that was placed between two elements.
		 * 
		 * @author iconmaster
		 *
		 */
		public static enum AccessType {
			/**
			 * Normal access notation, '.'. Standard access of a field, subpackage, etc.
			 */
			DOT,
			/**
			 * Call-chaining notation, '..'. Like '.', but will return the value it accessed instead of what it returns itself.
			 */
			NULLABLE_DOT,
			/**
			 * Nullable notation, '.?'. Like '.', but returns null instead if what it is accessing is null.
			 */
			DOUBLE_DOT;
			
			/**
			 * Creates an AccessType from a String that represents it.
			 * 
			 * @param s
			 * @return
			 */
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
	
	/**
	 * This represents a possible path returned by findPaths().
	 * It consists of a list of members that were found based on the names provided.
	 * 
	 * @author iconmaster
	 *
	 */
	public static class LookupPath {
		
		/**
		 * A Subject of a path is a MemberAccess that requires special action when it is encountered. This includes variables, fields, etc.
		 * 
		 * @author iconmaster
		 *
		 */
		public static class Subject {
			/**
			 * Where it is in the path's member list.
			 */
			public int loc;
			
			/**
			 * What the previous subject was. If this is a field, this is the callee, for example.
			 */
			public Subject previous;
			
			/**
			 * The member this subject represents.
			 */
			public MemberAccess member;
			
			/**
			 * What access operation binds together <tt>previous</tt> and <tt>this</tt>.
			 */
			public AccessType infix;
			
			/**
			 * Where this member originated.
			 */
			public SourceInfo source;
			
			/**
			 * The type this subject returns upon execution.
			 */
			public TypeRef type;
			
			/**
			 * The path this subject is from.
			 */
			public LookupPath path;
			
			/**
			 * The type map this subject returns, suitable for the methods in {@link MemberAccess} or {@link TemplateUtils}.
			 */
			public Map<TemplateType, TypeRef> typeMap = new HashMap<>();
			
			public Subject(LookupPath path, int loc, MemberAccess member) {
				this.path = path;
				this.loc = loc;
				this.member = member;
			}
			
			@Override
			public String toString() {
				return "Subject("+member+")";
			}
		}
		
		/**
		 * The raw input given to this path.
		 */
		public List<LookupElement> names;
		
		/**
		 * The list of consecutive accesses that the names given represent.
		 */
		public List<MemberAccess> members = new ArrayList<>();
		
		/**
		 * The list of subjects that were accessed in this path.
		 */
		public List<Subject> subjects = new ArrayList<>();
		
		/**
		 * The type map at each step of this access path.
		 */
		public List<Map<TemplateType, TypeRef>> typeMaps = new ArrayList<>();
		
		public LookupPath(List<LookupElement> names) {
			this.names = new ArrayList<>();
			this.names.add(null);
			this.names.addAll(names);
		}
		
		/**
		 * Copies another LookupPath.
		 * 
		 * @param other
		 */
		public LookupPath(LookupPath other) {
			this.names = other.names;
			this.members = new ArrayList<>(other.members);
			this.subjects = new ArrayList<>(other.subjects);
			this.typeMaps = new ArrayList<>(other.typeMaps);
		}
		
		/**
		 * Adds a new member to this list. Automatically updates the subject and type map lists.
		 * 
		 * @param member
		 * @return <tt>this</tt>.
		 */
		public LookupPath add(MemberAccess member) {
			Map<TemplateType, TypeRef> typeMap = returnedTypeMap();
			LookupElement name = names.get(members.size());
			
			if (member instanceof Type) {
				TypeRef ref = new TypeRef(name == null ? null : name.source, (Type)member);
				if (name != null) ref.getTemplateArgs().addAll(name.template);
				member = TemplateUtils.replaceTemplates(ref, typeMap);
			}
			
			Map<TemplateType, TypeRef> newMap = member.getTemplateMap(typeMap);
			this.members.add(member);
			
			if (member instanceof Field || member instanceof Variable || member instanceof Function) {
				Subject sub = new Subject(this, members.size()-1, member);
				if (name != null) sub.source = name.source;
				
				if (!subjects.isEmpty()) {
					sub.previous = returnedSubject();
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
		
		/**
		 * Adds new members to this list. Automatically updates the subject and type map lists.
		 * 
		 * @param members
		 * @return <tt>this</tt>.
		 */
		public LookupPath addAll(Collection<MemberAccess> members) {
			for (MemberAccess member : members) {
				add(member);
			}
			
			return this;
		}
		
		/**
		 * @return true if this path does not violate any access rules.
		 */
		public boolean isValidPath() {
			if (subjects.isEmpty() || subjects.get(subjects.size()-1).loc != names.size()-1) return false;
			
			for (Subject sub : subjects) {
				TypeRef type = sub.previous == null ? null : sub.previous.type;
				Map<TemplateType, TypeRef> typeMap = sub.previous == null ? new HashMap<>() : sub.previous.typeMap;
				
				if (sub.member instanceof Field) {
					Field f = (Field) sub.member;
					Type fieldOf = f.getFieldOf();
					
					if (fieldOf == null && type != null) {
						return false;
					}
					
					if (fieldOf != null && (type == null || !type.canCastTo(TemplateUtils.replaceTemplates(new TypeRef(null, fieldOf), typeMap)))) {
						return false;
					}
				} else if (sub.member instanceof Function) {
					Function f = (Function) sub.member;
					Type fieldOf = f.getFieldOf();
					
					if (fieldOf == null && type != null) {
						return false;
					}
					
					if (fieldOf != null && (type == null || !type.canCastTo(TemplateUtils.replaceTemplates(new TypeRef(null, fieldOf), typeMap)))) {
						return false;
					}
				}
			}
			
			return true;
		}
		
		/**
		 * Given a list of members, returns the list of paths that are created by appending each member to the end of the existing path.
		 * 
		 * @param newMembers
		 * @return
		 */
		public List<LookupPath> branch(List<MemberAccess> newMembers) {
			return newMembers.stream().map((member)->new LookupPath(this).add(member)).collect(Collectors.toList());
		}
		
		/**
		 * @return the member that this entire lookup will return (but not necessarily the last one in the list).
		 */
		public MemberAccess returnedMember() {
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
		
		/**
		 * @return the subject that this entire lookup will return (but not necessarily the last one in the list).
		 */
		public Subject returnedSubject() {
			if (subjects.isEmpty()) return null;
			
			Subject e = subjects.get(subjects.size()-1);
			
			while (e.infix == AccessType.DOUBLE_DOT) {
				e = e.previous;
			}
			
			return e;
		}
		
		/**
		 * @return the type map that this entire lookup will return (but not necessarily the last one in the list).
		 */
		public Map<TemplateType, TypeRef> returnedTypeMap() {
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
		
		/**
		 * Takes the final subject off the list and gives it to you.
		 * @return
		 */
		public Subject popSubject() {
			Subject sub = subjects.remove(subjects.size()-1);
			// TODO: remove non-subject members
			return sub;
		}
		
		@Override
		public String toString() {
			return "path"+members;
		}
		
		/**
		 * Returns the member you should do lookup based on.
		 * @return
		 */
		public MemberAccess getLookup() {
			Subject sub = returnedSubject();
			MemberAccess member = returnedMember();
			if (sub == null || sub.type == null || sub.member != member) {
				return member;
			} else {
				return sub.type;
			}
		}
		
		/**
		 * @return the last member (but not necessarily the one that this lookup returns).
		 */
		public MemberAccess lastMember() {
			return members.isEmpty() ? null : members.get(members.size()-1);
		}
		
		/**
		 * @return the last subject (but not necessarily the one that this lookup returns).
		 */
		public Subject lastSubject() {
			return subjects.isEmpty() ? null : subjects.get(subjects.size()-1);
		}
		
		/**
		 * @return the last type map (but not necessarily the one that this lookup returns).
		 */
		public Map<TemplateType, TypeRef> lastTypeMap() {
			return typeMaps.isEmpty() ? null : typeMaps.get(typeMaps.size()-1);
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
						.map((path)->path.branch(path.getLookup().getMembers(name.name, path.returnedTypeMap())))
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
	
	/**
	 * Given a path, this function will add the code needed to return the contents of this path to the code block provided.
	 * 
	 * @param scope The current scope.
	 * @param path The access path.
	 * @return The variable that contains the results of the lookup.
	 */
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
				scope.getCodeBlock().ops.add(new Instruction(scope.getCodeBlock().tni, source, OpCode.JUMPTRUE, new Object[] {tempVar, label}));
			}
			
			if (sub.member instanceof Variable) {
				var = (Variable) sub.member;
			}
			
			if (sub.member instanceof Field) {
				Field f = (Field) sub.member;
				Type fieldOf = f.getFieldOf();
				Variable newVar = scope.addTempVar(TemplateUtils.replaceTemplates(f.type, sub.previous == null ? sub.typeMap : sub.previous.typeMap), source);
				
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
					
					scope.getCodeBlock().ops.add(new Instruction(scope.getCodeBlock().tni, source, OpCode.CALL, new Object[] {Arrays.asList(newVar), var, f.getGetter().getVirtualBase(fieldOf), new ArrayList<>()}));
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
	
	/**
	 * An argument passed into a function. Used for function lookup.
	 * 
	 * @author iconmaster
	 *
	 */
	public static class LookupArgument {
		/**
		 * The variable containing the argument's value.
		 */
		public Variable var;
		
		/**
		 * The label of the argumnet. May be null.
		 */
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
	
	public static class FuncArgMap {
		public Map<Parameter, Variable> args = new HashMap<>();
		public Map<Parameter, List<Variable>> varargs = new HashMap<>();
		public Map<Parameter, Map<String, Variable>> varflags = new HashMap<>();
	}
	
	/**
	 * Creates a map of parameters to arguments for a function.
	 * 
	 * @param f
	 * @param args The arguments supplied, in the order they were supplied.
	 * @return
	 */
	public static FuncArgMap getFuncArgMap(Function f, List<LookupArgument> args) {
		FuncArgMap result = new FuncArgMap();
		
		// find the vararg or varflag param if they exist
		Parameter varargParam = null, varflagParam = null;
		for (Parameter param : f.getParams()) {
			if (param.hasAnnot(f.tni.corePackage.ANNOT_VARARG)) {
				varargParam = param;
				result.varargs.put(param, new ArrayList<>());
			} else if (param.hasAnnot(f.tni.corePackage.ANNOT_VARFLAG)) {
				varflagParam = param;
				result.varflags.put(param, new HashMap<>());
			}
		}
		
		// populate map
		for (LookupArgument arg : args) {
			if (arg.label == null) {
				// positional argument; find the first unallocated parameter
				boolean found = false;
				for (Parameter param : f.getParams()) {
					if (result.varargs.containsKey(param) || result.varflags.containsKey(param)) continue;
					
					if (!result.args.containsKey(param)) {
						result.args.put(param, arg.var);
						found = true;
						break;
					}
				}
				
				if (!found) {
					if (varargParam != null) {
						// put it in the vararg
						result.varargs.get(varargParam).add(arg.var);
					} else {
						// error; too many arguments
						return null;
					}
				}
			} else {
				// keyword argument; find the corresponding parameter
				boolean found = false;
				for (Parameter param : f.getParams()) {
					if (param.getName().equals(arg.label)) {
						if (result.args.containsKey(param)) {
							// error; duplicate argument
							return null;
						}
						
						result.args.put(param, arg.var);
						found = true;
						break;
					}
				}
				
				if (!found) {
					if (varflagParam != null) {
						if (!result.varflags.get(varflagParam).containsKey(arg.label)) {
							// add it to the varflags
							result.varflags.get(varflagParam).put(arg.label, arg.var);
						} else {
							// error; duplicate argument
							return null;
						}
					} else {
						// error; no argument with that key
						return null;
					}
				}
			}
		}
		
		// check to make sure no required parameters are left out
		for (Parameter param : f.getParams()) {
			if (result.varargs.containsKey(param) || result.varflags.containsKey(param)) continue;
			
			if (!param.isOptional() && !result.args.containsKey(param)) {
				// error; parameter required
				return null;
			}
		}
		
		// return the arg map
		return result;
	}
	
	public static boolean areFuncArgsCompatibleWith(Scope scope, Function f, List<LookupArgument> args, Map<TemplateType, TypeRef> typeMap, Map<Variable, ExprContext> argMap) {
		CorePackage core = f.tni.corePackage;
		if (argMap == null) argMap = new HashMap<>();
		
		// check if the argumnet's number/labels all match up to the signature
		FuncArgMap map = LookupUtils.getFuncArgMap(f, args);
		if (map == null) {
			return false;
		}
		
		// calculate the function's template map
		// TODO: add vararg stuff to these lists
		List<TypeRef> params = f.getParams().stream().filter(p->map.args.containsKey(p)).map(p->p.getType()).collect(Collectors.toList());
		List<TypeRef> args2 = f.getParams().stream().filter(p->map.args.containsKey(p)).map(p->map.args.get(p).type).collect(Collectors.toList());

		Map<TemplateType, TypeRef> funcTempMap = TemplateUtils.inferTemplatesFromArguments(core.tni, params, args2, f.getFuncTemplateMap(typeMap));
		
		// check if the types match up to the signature
		for (Entry<Parameter, Variable> entry : map.args.entrySet()) {
			TypeRef a = argMap.containsKey(entry.getValue()) ? TyphonCompiler.getExprType(scope, argMap.get(entry.getValue()), Arrays.asList(entry.getKey().getType())).get(0) : entry.getValue().type;
			TypeRef b = entry.getKey().getType();
			
			a = TemplateUtils.replaceTemplates(TemplateUtils.replaceTemplates(a, funcTempMap), typeMap);
			b = TemplateUtils.replaceTemplates(TemplateUtils.replaceTemplates(b, funcTempMap), typeMap);
			
			if (!a.canCastTo(b)) {
				return false;
			}
		}
		
		for (Entry<Parameter, List<Variable>> entry : map.varargs.entrySet()) {
			Parameter p = entry.getKey();
			TypeRef elemType = TemplateUtils.matchAllTemplateArgs(p.getType()).get(core.TYPE_LIST.getTemplates().get(0));
			elemType = TemplateUtils.replaceTemplates(TemplateUtils.replaceTemplates(elemType, funcTempMap), typeMap);
			
			for (Variable var : entry.getValue()) {
				TypeRef vtype = argMap.containsKey(var) ? TyphonCompiler.getExprType(scope, argMap.get(var), Arrays.asList(entry.getKey().getType())).get(0) : var.type;
				vtype = TemplateUtils.replaceTemplates(TemplateUtils.replaceTemplates(vtype, funcTempMap), typeMap);
				
				if (!vtype.canCastTo(elemType)) {
					return false;
				}
			}
		}
		
		for (Entry<Parameter, Map<String, Variable>> entry : map.varflags.entrySet()) {
			Parameter p = entry.getKey();
			TypeRef elemType = TemplateUtils.matchAllTemplateArgs(p.getType()).get(core.TYPE_MAP.getTemplates().get(1));
			elemType = TemplateUtils.replaceTemplates(TemplateUtils.replaceTemplates(elemType, funcTempMap), typeMap);
			
			for (Variable var : entry.getValue().values()) {
				TypeRef vtype = argMap.containsKey(var) ? TyphonCompiler.getExprType(scope, argMap.get(var), Arrays.asList(entry.getKey().getType())).get(0) : var.type;
				vtype = TemplateUtils.replaceTemplates(TemplateUtils.replaceTemplates(vtype, funcTempMap), typeMap);
				
				if (!vtype.canCastTo(elemType)) {
					return false;
				}
			}
		}
		
		return true;
	}
}
