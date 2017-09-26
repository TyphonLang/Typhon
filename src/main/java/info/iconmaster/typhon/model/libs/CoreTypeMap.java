package info.iconmaster.typhon.model.libs;

import info.iconmaster.typhon.TyphonInput;
import info.iconmaster.typhon.model.Annotation;
import info.iconmaster.typhon.model.Constructor;
import info.iconmaster.typhon.model.Constructor.ConstructorParameter;
import info.iconmaster.typhon.model.Field;
import info.iconmaster.typhon.model.Function;
import info.iconmaster.typhon.model.Parameter;
import info.iconmaster.typhon.model.TemplateArgument;
import info.iconmaster.typhon.types.TemplateType;
import info.iconmaster.typhon.types.Type;
import info.iconmaster.typhon.types.TypeRef;
import info.iconmaster.typhon.types.UserType;

public class CoreTypeMap extends UserType {
	public static class CoreTypeEntry extends UserType {
		public TemplateType K, V;
		
		public Constructor FUNC_NEW;
		
		public Field FIELD_KEY, FIELD_VALUE;
		
		public CoreTypeEntry(CoreTypeMap map) {
			super("Entry", map.tni.corePackage.TYPE_ANY); markAsLibrary();
			
			getTemplates().add(K = new TemplateType("K", map.tni.corePackage.TYPE_ANY, null));
			getTemplates().add(V = new TemplateType("V", map.tni.corePackage.TYPE_ANY, null));
		}
	}
	
	public TemplateType K, V;
	
	public Field FIELD_KEYS;
	
	public Function FUNC_GET, FUNC_SET, FUNC_KEYS;
	
	public CoreTypeEntry TYPE_ENTRY;
	
	public CoreTypeMap(TyphonInput input) {
		super(input, "Map"); markAsLibrary();
		
		getTemplates().add(K = new TemplateType("K", input.corePackage.TYPE_ANY, null));
		getTemplates().add(V = new TemplateType("V", input.corePackage.TYPE_ANY, null));
		
		getTypePackage().addType(TYPE_ENTRY = new CoreTypeEntry(this));
	}
	
	/**
	 * Called by CorePackage.
	 * This is done because all the types need to be made before these can be made.
	 */
	public void addMembers() {
		getParentTypes().add(new TypeRef(tni.corePackage.TYPE_ITERABLE, new TemplateArgument(new TypeRef(TYPE_ENTRY, new TemplateArgument(K), new TemplateArgument(V)))));
		getAnnots().add(new Annotation(tni.corePackage.ANNOT_ABSTRACT));
		
		// add abstract methods
		getTypePackage().addFunction(FUNC_GET = new Function(tni, "get", new TemplateType[] {
				
		}, new Parameter[] {
				new Parameter(tni, "key", K, false)
		}, new Type[] {
				V
		}));
		FUNC_GET.getAnnots().add(new Annotation(tni.corePackage.ANNOT_ABSTRACT));
		FUNC_GET.getAnnots().add(new Annotation(tni.corePackage.LIB_OPS.ANNOT_INDEX_GET));
		
		getTypePackage().addFunction(FUNC_SET = new Function(tni, "set", new TemplateType[] {
				
		}, new Parameter[] {
				new Parameter(tni, "value", V, false),
				new Parameter(tni, "key", K, false)
		}, new Type[] {
				
		}));
		FUNC_SET.getAnnots().add(new Annotation(tni.corePackage.ANNOT_ABSTRACT));
		FUNC_SET.getAnnots().add(new Annotation(tni.corePackage.LIB_OPS.ANNOT_INDEX_SET));
		
		getTypePackage().addField(FIELD_KEYS = new Field("keys", new TypeRef(tni.corePackage.TYPE_ITERABLE, new TemplateArgument(K))));
		
		getTypePackage().addFunction(FUNC_KEYS = new Function(tni, "keys", new TemplateType[] {
				
		}, new Parameter[] {
				
		}, new Type[] {
				tni.corePackage.TYPE_INT
		}));
		FUNC_KEYS.getAnnots().add(new Annotation(tni.corePackage.ANNOT_ABSTRACT));
		FIELD_KEYS.setGetter(FUNC_KEYS);
		
		// add concrete methods
		// TODO
		
		// add methods for Entry
		TYPE_ENTRY.getTypePackage().addField(TYPE_ENTRY.FIELD_KEY = new Field("key", TYPE_ENTRY.K));
		TYPE_ENTRY.getTypePackage().addField(TYPE_ENTRY.FIELD_VALUE = new Field("value", TYPE_ENTRY.V));
		
		TYPE_ENTRY.getTypePackage().addFunction(TYPE_ENTRY.FUNC_NEW = new Constructor(TYPE_ENTRY, ConstructorParameter.fieldParam(TYPE_ENTRY.FIELD_KEY), ConstructorParameter.fieldParam(TYPE_ENTRY.FIELD_VALUE)));
	}
}
