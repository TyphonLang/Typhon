package info.iconmaster.typhon.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import info.iconmaster.typhon.errors.TemplateLabelNotFoundError;
import info.iconmaster.typhon.errors.TemplateNumberError;
import info.iconmaster.typhon.errors.TemplateTypeError;
import info.iconmaster.typhon.model.TemplateArgument;
import info.iconmaster.typhon.types.FunctionType;
import info.iconmaster.typhon.types.TemplateType;
import info.iconmaster.typhon.types.Type;
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
	 * @param typeToMap The type you're checking. Used for error output.
	 * @param template The list of template parameters.
	 * @param args The list of template arguments.
	 */
	public static void checkTemplateArgs(TypeRef typeToMap) {
		Map<TemplateType, TypeRef> result = new HashMap<>();
		
		// insert all the named arguments
		for (TemplateArgument arg : typeToMap.getTemplateArgs()) {
			if (arg.getLabel() != null) {
				TemplateType found = null;
				
				for (TemplateType type : typeToMap.getMemberTemplate()) {
					if (type.getName().equals(arg.getLabel())) {
						found = type;
						result.put(type, arg.getValue());
						break;
					}
				}
				
				if (found == null) {
					// no position for the argument; error
					typeToMap.tni.errors.add(new TemplateLabelNotFoundError(typeToMap, arg));
				}
			}
		}
		
		// insert all the positional arguments. They should go in the first type in which they can fit.
		for (TemplateArgument arg : typeToMap.getTemplateArgs()) {
			if (arg.getLabel() == null) {
				TemplateType found = null;
				
				for (TemplateType type : typeToMap.getMemberTemplate()) {
					if (!result.containsKey(type)) {
						if (!arg.getValue().canCastTo(new TypeRef(type))) {
							// Cannot cast to next logical template in the sequence; error
							typeToMap.tni.errors.add(new TemplateTypeError(typeToMap, type, arg.getValue()));
						}
						
						found = type;
						result.put(type, arg.getValue());
						break;
					}
				}
				
				if (found == null) {
					// too many template arguments; error
					typeToMap.tni.errors.add(new TemplateNumberError(typeToMap));
				}
			}
		}
		
		// recursively check subtemplates
		for (TemplateArgument arg : typeToMap.getTemplateArgs()) {
			checkTemplateArgs(arg.getValue());
		}
	}
	
	/**
	 * Generates a mapping of template parameters to arguments for a type.
	 * 
	 * @param typeToMap The type you're working on. Used for error output.
	 * @param template The list of template parameters.
	 * @param args The list of template arguments.
	 */
	public static Map<TemplateType, TypeRef> matchTemplateArgs(TypeRef typeToMap) {
		Map<TemplateType, TypeRef> result = new HashMap<>();
		
		// insert all the named arguments
		for (TemplateArgument arg : typeToMap.getTemplateArgs()) {
			if (arg.getLabel() != null) {
				for (TemplateType type : typeToMap.getMemberTemplate()) {
					if (type.getName().equals(arg.getLabel())) {
						result.put(type, arg.getValue());
						break;
					}
				}
			}
		}
		
		// insert all the positional arguments. They should go in the first type in which they can fit.
		for (TemplateArgument arg : typeToMap.getTemplateArgs()) {
			if (arg.getLabel() == null) {
				for (TemplateType type : typeToMap.getMemberTemplate()) {
					if (!result.containsKey(type)) {
						result.put(type, arg.getValue());
						break;
					}
				}
			}
		}
		
		// finally, replace all missing arguments with their defaults.
		for (TemplateType type : typeToMap.getMemberTemplate()) {
			if (!result.containsKey(type)) {
				result.put(type, type.getDefaultValue() == null ? type.getBaseType() : type.getDefaultValue());
			}
		}
		
		return result;
	}
	
	/**
	 * Generates a mapping of template parameters to arguments for a type, and all template types held inside this type.
	 * 
	 * @param typeToMap The type you're working on. Used for error output.
	 * @param template The list of template parameters.
	 * @param args The list of template arguments.
	 */
	public static Map<TemplateType, TypeRef> matchAllTemplateArgs(TypeRef typeToMap) {
		Map<TemplateType, TypeRef> result = new HashMap<>();
		
		for (TemplateArgument arg : typeToMap.getTemplateArgs()) {
			result.putAll(matchAllTemplateArgs(arg.getValue()));
		}
		
		result.putAll(matchTemplateArgs(typeToMap));
		
		return result;
	}
	
	public static Map<TemplateType, TypeRef> inferMapFromArguments(List<TypeRef> params, List<TypeRef> args, Map<TemplateType, TypeRef> defaults) {
		Map<TemplateType, TypeRef> result = new HashMap<>();
		
		inferMapFromArguments(params, args, defaults, result);
		
		return new HashMap<TemplateType, TypeRef>(defaults){{
			putAll(result);
		}};
	}
	
	private static void inferMapFromArguments(List<TypeRef> params, List<TypeRef> args, Map<TemplateType, TypeRef> defaults, Map<TemplateType, TypeRef> result) {
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
				int minLen = Math.min(param.getTemplateArgs().size(), arg.getTemplateArgs().size());
				List<TypeRef> paramList = param.getTemplateArgs().subList(0, minLen).stream().map((a)->a.getValue()).collect(Collectors.toList());
				List<TypeRef> argList = arg.getTemplateArgs().subList(0, minLen).stream().map((a)->a.getValue()).collect(Collectors.toList());
				
				inferMapFromArguments(paramList, argList, defaults, result);
			}
			
			i++;
		}
	}
}
