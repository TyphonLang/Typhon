package info.iconmaster.typhon.types;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.antlr.v4.runtime.Token;

import info.iconmaster.typhon.TyphonInput;
import info.iconmaster.typhon.antlr.TyphonParser.ArrayTypeContext;
import info.iconmaster.typhon.antlr.TyphonParser.BasicTypeContext;
import info.iconmaster.typhon.antlr.TyphonParser.ConstTypeContext;
import info.iconmaster.typhon.antlr.TyphonParser.FuncTypeContext;
import info.iconmaster.typhon.antlr.TyphonParser.MapTypeContext;
import info.iconmaster.typhon.antlr.TyphonParser.TemplateArgContext;
import info.iconmaster.typhon.antlr.TyphonParser.TypeContext;
import info.iconmaster.typhon.antlr.TyphonParser.TypeMemberItemContext;
import info.iconmaster.typhon.antlr.TyphonParser.VarTypeContext;
import info.iconmaster.typhon.errors.AmbiguousAnnotError;
import info.iconmaster.typhon.errors.AmbiguousTypeError;
import info.iconmaster.typhon.errors.AnnotNotFoundError;
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
		p.getTypes().stream().forEach((e)->resolve(e));
		p.getSubpackges().stream().forEach((e)->resolve(e));
		
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
		
		for (TypeContext rule : f.getRawRetType()) {
			f.getRetType().add(readType(f.tni, rule, f.getMemberParent()));
		}
		
		for (Parameter p : f.getParams()) {
			resolve(p, f.getMemberParent());
		}
		
		f.getAnnots().stream().forEach((e)->resolve(e, f.getMemberParent()));
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
				userType.getParentTypes().add(readType(userType.tni, rule, t));
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
				// ensure all lookup members are resolved already
				for (MemberAccess match : matches) {
					if (match instanceof Type) {
						resolve((Type)match);
					}
				}
				
				// do the lookup of a single member
				List<MemberAccess> newMatches = new ArrayList<>();
				
				for (MemberAccess match : matches) {
					newMatches.addAll(match.getMembers(name.getText()));
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
			
			for (TypeContext argRule : ((FuncTypeContext) rule).tnArgTypes) {
				type.getArgTypes().add(readType(tni, argRule, lookup));
			}
			for (TypeContext retRule : TyphonModelReader.readTypes(((FuncTypeContext) rule).tnRetType)) {
				type.getRetTypes().add(readType(tni, retRule, lookup));
			}
			if (((FuncTypeContext) rule).tnTemplate != null) {
				type.getTemplate().addAll(TyphonModelReader.readTemplateParams(tni, ((FuncTypeContext) rule).tnTemplate.tnArgs));
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
			TypeRef ref = new TypeRef(new SourceInfo(rule), tni.corePackage.TYPE_ANY);
			ref.isVar(true);
			return ref;
		} else if (rule instanceof ConstTypeContext) {
			TypeRef ref = readType(tni, ((ConstTypeContext) rule).tnType, lookup);
			ref.source = new SourceInfo(rule);
			ref.isConst(true);
			return ref;
		} else if (rule instanceof BasicTypeContext) {
			MemberAccess base = lookup;
			
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
						newMatches.addAll(match.getMembers(name.tnName.getText()));
					}
					
					matches = newMatches;
				}
				
				List<Type> candidates = matches.stream().filter((e)->e instanceof Type).map((e)->(Type)e).collect(Collectors.toList());
				if (!candidates.isEmpty()) {
					if (candidates.size() != 1) {
						tni.errors.add(new AmbiguousTypeError(((BasicTypeContext) rule).tnLookup, candidates));
						return new TypeRef(tni.corePackage.TYPE_ANY);
					}
					
					TypeMemberItemContext lastLookup = ((BasicTypeContext) rule).tnLookup.get(((BasicTypeContext) rule).tnLookup.size()-1);
					
					TypeRef ref = new TypeRef(new SourceInfo(rule), candidates.get(0));
					if (lastLookup.tnTemplate != null) {
						ref.getTemplateArgs().addAll(readTemplateArgs(tni, lastLookup.tnTemplate.tnArgs, lookup));
						TemplateUtils.checkTemplateArgs(ref, ref.getType().getMemberTemplate(), ref.getTemplateArgs());
					}
					return ref;
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
