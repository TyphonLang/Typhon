package info.iconmaster.typhon.types;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import info.iconmaster.typhon.errors.AbstractFunctionError;
import info.iconmaster.typhon.errors.AnnotFormatError;
import info.iconmaster.typhon.errors.DuplicateOverrideError;
import info.iconmaster.typhon.errors.VirtualBaseNotFoundError;
import info.iconmaster.typhon.model.Annotation;
import info.iconmaster.typhon.model.Constructor;
import info.iconmaster.typhon.model.Field;
import info.iconmaster.typhon.model.Function;
import info.iconmaster.typhon.model.Package;
import info.iconmaster.typhon.model.Parameter;
import info.iconmaster.typhon.model.Function.Form;
import info.iconmaster.typhon.util.TemplateUtils;

public class TyphonAnnotChecker {
	private TyphonAnnotChecker() {}
	
	public static void check(Package p) {
		if (!p.needsAnnotsChecked()) {
			return;
		}
		p.needsAnnotsChecked(false);
		
		p.getFields().stream().forEach((e)->check(e));
		p.getFunctions().stream().forEach((e)->check(e));
		p.getSubpackges().stream().forEach((e)->check(e));
		p.getTypes().stream().forEach((e)->check(e));
	}
	
	public static void check(Function f) {
		if (!f.needsAnnotsChecked()) {
			return;
		}
		f.needsAnnotsChecked(false);
		
		// check for varvarg/varflag
		boolean hasVararg = false, hasVarflag = false;
		for (Parameter p : f.getParams()) {
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
		}
		
		// check for overrides
		List<Annotation> overrides = f.getAnnots(f.tni.corePackage.ANNOT_OVERRIDE);
		for (Annotation override : overrides) {
			if (f.isStatic()) {
				f.tni.errors.add(new AnnotFormatError(override.source, override, "Overrides cannot be static"));
				continue;
			}
			
			for (Parameter p : f.getParams()) {
				if (p.getRawDefaultValue() != null || p.getDefaultValue() != null) {
					f.tni.errors.add(new AnnotFormatError(override.source, override, "Override parameters cannot have default values"));
				}
			}
			
			Type overrideType = f.getFieldOf();
			Function virtualFunc = null;
			
			if (!(overrideType instanceof UserType)) {
				f.tni.errors.add(new AnnotFormatError(override.source, override, "Overrides can only apply to user-defined types"));
				continue;
			}
			
			UserType overrideUserType = (UserType) overrideType;
			List<TypeRef> parents = overrideUserType.getAllParents();
			
			for (TypeRef parent : parents) {
				List<Function> fs = parent.getType().getTypePackage().getFunctions().stream().filter(ff->{
					return !(ff instanceof Constructor) && !ff.isStatic() && !ff.hasAnnot(f.tni.corePackage.ANNOT_OVERRIDE);
				}).collect(Collectors.toList());
				
				funcLoop:
				for (Function memberFunc : fs) {
					if (!memberFunc.getName().equals(f.getName())) continue funcLoop;
					
					if (f.getParams().size() != memberFunc.getParams().size()) continue funcLoop;
					if (f.getRetType().size() != memberFunc.getRetType().size()) continue funcLoop;
					
					int i;
					
					i = 0;
					for (Parameter a : memberFunc.getParams()) {
						Parameter b = f.getParams().get(i);
						
						TypeRef ta = TemplateUtils.replaceTemplates(a.getType(), parent.getTemplateMap(new HashMap<>()));
						TypeRef tb = TemplateUtils.replaceTemplates(b.getType(), parent.getTemplateMap(new HashMap<>()));
						
						if (!ta.equals(tb)) continue funcLoop;
						
						i++;
					}
					
					i = 0;
					for (TypeRef a : memberFunc.getRetType()) {
						TypeRef b = f.getRetType().get(i);
						
						TypeRef ta = TemplateUtils.replaceTemplates(a, parent.getTemplateMap(new HashMap<>()));
						TypeRef tb = TemplateUtils.replaceTemplates(b, parent.getTemplateMap(new HashMap<>()));
						
						if (!ta.equals(tb)) continue funcLoop;
						
						i++;
					}
					
					virtualFunc = memberFunc;
				}
			}
			
			if (virtualFunc == null) {
				// error, virtual func not found
				f.tni.errors.add(new VirtualBaseNotFoundError(override.source, f));
			} else if (virtualFunc.getVirtualOverrides().stream().anyMatch(ff->overrideType.equals(ff.getFieldOf()))) {
				// error, override already exists for this function
				f.tni.errors.add(new DuplicateOverrideError(override.source, virtualFunc, f, overrideType));
			} else {
				Function.setOverride(virtualFunc, f);
			}
		}
		
		// check for getter
		List<Annotation> getters = f.getAnnots(f.tni.corePackage.ANNOT_GETTER);
		
		if (getters.size() > 1) {
			f.tni.errors.add(new AnnotFormatError(getters.get(1).source, getters.get(1), "cannot be annotated multiple times"));
		}
		
		if (!getters.isEmpty()) {
			Annotation annot = getters.get(0);
			
			Field field = f.getParent().getField(f.getName());
			
			if (field == null) {
				field = new Field(f.getName(), f.getRetType().isEmpty() ? new TypeRef(f.tni.corePackage.TYPE_ANY) : f.getRetType().get(0));
				f.getParent().addField(field);
				if (f.isStatic()) field.getAnnots().add(new Annotation(annot.source, f.tni.corePackage.ANNOT_STATIC));
			}
			
			if (!field.isActualField() && field.getGetter() != null) {
				f.tni.errors.add(new AnnotFormatError(annot.source, annot, "field already has a getter"));
			}
			
			if (f.isStatic() != field.isStatic()) {
				f.tni.errors.add(new AnnotFormatError(annot.source, annot, "the field and the getter function must either be both static members or both instance members"));
			}
			
			if (f.getRetType().size() != 1 || !f.getRetType().get(0).canCastTo(field.getType())) {
				f.tni.errors.add(new AnnotFormatError(annot.source, annot, "the getter function must return one value of type "+field.getType().getName()));
			}
			
			if (f.getParams().size() != 0) {
				f.tni.errors.add(new AnnotFormatError(annot.source, annot, "the getter function must have no parameters"));
			}
			
			field.setGetter(f);
		}
		
		// check for setter
		List<Annotation> setters = f.getAnnots(f.tni.corePackage.ANNOT_SETTER);
		
		if (setters.size() > 1) {
			f.tni.errors.add(new AnnotFormatError(setters.get(1).source, setters.get(1), "cannot be annotated multiple times"));
		}
		
		if (!setters.isEmpty()) {
			Annotation annot = setters.get(0);
			
			Field field = f.getParent().getField(f.getName());
			
			if (field == null) {
				field = new Field(f.getName(), f.getParams().isEmpty() ? new TypeRef(f.tni.corePackage.TYPE_ANY) : f.getParams().get(0).getType());
				f.getParent().addField(field);
				if (f.isStatic()) field.getAnnots().add(new Annotation(annot.source, f.tni.corePackage.ANNOT_STATIC));
			}
			
			if (!field.isActualField() && field.getSetter() != null) {
				f.tni.errors.add(new AnnotFormatError(annot.source, annot, "field already has a setter"));
			}
			
			if (f.isStatic() != field.isStatic()) {
				f.tni.errors.add(new AnnotFormatError(annot.source, annot, "the field and the setter function must either be both static members or both instance members"));
			}
			
			if (f.getParams().size() != 1 || !f.getParams().get(0).getType().canCastTo(field.getType())) {
				f.tni.errors.add(new AnnotFormatError(annot.source, annot, "the setter function must have one parameter of type "+field.getType().getName()));
			}
			
			if (f.getRetType().size() != 0) {
				f.tni.errors.add(new AnnotFormatError(annot.source, annot, "the setter function must have no return values"));
			}
			
			field.setSetter(f);
		}
		
		// check for equality operator
		if (f.hasAnnot(f.tni.corePackage.LIB_OPS.ANNOT_EQ)) {
			Annotation annot = f.getAnnots(f.tni.corePackage.LIB_OPS.ANNOT_EQ).get(0);
			
			if (f.isStatic()) {
				f.tni.errors.add(new AnnotFormatError(annot.source, annot, "function cannot be static"));
			}
			
			if (f.getRetType().isEmpty()) {
				f.tni.errors.add(new AnnotFormatError(annot.source, annot, "function must return at least one value"));
			} else {
				TypeRef t = f.getRetType().get(0);
				if (t.getType() != f.tni.corePackage.TYPE_BOOL) {
					f.tni.errors.add(new AnnotFormatError(annot.source, annot, "function must return a bool"));
				}
			}
		}
		
		// check for loop operator
		if (f.hasAnnot(f.tni.corePackage.LIB_OPS.ANNOT_LOOP)) {
			Annotation annot = f.getAnnots(f.tni.corePackage.LIB_OPS.ANNOT_LOOP).get(0);
			
			Type fieldOf = f.getFieldOf();
			
			if (fieldOf == null) {
				f.tni.errors.add(new AnnotFormatError(annot.source, annot, "function cannot be static"));
			} else {
				if (!fieldOf.canCastTo(new TypeRef(fieldOf), new TypeRef(f.tni.corePackage.TYPE_ITERABLE))) {
					f.tni.errors.add(new AnnotFormatError(annot.source, annot, "function must be an instance function of type Iterable"));
				}
			}
			
			if (f.getParams().size() != 1) {
				f.tni.errors.add(new AnnotFormatError(annot.source, annot, "function must take in exactly 1 argument"));
			} else {
				TypeRef t = f.getParams().get(0).getType();
				if (!t.canCastTo(new TypeRef(f.tni.corePackage.TYPE_ITERATOR))) {
					f.tni.errors.add(new AnnotFormatError(annot.source, annot, "function must take in a parameter of type Iterator"));
				}
			}
			
			if (f.getRetType().isEmpty()) {
				f.tni.errors.add(new AnnotFormatError(annot.source, annot, "function must return at least one value"));
			}
		}
		
		// check for abstract
		if (f.hasAnnot(f.tni.corePackage.ANNOT_ABSTRACT)) {
			Annotation annot = f.getAnnots(f.tni.corePackage.ANNOT_ABSTRACT).get(0);
			
			if (f.isStatic()) {
				f.tni.errors.add(new AnnotFormatError(annot.source, annot, "function cannot be static"));
			} else {
				if (!f.getFieldOf().hasAnnot(f.tni.corePackage.ANNOT_ABSTRACT)) {
					f.tni.errors.add(new AnnotFormatError(annot.source, annot, "function is not a member of an abstract class"));
				}
			}
			
			if (f.getForm() != Form.STUB) {
				f.tni.errors.add(new AnnotFormatError(annot.source, annot, "function cannot have an implementation"));
			}
			
			if (f.hasAnnot(f.tni.corePackage.ANNOT_OVERRIDE)) {
				f.tni.errors.add(new AnnotFormatError(annot.source, annot, "function cannot be an override"));
			}
		}
	}
	
