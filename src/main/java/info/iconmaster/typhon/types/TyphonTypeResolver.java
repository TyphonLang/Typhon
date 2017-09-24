package info.iconmaster.typhon.types;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.antlr.v4.runtime.Token;

import info.iconmaster.typhon.TyphonInput;
import info.iconmaster.typhon.antlr.TyphonParser.ArrayTypeContext;
import info.iconmaster.typhon.antlr.TyphonParser.BasicTypeContext;
import info.iconmaster.typhon.antlr.TyphonParser.ComboTypeContext;
import info.iconmaster.typhon.antlr.TyphonParser.ConstTypeContext;
import info.iconmaster.typhon.antlr.TyphonParser.FuncTypeContext;
import info.iconmaster.typhon.antlr.TyphonParser.MapTypeContext;
import info.iconmaster.typhon.antlr.TyphonParser.TemplateArgContext;
import info.iconmaster.typhon.antlr.TyphonParser.TypeContext;
import info.iconmaster.typhon.antlr.TyphonParser.TypeMemberItemContext;
import info.iconmaster.typhon.antlr.TyphonParser.VarTypeContext;
import info.iconmaster.typhon.errors.AmbiguousAnnotError;
import info.iconmaster.typhon.errors.AmbiguousTypeError;
import info.iconmaster.typhon.errors.AnnotFormatError;
import info.iconmaster.typhon.errors.AnnotNotFoundError;
import info.iconmaster.typhon.errors.ParentTypeError;
import info.iconmaster.typhon.errors.TemplateDefaultTypeError;
import info.iconmaster.typhon.errors.TypeNotFoundError;
import info.iconmaster.typhon.model.Annotation;
import info.iconmaster.typhon.model.AnnotationDefinition;
import info.iconmaster.typhon.model.Field;
import info.iconmaster.typhon.model.Function;
import info.iconmaster.typhon.model.MemberAccess;
import info.iconmaster.typhon.model.Package;
import info.iconmaster.typhon.model.Parameter;
import info.iconmaster.typhon.model.TemplateArgument;
import info.iconmaster.typhon.model.TyphonModelReader;
import info.iconmaster.typhon.util.SourceInfo;
import info.iconmaster.typhon.util.TemplateUtils;

/**
 * This class turns rules for ANTLR types into Typhon types by scanning through packages and translating raw data
 * previously set with <tt>setRawData</tt>.
 * 
 * Type resolution is only fully effective after linking is done.
 * 
 * @author iconmaster
 *
 */
public class TyphonTypeResolver {
	private TyphonTypeResolver() {}
	
	/**
	 * Resolves all the types in a package (and subpackages).
	 * 
	 * @param p The package to resolve.
	 */
	public static void resolve(Package p) {
		if (!p.needsTypesResolved()) {
			return;
		}
		p.needsTypesResolved(false);
		
		p.getFields().stream().forEach((e)->resolve(e));
		p.getFunctions().stream().forEach((e)->resolve(e));
		p.getSubpackges().stream().forEach((e)->resolve(e));
		p.getTypes().stream().forEach((e)->resolve(e));
		
		p.getAnnots().stream().forEach((e)->resolve(e, p));
	}
	
