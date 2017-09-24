package info.iconmaster.typhon.model.libs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import info.iconmaster.typhon.TyphonInput;
import info.iconmaster.typhon.model.Annotation;
import info.iconmaster.typhon.model.AnnotationDefinition;
import info.iconmaster.typhon.model.Function;
import info.iconmaster.typhon.model.Package;
import info.iconmaster.typhon.model.Parameter;
import info.iconmaster.typhon.types.TemplateType;
import info.iconmaster.typhon.types.Type;

/**
 * This package contains functions and annotations dealing with the builtin operators and overloading them.
 * 
 * @author iconmaster
 *
 */
public class CoreLibraryOperators extends Package {
	
	/**
	 * Constants for operator annotations.
	 */
	public AnnotationDefinition ANNOT_ADD, ANNOT_SUB, ANNOT_MUL, ANNOT_DIV, ANNOT_MOD,
	ANNOT_BAND, ANNOT_BOR, ANNOT_XOR, ANNOT_SHL, ANNOT_SHR,
	ANNOT_LT, ANNOT_LE, ANNOT_GT, ANNOT_GE,
	ANNOT_NEG, ANNOT_POS, ANNOT_BNOT,
	ANNOT_EQ;
	
	/**
	 * A list of various operator functions.
	 */
	public Map<AnnotationDefinition, List<Function>> OP_FUNCS = new HashMap<>();
	
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
		
		if (!OP_FUNCS.containsKey(op)) {
			OP_FUNCS.put(op, new ArrayList<>());
		}
		OP_FUNCS.get(op).add(f);
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
	
	private void addRelOpFunc(AnnotationDefinition op) {
		CorePackage c = tni.corePackage;
		
		// add the general version
		addOpFunc(c.TYPE_NUMBER.getTypePackage(), op, new Function(tni, op.getName(), new TemplateType[] {
				
		}, new Parameter[] {
				new Parameter(tni, "other", c.TYPE_NUMBER, false),
		}, new Type[] {
				c.TYPE_BOOL,
		}));
	}
	
