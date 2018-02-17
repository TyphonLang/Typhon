package info.iconmaster.typhon.model.libs;

import info.iconmaster.typhon.TyphonInput;
import info.iconmaster.typhon.model.Annotation;
import info.iconmaster.typhon.model.Field;
import info.iconmaster.typhon.model.Function;
import info.iconmaster.typhon.model.Parameter;
import info.iconmaster.typhon.model.TemplateArgument;
import info.iconmaster.typhon.types.TemplateType;
import info.iconmaster.typhon.types.Type;
import info.iconmaster.typhon.types.TypeRef;
import info.iconmaster.typhon.types.UserType;

public class CoreTypeList extends UserType {
	public TemplateType T;
	
	public Field FIELD_SIZE;
	
	public Function FUNC_GET, FUNC_SET, FUNC_SIZE, FUNC_ADD, FUNC_REMOVE;
	
	public CoreTypeList(TyphonInput input) {
		super(input, "List"); markAsLibrary();
		
		getTemplates().add(T = new TemplateType("T", input.corePackage.TYPE_ANY, null));
	}
	
	/**
	 * Called by CorePackage.
	 * This is done because all the types need to be made before these can be made.
	 */
	public void addMembers() {
		getParentTypes().add(new TypeRef(tni.corePackage.TYPE_ITERABLE, new TemplateArgument(T)));
		getAnnots().add(new Annotation(tni.corePackage.ANNOT_ABSTRACT));
		
		getTypePackage().addFunction(FUNC_GET = new Function(tni, "get", new TemplateType[] {
				
		}, new Parameter[] {
				new Parameter(tni, "i", tni.corePackage.TYPE_INT, false)
		}, new Type[] {
				T
		}));
		FUNC_GET.getAnnots().add(new Annotation(tni.corePackage.ANNOT_ABSTRACT));
		FUNC_GET.getAnnots().add(new Annotation(tni.corePackage.LIB_OPS.ANNOT_INDEX_GET));
		
		getTypePackage().addFunction(FUNC_SET = new Function(tni, "set", new TemplateType[] {
				
		}, new Parameter[] {
				new Parameter(tni, "v", T, false),
				new Parameter(tni, "i", tni.corePackage.TYPE_INT, false)
		}, new Type[] {
				
		}));
		FUNC_SET.getAnnots().add(new Annotation(tni.corePackage.ANNOT_ABSTRACT));
		FUNC_SET.getAnnots().add(new Annotation(tni.corePackage.LIB_OPS.ANNOT_INDEX_SET));
		
		getTypePackage().addField(FIELD_SIZE = new Field("size", tni.corePackage.TYPE_INT));
		
		getTypePackage().addFunction(FUNC_SIZE = new Function(tni, "size", new TemplateType[] {
				
		}, new Parameter[] {
				
		}, new Type[] {
				tni.corePackage.TYPE_INT
		}));
		FUNC_SIZE.getAnnots().add(new Annotation(tni.corePackage.ANNOT_ABSTRACT));
		FIELD_SIZE.setGetter(FUNC_SIZE);
		
		getTypePackage().addFunction(FUNC_ADD = new Function(tni, "add", new TemplateType[] {
				
		}, new Parameter[] {
				new Parameter(tni, "v", T, false),
				new Parameter(tni, "i", tni.corePackage.TYPE_INT, true),
		}, new Type[] {
				
		}));
		FUNC_ADD.getAnnots().add(new Annotation(tni.corePackage.ANNOT_ABSTRACT));
		
		getTypePackage().addFunction(FUNC_REMOVE = new Function(tni, "remove", new TemplateType[] {
				
		}, new Parameter[] {
				new Parameter(tni, "i", tni.corePackage.TYPE_INT, true),
		}, new Type[] {
				T
		}));
		FUNC_REMOVE.getAnnots().add(new Annotation(tni.corePackage.ANNOT_ABSTRACT));
		
		// add concrete methods
		// TODO
	}
}