	/**
	 * Resolves all the types in a function.
	 * 
	 * @param f The function to resolve.
	 */
	public static void resolve(Function f) {
		if (!f.needsTypesResolved()) {
			return;
		}
		f.needsTypesResolved(false);
		
		// do type resolution
		for (TemplateType t : f.getTemplate()) {
			resolve(t);
		}
		
		for (TypeContext rule : f.getRawRetType()) {
			f.getRetType().add(readType(f.tni, rule, f));
		}
		
		for (Parameter p : f.getParams()) {
			resolve(p, f);
		}
		
		f.getAnnots().stream().forEach((e)->resolve(e, f));
		
		// check that the annotations are valid
		boolean hasVararg = false, hasVarflag = false;
		for (Parameter p : f.getParams()) {
			// check for varvarg/varflag
			List<Annotation> varargs = p.getAnnots(f.tni.corePackage.ANNOT_VARARG);
			List<Annotation> varflags = p.getAnnots(f.tni.corePackage.ANNOT_VARFLAG);
			
			if (varargs.size() > 1 || varflags.size() > 1) {
				f.tni.errors.add(new AnnotFormatError(p.source, varargs.size() <= 1 ? varflags.get(0) : varargs.get(0), "Cannot be annotated multiple times"));
			}
			
			if (!varargs.isEmpty()) {
				if (hasVararg) {
					f.tni.errors.add(new AnnotFormatError(p.source, varargs.get(0), "Only 1 parameter may have this annotation"));
				}
				
				if (p.isOptional()) {
					f.tni.errors.add(new AnnotFormatError(p.source, varargs.get(0), "Parameter cannot be optional"));
				}
				
				if (p.getType().getType() != f.tni.corePackage.TYPE_LIST) {
					f.tni.errors.add(new AnnotFormatError(p.source, varargs.get(0), "Type of parameter must be List"));
				}
				
				hasVararg = true;
			}
			
			if (!varflags.isEmpty()) {
				if (hasVarflag) {
					f.tni.errors.add(new AnnotFormatError(p.source, varflags.get(0), "Only 1 parameter may have this annotation"));
				}
				
				if (p.isOptional()) {
					f.tni.errors.add(new AnnotFormatError(p.source, varflags.get(0), "Parameter cannot be optional"));
				}
				
				if (p.getType().getType() != f.tni.corePackage.TYPE_MAP) {
					f.tni.errors.add(new AnnotFormatError(p.source, varflags.get(0), "Type of parameter must be Map"));
				} else {
					TemplateType keyTemp = f.tni.corePackage.TYPE_MAP.getTemplates().get(0);
					Map<TemplateType, TypeRef> tempMap = TemplateUtils.matchAllTemplateArgs(p.getType());
					
					if (!tempMap.containsKey(keyTemp) || tempMap.get(keyTemp).getType() != f.tni.corePackage.TYPE_STRING) {
						f.tni.errors.add(new AnnotFormatError(p.source, varflags.get(0), "Key type of parameter must be String"));
					}
				}
				
				hasVarflag = true;
			}
			
			// check for other parameter annots
		}
	}
	
	/**
	 * Resolves all the types in a field.
	 * 
	 * @param f The field to resolve.
	 */
	public static void resolve(Field f) {
		if (!f.needsTypesResolved()) {
			return;
		}
		f.needsTypesResolved(false);
		
		f.setType(readType(f.tni, f.getRawType(), f.getMemberParent()));
		
		f.getAnnots().stream().forEach((e)->resolve(e, f.getMemberParent()));
	}
	
	/**
	 * Resolves all the types in a type definition.
	 * 
	 * @param f The type to resolve.
	 * @param lookup The package in which this type occurs.
	 */
	public static void resolve(Type t) {
		if (!t.needsTypesResolved()) {
			return;
		}
		t.needsTypesResolved(false);
		
		if (t instanceof UserType) {
			UserType userType = (UserType) t;

			for (TypeContext rule : userType.getRawParentTypes()) {
				TypeRef parentType = readType(userType.tni, rule, t);
				userType.getParentTypes().add(parentType);
				
				if (!(parentType.getType() instanceof AnyType || parentType.getType() instanceof UserType)) {
					t.tni.errors.add(new ParentTypeError(parentType.source, userType, parentType));
				}
			}
			
			if (userType.getParentTypes().isEmpty()) {
				userType.getParentTypes().add(new TypeRef(t.tni.corePackage.TYPE_ANY));
			}
		} else if (t instanceof TemplateType) {
			TemplateType tempType = (TemplateType) t;
			tempType.setBaseType(readType(tempType.tni, tempType.getRawBaseType(), t));
			if (tempType.getRawDefaultValue() != null) tempType.setDefaultValue(readType(tempType.tni, tempType.getRawDefaultValue(), t));
			
			if (tempType.getDefaultValue() != null && !tempType.getDefaultValue().canCastTo(tempType.getBaseType())) {
				// error; default value must be a subtype of the base value
				t.tni.errors.add(new TemplateDefaultTypeError(tempType));
			}
		} else if (t instanceof FunctionType) {
			FunctionType funcType = (FunctionType) t;
			
			for (TypeRef argType : funcType.getArgTypes()) {
				resolve(argType.getType());
			}
			
			for (TypeRef retType : funcType.getRetTypes()) {
				resolve(retType.getType());
			}
		}
		
		resolve(t.getTypePackage());
		
		t.getAnnots().stream().forEach((e)->resolve(e, t));
	}
	
