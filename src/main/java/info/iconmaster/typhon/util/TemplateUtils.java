package info.iconmaster.typhon.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import info.iconmaster.typhon.TyphonInput;
import info.iconmaster.typhon.errors.TemplateLabelNotFoundError;
import info.iconmaster.typhon.errors.TemplateNumberError;
import info.iconmaster.typhon.errors.TemplateTypeError;
import info.iconmaster.typhon.model.TemplateArgument;
import info.iconmaster.typhon.model.TyphonModelEntity;
import info.iconmaster.typhon.types.FunctionType;
import info.iconmaster.typhon.types.TemplateType;
import info.iconmaster.typhon.types.TypeRef;
import info.iconmaster.typhon.types.UserType;

/**
 * This class contains utilities related to Typhon's templating system.
 * 
 * @author iconmaster
 *
 */
public class TemplateUtils {
	private TemplateUtils() {}
	
	/**
	 * Given a mapping of template parameters to arguments, finds the instance of a type with templates replaced.
	 * 
	 * @param typeToReplace The template to do replacements on.
	 * @param newTypes A map of templates to thier replacements.
	 * @return typeToReplace, with templates replaced.
	 */
	public static TypeRef replaceTemplates(TypeRef typeToReplace, Map<TemplateType, TypeRef> newTypes) {
		if (typeToReplace.getType() instanceof TemplateType) {
			if (newTypes.containsKey(typeToReplace.getType())) {
				return newTypes.get(typeToReplace.getType());
			}
			return typeToReplace;
		} else if (typeToReplace.getType() instanceof UserType) {
			TypeRef newRef = new TypeRef(typeToReplace.getType());
			for (TemplateArgument tempType : typeToReplace.getTemplateArgs()) {
				newRef.getTemplateArgs().add(new TemplateArgument(replaceTemplates(tempType.getValue(), newTypes)));
			}
			return newRef;
		} else if (typeToReplace.getType() instanceof FunctionType) {
			FunctionType funcType = new FunctionType(typeToReplace.tni, typeToReplace.source);
			
			for (TypeRef argType : ((FunctionType)typeToReplace.getType()).getArgTypes()) {
				funcType.getArgTypes().add(replaceTemplates(argType, newTypes));
			}
			
			for (TypeRef retType : ((FunctionType)typeToReplace.getType()).getRetTypes()) {
				funcType.getRetTypes().add(replaceTemplates(retType, newTypes));
			}
			
			TypeRef newRef = new TypeRef(funcType);
			return newRef;
		} else {
			return typeToReplace;
		}
	}
	
	/**
	 * Checks to ensure a template instantiation is valid. Adds errors if it isn't.
	 * 
	 * @param typeToMap The type you're checking.
	 */
	public static void checkTemplateArgs(TypeRef typeToMap) {
		checkTemplateArgs(typeToMap, typeToMap.getName(), typeToMap.getMemberTemplate(), typeToMap.getTemplateArgs());
	}
	
	/**
	 * Generates a mapping of template parameters to arguments for a type.
	 * Does NOT include default values in its output. If you want those, use matchAllTemplateArgs instead.
	 * 
	 * @param typeToMap The type you're working on.
	 */
	public static Map<TemplateType, TypeRef> matchTemplateArgs(TypeRef typeToMap) {
		return matchTemplateArgs(typeToMap.getMemberTemplate(), typeToMap.getTemplateArgs());
	}
	
	/**
	 * Generates a mapping of template parameters to arguments for a type, with suitable defaults placed in if not present.
	 * 
	 * @param typeToMap The type you're working on.
	 */
	public static Map<TemplateType, TypeRef> matchAllTemplateArgs(TypeRef typeToMap) {
		return matchAllTemplateArgs(typeToMap.getMemberTemplate(), typeToMap.getTemplateArgs());
	}
	
	/**
	 * This function is used to infer template types where necessary, as in functions or constructors.
	 * 
	 * @param tni
	 * @param params Parameters. Should include {@link TemplateType}s.
	 * @param args Arguments. Should include the replacements for the {@link TemplateType}s.
	 * @param defaults The default values for any template types included.
	 * @return A map of template parameters to the inferred arguments. If nothing was inferred, the default value is used.
	 */
	public static Map<TemplateType, TypeRef> inferTemplatesFromArguments(TyphonInput tni, List<TypeRef> params, List<TypeRef> args, Map<TemplateType, TypeRef> defaults) {
		Map<TemplateType, TypeRef> result = new HashMap<>();
		
		inferTemplatesFromArguments(tni, params, args, defaults, result);
		
		return new HashMap<TemplateType, TypeRef>(defaults){{
			putAll(result);
		}};
	}
	
