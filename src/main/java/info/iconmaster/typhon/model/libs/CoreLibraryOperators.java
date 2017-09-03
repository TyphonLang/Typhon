package info.iconmaster.typhon.model.libs;

import info.iconmaster.typhon.TyphonInput;
import info.iconmaster.typhon.model.Annotation;
import info.iconmaster.typhon.model.AnnotationDefinition;
import info.iconmaster.typhon.model.CorePackage;
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
	
	private void addBinOpFunc(AnnotationDefinition op) {
		CorePackage c = tni.corePackage;
		Type[] types = new Type[] {c.TYPE_UBYTE, c.TYPE_BYTE, c.TYPE_USHORT, c.TYPE_SHORT, c.TYPE_UINT, c.TYPE_INT, c.TYPE_ULONG, c.TYPE_LONG, c.TYPE_DOUBLE, c.TYPE_FLOAT};
		
		// add the specific versions
		for (Type t : types) {
			addOpFunc(t.getTypePackage(), op, new Function(tni, op.getName(), new TemplateType[] {
					
			}, new Parameter[] {
					new Parameter(tni, "other", t, false),
			}, new Type[] {
					t,
			}));
		}
		
		// add the type-extension versions
		for (int i = 0; i < types.length; i++) {
			for (int j = 0; j < types.length; j++) {
				if (i == j) continue;
				Type t1 = types[i];
				Type t2 = types[j];
				
				addOpFunc(t1.getTypePackage(), op, new Function(tni, op.getName(), new TemplateType[] {
						
				}, new Parameter[] {
						new Parameter(tni, "other", t2, false),
				}, new Type[] {
						i > j ? t1 : t2,
				}));
			}
		}
		
		// add the general version
		addOpFunc(c.TYPE_NUMBER.getTypePackage(), op, new Function(tni, op.getName(), new TemplateType[] {
				
		}, new Parameter[] {
				new Parameter(tni, "other", c.TYPE_NUMBER, false),
		}, new Type[] {
				c.TYPE_NUMBER,
		}));
	}
	
	private void addBitOpFunc(AnnotationDefinition op) {
		CorePackage c = tni.corePackage;
		Type[] types = new Type[] {c.TYPE_UBYTE, c.TYPE_BYTE, c.TYPE_USHORT, c.TYPE_SHORT, c.TYPE_UINT, c.TYPE_INT, c.TYPE_ULONG, c.TYPE_LONG};
		
		// add the specific versions
		for (Type t : types) {
			addOpFunc(t.getTypePackage(), op, new Function(tni, op.getName(), new TemplateType[] {
					
			}, new Parameter[] {
					new Parameter(tni, "other", t, false),
			}, new Type[] {
					t,
			}));
		}
		
		// add the type-extension versions
		for (int i = 0; i < types.length; i++) {
			for (int j = 0; j < types.length; j++) {
				if (i == j) continue;
				Type t1 = types[i];
				Type t2 = types[j];
				
				addOpFunc(t1.getTypePackage(), op, new Function(tni, op.getName(), new TemplateType[] {
						
				}, new Parameter[] {
						new Parameter(tni, "other", t2, false),
				}, new Type[] {
						i > j ? t1 : t2,
				}));
			}
		}
		
		// add the general version
		addOpFunc(c.TYPE_INTEGER.getTypePackage(), op, new Function(tni, op.getName(), new TemplateType[] {
				
		}, new Parameter[] {
				new Parameter(tni, "other", c.TYPE_INTEGER, false),
		}, new Type[] {
				c.TYPE_INTEGER,
		}));
	}
	
	private void addBitShiftOpFunc(AnnotationDefinition op) {
		CorePackage c = tni.corePackage;
		Type[] types = new Type[] {c.TYPE_UBYTE, c.TYPE_BYTE, c.TYPE_USHORT, c.TYPE_SHORT, c.TYPE_UINT, c.TYPE_INT, c.TYPE_ULONG, c.TYPE_LONG};
		
		// add the specific versions
		for (Type t : types) {
			addOpFunc(t.getTypePackage(), op, new Function(tni, op.getName(), new TemplateType[] {
					
			}, new Parameter[] {
					new Parameter(tni, "shiftBy", c.TYPE_INTEGER, false),
			}, new Type[] {
					t,
			}));
		}
		
		// add the general version
		addOpFunc(c.TYPE_INTEGER.getTypePackage(), op, new Function(tni, op.getName(), new TemplateType[] {
				
		}, new Parameter[] {
				new Parameter(tni, "shiftBy", c.TYPE_INTEGER, false),
		}, new Type[] {
				c.TYPE_INTEGER,
		}));
	}
	
	public CoreLibraryOperators(TyphonInput tni) {
		super(tni, "operator");
		
		// add the operator annotations
		tni.corePackage.ANNOT_OP_ADD = makeAnnotDef("add");
		tni.corePackage.ANNOT_OP_SUB = makeAnnotDef("sub");
		tni.corePackage.ANNOT_OP_MUL = makeAnnotDef("mul");
		tni.corePackage.ANNOT_OP_DIV = makeAnnotDef("div");
		tni.corePackage.ANNOT_OP_MOD = makeAnnotDef("mod");
		
		tni.corePackage.ANNOT_OP_BAND = makeAnnotDef("band");
		tni.corePackage.ANNOT_OP_BOR = makeAnnotDef("bor");
		tni.corePackage.ANNOT_OP_XOR = makeAnnotDef("xor");
		
		tni.corePackage.ANNOT_OP_SHL = makeAnnotDef("shl");
		tni.corePackage.ANNOT_OP_SHR = makeAnnotDef("shr");
		
		// add the operator functions
		addBinOpFunc(tni.corePackage.ANNOT_OP_ADD);
		addBinOpFunc(tni.corePackage.ANNOT_OP_SUB);
		addBinOpFunc(tni.corePackage.ANNOT_OP_MUL);
		addBinOpFunc(tni.corePackage.ANNOT_OP_DIV);
		addBinOpFunc(tni.corePackage.ANNOT_OP_MOD);
		
		addBitOpFunc(tni.corePackage.ANNOT_OP_BAND);
		addBitOpFunc(tni.corePackage.ANNOT_OP_BOR);
		addBitOpFunc(tni.corePackage.ANNOT_OP_XOR);
		
		addBitShiftOpFunc(tni.corePackage.ANNOT_OP_SHL);
		addBitShiftOpFunc(tni.corePackage.ANNOT_OP_SHR);
	}
}