	/**
	 * Resolves all the types in a parameter.
	 * 
	 * @param p The parameter to resolve.
	 * @param lookup The package in which this parameter occurs.
	 */
	public static void resolve(Parameter p, MemberAccess lookup) {
		if (!p.needsTypesResolved()) {
			return;
		}
		p.needsTypesResolved(false);
		
		p.setType(readType(p.tni, p.getRawType(), lookup));
		
		p.getAnnots().stream().forEach((e)->resolve(e, lookup));
	}
	
	/**
	 * Finds the AnnotationDefinition for a given Annotation.
	 * 
	 * @param a The annotation to resolve.
	 * @param lookup The package in which this annotation occurs.
	 */
	public static void resolve(Annotation a, MemberAccess lookup) {
		if (!a.needsTypesResolved()) {
			return;
		}
		a.needsTypesResolved(false);
		
		MemberAccess base = lookup;
		
		while (base != null) {
			List<MemberAccess> matches = new ArrayList<>();
			matches.add(base);
			
			for (Token name : a.getRawDefinition().tnName) {
				// do the lookup of a single member
				List<MemberAccess> newMatches = new ArrayList<>();
				
				for (MemberAccess match : matches) {
					newMatches.addAll(match.getMembers(name.getText(), new HashMap<>()));
				}
				
				matches = newMatches;
			}
			
			List<AnnotationDefinition> candidates = matches.stream().filter((e)->e instanceof AnnotationDefinition).map((e)->(AnnotationDefinition)e).collect(Collectors.toList());
			if (!candidates.isEmpty()) {
				if (candidates.size() != 1) {
					a.tni.errors.add(new AmbiguousAnnotError(a.getRawDefinition().tnName, candidates));
					return;
				}
				
				a.setDefinition(candidates.get(0));
				return;
			}
			
			base = base.getMemberParent();
		}
		
		a.tni.errors.add(new AnnotNotFoundError(a.getRawDefinition().tnName));
		return;
	}
	