	public CoreLibraryOperators(TyphonInput tni) {
		super(tni, "operator");
		
		// add the operator annotations
		ANNOT_ADD = makeAnnotDef("add");
		ANNOT_SUB = makeAnnotDef("sub");
		ANNOT_MUL = makeAnnotDef("mul");
		ANNOT_DIV = makeAnnotDef("div");
		ANNOT_MOD = makeAnnotDef("mod");
		
		ANNOT_BAND = makeAnnotDef("band");
		ANNOT_BOR = makeAnnotDef("bor");
		ANNOT_XOR = makeAnnotDef("xor");
		
		ANNOT_SHL = makeAnnotDef("shl");
		ANNOT_SHR = makeAnnotDef("shr");
		
		ANNOT_LT = makeAnnotDef("lt");
		ANNOT_LE = makeAnnotDef("le");
		ANNOT_GT = makeAnnotDef("gt");
		ANNOT_GE = makeAnnotDef("ge");
		
		ANNOT_NEG = makeAnnotDef("neg");
		ANNOT_POS = makeAnnotDef("pos");
		ANNOT_BNOT = makeAnnotDef("bnot");
		
		ANNOT_EQ = makeAnnotDef("eq");
		
		// add the operator functions
		addBinOpFunc(ANNOT_ADD);
		addBinOpFunc(ANNOT_SUB);
		addBinOpFunc(ANNOT_MUL);
		addBinOpFunc(ANNOT_DIV);
		addBinOpFunc(ANNOT_MOD);
		
		addBitOpFunc(ANNOT_BAND);
		addBitOpFunc(ANNOT_BOR);
		addBitOpFunc(ANNOT_XOR);
		
		addBitShiftOpFunc(ANNOT_SHL);
		addBitShiftOpFunc(ANNOT_SHR);
		
		addRelOpFunc(ANNOT_LT);
		addRelOpFunc(ANNOT_LE);
		addRelOpFunc(ANNOT_GT);
		addRelOpFunc(ANNOT_GE);
		
		{
			// add unary -
			
			CorePackage c = tni.corePackage;
			Type[] types = new Type[] {c.TYPE_UBYTE, c.TYPE_BYTE, c.TYPE_USHORT, c.TYPE_SHORT, c.TYPE_UINT, c.TYPE_INT, c.TYPE_ULONG, c.TYPE_LONG, c.TYPE_DOUBLE, c.TYPE_FLOAT};
			Type[] rtypes = new Type[] {c.TYPE_BYTE, c.TYPE_BYTE, c.TYPE_SHORT, c.TYPE_SHORT, c.TYPE_INT, c.TYPE_INT, c.TYPE_LONG, c.TYPE_LONG, c.TYPE_DOUBLE, c.TYPE_FLOAT};
			
			// add the specific versions
			int i = 0;
			for (Type t : types) {
				addOpFunc(t.getTypePackage(), ANNOT_NEG, new Function(tni, ANNOT_NEG.getName(), new TemplateType[] {
						
				}, new Parameter[] {
						
				}, new Type[] {
						rtypes[i],
				}));
				i++;
			}
			
			// add the general version
			addOpFunc(c.TYPE_NUMBER.getTypePackage(), ANNOT_NEG, new Function(tni, ANNOT_NEG.getName(), new TemplateType[] {
					
			}, new Parameter[] {
					
			}, new Type[] {
					c.TYPE_NUMBER,
			}));
		}
		
		{
			// add unary +
			
			CorePackage c = tni.corePackage;
			Type[] types = new Type[] {c.TYPE_UBYTE, c.TYPE_BYTE, c.TYPE_USHORT, c.TYPE_SHORT, c.TYPE_UINT, c.TYPE_INT, c.TYPE_ULONG, c.TYPE_LONG, c.TYPE_DOUBLE, c.TYPE_FLOAT};
			
			// add the specific versions
			for (Type t : types) {
				addOpFunc(t.getTypePackage(), ANNOT_POS, new Function(tni, ANNOT_POS.getName(), new TemplateType[] {
						
				}, new Parameter[] {
						
				}, new Type[] {
						t,
				}));
			}
			
			// add the general version
			addOpFunc(c.TYPE_NUMBER.getTypePackage(), ANNOT_POS, new Function(tni, ANNOT_POS.getName(), new TemplateType[] {
					
			}, new Parameter[] {
					
			}, new Type[] {
					c.TYPE_NUMBER,
			}));
		}
		
		{
			// add unary bit-not
			
			CorePackage c = tni.corePackage;
			Type[] types = new Type[] {c.TYPE_UBYTE, c.TYPE_BYTE, c.TYPE_USHORT, c.TYPE_SHORT, c.TYPE_UINT, c.TYPE_INT, c.TYPE_ULONG, c.TYPE_LONG};
			
			// add the specific versions
			for (Type t : types) {
				addOpFunc(t.getTypePackage(), ANNOT_BNOT, new Function(tni, ANNOT_BNOT.getName(), new TemplateType[] {
						
				}, new Parameter[] {
						
				}, new Type[] {
						t,
				}));
			}
			
			// add the general version
			addOpFunc(c.TYPE_INTEGER.getTypePackage(), ANNOT_BNOT, new Function(tni, ANNOT_BNOT.getName(), new TemplateType[] {
					
			}, new Parameter[] {
					
			}, new Type[] {
					c.TYPE_INTEGER,
			}));
		}
		
		{
			// add equality
			CorePackage c = tni.corePackage;
			
			addOpFunc(c.TYPE_ANY.getTypePackage(), ANNOT_EQ, new Function(tni, ANNOT_EQ.getName(), new TemplateType[] {
					
			}, new Parameter[] {
					new Parameter(tni, "other", c.TYPE_ANY, false),
			}, new Type[] {
					c.TYPE_BOOL,
			}));
		}
	}
}
