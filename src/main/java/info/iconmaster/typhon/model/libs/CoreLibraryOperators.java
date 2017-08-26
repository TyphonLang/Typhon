package info.iconmaster.typhon.model.libs;

import info.iconmaster.typhon.TyphonInput;
import info.iconmaster.typhon.model.Annotation;
import info.iconmaster.typhon.model.AnnotationDefinition;
import info.iconmaster.typhon.model.Function;
import info.iconmaster.typhon.model.Package;
import info.iconmaster.typhon.model.Parameter;
import info.iconmaster.typhon.types.TemplateType;
import info.iconmaster.typhon.types.Type;

/**
 * The core package.
 * This package contains data for built-in types and functions.
 * All packages derive from the core package.
 * The core package cannot have a parent.
 * 
 * @author iconmaster
 *
 */
public class CoreLibraryOperators extends Package {
	
	private AnnotationDefinition makeAnnotDef(String name, Parameter... params) {
		AnnotationDefinition annot = new AnnotationDefinition(tni, name, params);
		addAnnotDef(annot);
		return annot;
	}
	
	private void addOpFunc(Package p, AnnotationDefinition op, Function f) {
		p.addFunction(f);
		Annotation a = new Annotation(tni);
		a.setDefinition(op);
		f.getAnnots().add(a);
	}
	
	public CoreLibraryOperators(TyphonInput tni) {
		super(tni, "operator");
		
		// add the operator annotations
		tni.corePackage.ANNOT_OP_ADD = makeAnnotDef("add", new Parameter[] {});
		tni.corePackage.ANNOT_OP_SUB = makeAnnotDef("sub", new Parameter[] {});
		tni.corePackage.ANNOT_OP_MUL = makeAnnotDef("mul", new Parameter[] {});
		tni.corePackage.ANNOT_OP_DIV = makeAnnotDef("div", new Parameter[] {});
		tni.corePackage.ANNOT_OP_MOD = makeAnnotDef("mod", new Parameter[] {});
		
		// add the operator functions
		{
			TemplateType t = new TemplateType("T", tni.corePackage.TYPE_NUMBER, null);
			addOpFunc(tni.corePackage.TYPE_NUMBER.getTypePackage(), tni.corePackage.ANNOT_OP_ADD, new Function(tni, "add", new TemplateType[] {
					t
			}, new Parameter[] {
					new Parameter(tni, "other", t, false),
			}, new Type[] {
					t,
			}));
		}
		
		{
			TemplateType t = new TemplateType("T", tni.corePackage.TYPE_NUMBER, null);
			addOpFunc(tni.corePackage.TYPE_NUMBER.getTypePackage(), tni.corePackage.ANNOT_OP_SUB, new Function(tni, "sub", new TemplateType[] {
					t
			}, new Parameter[] {
					new Parameter(tni, "other", t, false),
			}, new Type[] {
					t,
			}));
		}
		
		{
			TemplateType t = new TemplateType("T", tni.corePackage.TYPE_NUMBER, null);
			addOpFunc(tni.corePackage.TYPE_NUMBER.getTypePackage(), tni.corePackage.ANNOT_OP_MUL, new Function(tni, "mul", new TemplateType[] {
					t
			}, new Parameter[] {
					new Parameter(tni, "other", t, false),
			}, new Type[] {
					t,
			}));
		}
		
		{
			TemplateType t = new TemplateType("T", tni.corePackage.TYPE_NUMBER, null);
			addOpFunc(tni.corePackage.TYPE_NUMBER.getTypePackage(), tni.corePackage.ANNOT_OP_DIV, new Function(tni, "div", new TemplateType[] {
					t
			}, new Parameter[] {
					new Parameter(tni, "other", t, false),
			}, new Type[] {
					t,
			}));
		}
		
		{
			TemplateType t = new TemplateType("T", tni.corePackage.TYPE_NUMBER, null);
			addOpFunc(tni.corePackage.TYPE_NUMBER.getTypePackage(), tni.corePackage.ANNOT_OP_MOD, new Function(tni, "mod", new TemplateType[] {
					t
			}, new Parameter[] {
					new Parameter(tni, "other", t, false),
			}, new Type[] {
					t,
			}));
		}
	}
}