	/**
	 * Converts an ANTLR rule for a type into a Typhon type.
	 * 
	 * @param tni
	 * @param rule The ANTLR rule to parse. If it is null, this function will return TYPE_ANY.
	 * @param lookup Where this rule occurs in the Typhon model.
	 * @return A Typhon type representing the input.
	 * If the type could not be resolved, it will return TYPE_ANY and add an error to the TyphonInput.
	 */
	public static TypeRef readType(TyphonInput tni, TypeContext rule, MemberAccess lookup) {
		if (rule == null) return new TypeRef(tni.corePackage.TYPE_ANY);
		
		if (rule instanceof FuncTypeContext) {
			FunctionType type = new FunctionType(tni, new SourceInfo(rule));
			type.setLookupLocation(lookup);
			
			if (((FuncTypeContext) rule).tnTemplate != null) {
				type.getTemplate().addAll(TyphonModelReader.readTemplateParams(tni, ((FuncTypeContext) rule).tnTemplate.tnArgs));
			}
			
			for (TypeContext argRule : ((FuncTypeContext) rule).tnArgTypes) {
				type.getArgTypes().add(readType(tni, argRule, type));
			}
			for (TypeContext retRule : TyphonModelReader.readTypes(((FuncTypeContext) rule).tnRetType)) {
				type.getRetTypes().add(readType(tni, retRule, type));
			}
			
			return new TypeRef(new SourceInfo(rule), type);
		} else if (rule instanceof ArrayTypeContext) {
			TypeRef ref = new TypeRef(new SourceInfo(rule), tni.corePackage.TYPE_LIST);
			ref.getTemplateArgs().add(new TemplateArgument(new SourceInfo(rule), readType(tni, ((ArrayTypeContext) rule).tnBaseType, lookup)));
			return ref;
		} else if (rule instanceof MapTypeContext) {
			TypeRef ref = new TypeRef(new SourceInfo(rule), tni.corePackage.TYPE_MAP);
			ref.getTemplateArgs().add(new TemplateArgument(new SourceInfo(rule), readType(tni, ((MapTypeContext) rule).tnKeyType, lookup)));
			ref.getTemplateArgs().add(new TemplateArgument(new SourceInfo(rule), readType(tni, ((MapTypeContext) rule).tnValueType, lookup)));
			return ref;
		} else if (rule instanceof VarTypeContext) {
			return TypeRef.var(tni);
		} else if (rule instanceof ConstTypeContext) {
			TypeRef ref = readType(tni, ((ConstTypeContext) rule).tnType, lookup);
			ref.source = new SourceInfo(rule);
			ref.isConst(true);
			return ref;
		} else if (rule instanceof ComboTypeContext) {
			ComboType type = new ComboType(tni, new SourceInfo(rule));
			
			for (TypeContext subtype : ((ComboTypeContext) rule).tnTypes) {
				type.getTypes().add(readType(tni, subtype, lookup));
			}
			
			TypeRef ref = new TypeRef(new SourceInfo(rule), type);
			return ref;
		} else if (rule instanceof BasicTypeContext) {
			MemberAccess base = lookup;
			Map<TemplateType, TypeRef> map = new HashMap<>();
			
			while (base != null) {
				List<MemberAccess> matches = new ArrayList<>();
				matches.add(base);
				
				for (TypeMemberItemContext name : ((BasicTypeContext) rule).tnLookup) {
					// ensure all lookup members are resolved already
					for (MemberAccess match : matches) {
						if (match instanceof Type) {
							resolve((Type)match);
						}
					}
					
					// do the lookup of a single member
					List<MemberAccess> newMatches = new ArrayList<>();
					
					for (MemberAccess match : matches) {
						// handle template types correctly
						if (match instanceof TemplateType && map.containsKey(match)) {
							match = map.get(match);
							map = TemplateUtils.matchAllTemplateArgs((TypeRef) match);
						}
						
						// find the next members with the given name
						List<MemberAccess> found = match.getMembers(name.tnName.getText(), map);
						
						// replace Types with their proper TypeRefs if there is a template
						// since only types can have templates, elements may be removed from the list of found things
						if (name.tnTemplate != null) {
							List<MemberAccess> newFound = new ArrayList<>();
							
							for (MemberAccess member : found) {
								if (member instanceof Type) {
									TypeRef ref = new TypeRef(tni);
									ref.setType((Type) member);
									ref.getTemplateArgs().addAll(readTemplateArgs(tni, name.tnTemplate.tnArgs, lookup));
									ref.getTemplateArgs().stream().forEach((tt)->resolve(tt.getValue().getType()));
									
									map = TemplateUtils.matchAllTemplateArgs(ref);
									newFound.add(TemplateUtils.replaceTemplates(ref, map));
									
									TemplateUtils.checkTemplateArgs(ref);
								}
							}
							
							found = newFound;
						}
						
						// finish the loop
						newMatches.addAll(found);
					}
					
					matches = newMatches;
				}
				
				List<TypeRef> candidates = matches.stream().filter((e)->e instanceof Type || e instanceof TypeRef).map((e)->(e instanceof Type ? new TypeRef(((Type)e).source, (Type)e) : ((TypeRef)e))).collect(Collectors.toList());
				if (!candidates.isEmpty()) {
					if (candidates.size() != 1) {
						tni.errors.add(new AmbiguousTypeError(((BasicTypeContext) rule).tnLookup, candidates));
						return new TypeRef(tni.corePackage.TYPE_ANY);
					}
					
					return candidates.get(0);
				}
				
				base = base.getMemberParent();
			}
			
			tni.errors.add(new TypeNotFoundError(((BasicTypeContext) rule).tnLookup));
			return new TypeRef(tni.corePackage.TYPE_ANY);
		} else {
			throw new IllegalArgumentException("Unknown subclass of TypeContext");
		}
	}
	
	/**
	 * Converts ANTLR rules for template arguments into their respective types.
	 * 
	 * @param tni
	 * @param rules A list of rules for template arguments.
	 * @param lookup Where this rule occurs in the Typhon model.
	 * @return A list of type arguments the input represents.
	 */
	public static List<TemplateArgument> readTemplateArgs(TyphonInput tni, List<TemplateArgContext> rules, MemberAccess lookup) {
		return rules.stream().map((rule)->{
			TemplateArgument arg = new TemplateArgument(tni, new SourceInfo(rule));
			
			if (rule.tnLabel != null) arg.setLabel(rule.tnLabel.getText());
			arg.setValue(readType(tni, rule.tnType, lookup));
			
			return arg;
		}).collect(Collectors.toCollection(()->new ArrayList<>()));
	}
}
