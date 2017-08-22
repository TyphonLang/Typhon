package info.iconmaster.typhon.types;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import info.iconmaster.typhon.errors.TemplateLabelNotFoundError;
import info.iconmaster.typhon.errors.TemplateNumberError;
import info.iconmaster.typhon.errors.TemplateTypeError;
import info.iconmaster.typhon.model.TemplateArgument;

public class TemplateUtils {
	private TemplateUtils() {}
	
	public static TypeRef replaceTemplates(TypeRef typeToReplace, Map<TemplateType, TypeRef> newTypes) {
		if (typeToReplace.getType() instanceof UserType) {
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
	
	public static Map<TemplateType, TypeRef> matchTemplateArgs(TypeRef typeToMap, List<TemplateType> template, List<TemplateArgument> args) {
		Map<TemplateType, TypeRef> result = new HashMap<>();
		
		// insert all the named arguments
		for (TemplateArgument arg : args) {
			if (arg.getLabel() != null) {
				TemplateType found = null;
				
				for (TemplateType type : template) {
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
		for (TemplateArgument arg : args) {
			if (arg.getLabel() == null) {
				TemplateType found = null;
				
				for (TemplateType type : template) {
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
		
		// finally, replace all missing arguments with their defaults.
		for (TemplateType type : template) {
			if (!result.containsKey(type)) {
				result.put(type, type.getDefaultValue() == null ? type.getBaseType() : type.getDefaultValue());
			}
		}
		
		return result;
	}
}