	private static void inferTemplatesFromArguments(TyphonInput tni, List<TypeRef> params, List<TypeRef> args, Map<TemplateType, TypeRef> defaults, Map<TemplateType, TypeRef> result) {
		int i = 0;
		for (TypeRef param : params) {
			TypeRef arg = args.get(i);
			
			if (param.getType() instanceof TemplateType) {
				// we have a match
				TemplateType t = (TemplateType) param.getType();
				if (!defaults.containsKey(t) || arg.canCastTo(defaults.get(t))) {
					if (result.containsKey(t)) {
						TypeRef inMap = result.get(t);
						
						if (!arg.canCastTo(inMap) && inMap.canCastTo(arg)) {
							// widening of current value.
							result.put(t, arg);
						} else if (defaults.containsKey(t)) {
							// they both cast to the default, so do that at the very least.
							// TODO: find most narrow common parentage instead
							result.put(t, defaults.get(t));
						}
					} else if (arg.canCastTo(param)) {
						// narrowing of default
						result.put(t, arg);
					}
				}
			} else if (param.getType() instanceof UserType) {
				// recurse on template arguments
				List<TypeRef> paramList = new ArrayList<>();
				List<TypeRef> argList = new ArrayList<>();
				
				Map<TemplateType, TypeRef> paramMap = matchTemplateArgs(param);
				Map<TemplateType, TypeRef> argMap = matchTemplateArgs(arg);
				
				HashMap<TemplateType, TypeRef> withAll = new HashMap<>(paramMap);
				withAll.putAll(argMap);
				for (TemplateType t : withAll.keySet()) {
					if (paramMap.containsKey(t) && argMap.containsKey(t)) {
						paramList.add(paramMap.get(t));
						argList.add(argMap.get(t));
					}
				}
				
				inferTemplatesFromArguments(tni, paramList, argList, defaults, result);
			}
			
			i++;
		}
	}
	
	/**
	 * Generates a mapping of template parameters to arguments for a type.
	 * Does NOT include default values in its output. If you want those, use matchAllTemplateArgs instead.
	 * 
	 * @param params The list of template parameters.
	 * @param args The list of template arguments.
	 */
	public static Map<TemplateType, TypeRef> matchTemplateArgs(List<TemplateType> params, List<TemplateArgument> args) {
		Map<TemplateType, TypeRef> result = new HashMap<>();
		
		// insert all the named arguments
		for (TemplateArgument arg : args) {
			if (arg.getLabel() != null) {
				for (TemplateType type : params) {
					if (type.getName().equals(arg.getLabel())) {
						result.put(type, arg.getValue());
						break;
					}
				}
			}
		}
		
		// insert all the positional arguments. They should go in the first type in which they can fit.
		for (TemplateArgument arg : args) {
			if (arg.getLabel() == null) {
				for (TemplateType type : params) {
					if (!result.containsKey(type)) {
						result.put(type, arg.getValue());
						break;
					}
				}
			}
		}
		
		// return the result
		return result;
	}
	
	/**
	 * Generates a mapping of template parameters to arguments for a type, with suitable defaults placed in if not present.
	 * 
	 * @param template The list of template parameters.
	 * @param args The list of template arguments.
	 */
	public static Map<TemplateType, TypeRef> matchAllTemplateArgs(List<TemplateType> params, List<TemplateArgument> args) {
		// generate the template arguments
		Map<TemplateType, TypeRef> result = matchTemplateArgs(params, args);
		
		// replace all missing arguments with their defaults.
		for (TemplateType type : params) {
			if (!result.containsKey(type)) {
				result.put(type, type.getDefaultValue() == null ? type.getBaseType() : type.getDefaultValue());
			}
		}
		
		// return the result
		return result;
	}
	
	/**
	 * Checks to ensure a template instantiation is valid. Adds errors if it isn't.
	 * 
	 * @param toMap The thing you're checking. Used for error output.
	 * @param toMapDesc The name of the thing you're checking. Used for error output.
	 * @param params The list of template parameters.
	 * @param args The list of template arguments.
	 */
	public static void checkTemplateArgs(TyphonModelEntity toMap, String toMapDesc, List<TemplateType> params, List<TemplateArgument> args) {
		Map<TemplateType, TypeRef> result = new HashMap<>();
		
		// insert all the named arguments
		for (TemplateArgument arg : args) {
			if (arg.getLabel() != null) {
				TemplateType found = null;
				
				for (TemplateType type : params) {
					if (type.getName().equals(arg.getLabel())) {
						found = type;
						result.put(type, arg.getValue());
						break;
					}
				}
				
				if (found == null) {
					// no position for the argument; error
					toMap.tni.errors.add(new TemplateLabelNotFoundError(toMap, toMapDesc, arg));
				}
			}
		}
		
		// insert all the positional arguments. They should go in the first type in which they can fit.
		for (TemplateArgument arg : args) {
			if (arg.getLabel() == null) {
				TemplateType found = null;
				
				for (TemplateType type : params) {
					if (!result.containsKey(type)) {
						if (!arg.getValue().canCastTo(new TypeRef(type))) {
							// Cannot cast to next logical template in the sequence; error
							toMap.tni.errors.add(new TemplateTypeError(toMap, toMapDesc, type, arg.getValue()));
						}
						
						found = type;
						result.put(type, arg.getValue());
						break;
					}
				}
				
				if (found == null) {
					// too many template arguments; error
					toMap.tni.errors.add(new TemplateNumberError(toMap, toMapDesc));
				}
			}
		}
		
		// recursively check subtemplates
		for (TemplateArgument arg : args) {
			checkTemplateArgs(arg.getValue(), arg.getValue().getName(), arg.getValue().getMemberTemplate(), arg.getValue().getTemplateArgs());
		}
	}
}
