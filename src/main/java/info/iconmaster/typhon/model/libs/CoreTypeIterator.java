package info.iconmaster.typhon.model.libs;

import info.iconmaster.typhon.TyphonInput;
import info.iconmaster.typhon.model.Annotation;
import info.iconmaster.typhon.model.Function;
import info.iconmaster.typhon.model.Parameter;
import info.iconmaster.typhon.model.TemplateArgument;
import info.iconmaster.typhon.types.TemplateType;
import info.iconmaster.typhon.types.Type;
import info.iconmaster.typhon.types.TypeRef;
import info.iconmaster.typhon.types.UserType;

public class CoreTypeIterator extends UserType {
	public TemplateType T;
	
	public Function FUNC_NEXT, FUNC_DONE, FUNC_ITERATOR;
	
	public CoreTypeIterator(TyphonInput input) {
		super(input, "Iterator");
		markAsLibrary();
		
		getTemplates().add(T = new TemplateType("T", input.corePackage.TYPE_ANY, null));
		getParentTypes().add(new TypeRef(tni.corePackage.TYPE_ITERABLE, new TemplateArgument(T)));
	}
	
	/**
	 * Called by CorePackage.
	 * This is done because all the types need to be made before these can be made.
	 */
	public void addMembers() {
		getAnnots().add(new Annotation(tni.corePackage.ANNOT_ABSTRACT));
		
		getTypePackage().addFunction(FUNC_NEXT = new Function(tni, "next", new TemplateType[] {
				
		}, new Parameter[] {
				
		}, new Type[] {
				T
		}));
		FUNC_NEXT.getAnnots().add(new Annotation(tni.corePackage.ANNOT_ABSTRACT));
		
		getTypePackage().addFunction(FUNC_DONE = new Function(tni, "done", new TemplateType[] {
				
		}, new Parameter[] {
				
		}, new Type[] {
				tni.corePackage.TYPE_BOOL
		}));
		FUNC_DONE.getAnnots().add(new Annotation(tni.corePackage.ANNOT_ABSTRACT));
		
		getTypePackage().addFunction(FUNC_ITERATOR = new Function(tni, "iterator", new TemplateType[] {
				
		}, new Parameter[] {
				
		}, new TypeRef[] {
				new TypeRef(tni.corePackage.TYPE_ITERATOR, new TemplateArgument(T))
		}));
		Function.setOverride(tni.corePackage.TYPE_ITERABLE.FUNC_ITERATOR, FUNC_ITERATOR);
	}
}