	public static void check(Field f) {
		if (!f.needsAnnotsChecked()) {
			return;
		}
		f.needsAnnotsChecked(false);
	}
	
	public static void check(Type t) {
		if (!t.needsAnnotsChecked()) {
			return;
		}
		t.needsAnnotsChecked(false);
		
		check(t.getTypePackage());
		
		// if this type isn't abstract, check to make sure all abstract methods are implemented
		if (t instanceof UserType && !t.hasAnnot(t.tni.corePackage.ANNOT_ABSTRACT)) {
			List<TypeRef> parents = ((UserType)t).getAllParents();
			List<Function> abstracts = new ArrayList<>();
			
			// find the abstract functions
			for (TypeRef parent : parents) {
				abstracts.addAll(parent.getType().getTypePackage().getFunctions().stream().filter(ff->
					ff.hasAnnot(t.tni.corePackage.ANNOT_ABSTRACT)
				).collect(Collectors.toList()));
			}
			
			// remove the abstract functions that were implemented
			t.getTypePackage().getFunctions().stream().filter(ff->
				!ff.hasAnnot(t.tni.corePackage.ANNOT_ABSTRACT) && abstracts.contains(ff.getVirtualBase(t))
			).forEach(ff->{
				abstracts.remove(ff.getVirtualBase(t));
			});
			
			for (TypeRef parent : parents) {
				parent.getType().getTypePackage().getFunctions().stream().filter(ff->
					!ff.hasAnnot(t.tni.corePackage.ANNOT_ABSTRACT) && abstracts.contains(ff.getVirtualBase(parent.getType()))
				).forEach(ff->{
					abstracts.remove(ff.getVirtualBase(parent.getType()));
				});
			}
			
			if (!abstracts.isEmpty()) {
				// error; not all abstract methods implemented
				for (Function ff : abstracts) {
					t.tni.errors.add(new AbstractFunctionError(t.source, t, ff));
				}
			}
		}
	}
}
