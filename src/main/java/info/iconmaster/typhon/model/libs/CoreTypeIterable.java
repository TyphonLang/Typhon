package info.iconmaster.typhon.model.libs;

import info.iconmaster.typhon.TyphonInput;
import info.iconmaster.typhon.model.Annotation;
import info.iconmaster.typhon.model.Function;
import info.iconmaster.typhon.model.Parameter;
import info.iconmaster.typhon.model.TemplateArgument;
import info.iconmaster.typhon.types.TemplateType;
import info.iconmaster.typhon.types.TypeRef;
import info.iconmaster.typhon.types.UserType;

public class CoreTypeIterable extends UserType {
	public TemplateType T;
	
	public Function FUNC_ITERATOR;
	
	public CoreTypeIterable(TyphonInput input) {
		super("Iterable", input.corePackage.TYPE_ANY);
		getTemplates().add(T = new TemplateType("T", input.corePackage.TYPE_ANY, null));
	}
	
	/**
	 * Called by CorePackage.
	 * This is done because all the types need to be made before these can be made.
	 */
	public void addMembers() {
		getAnnots().add(new Annotation(tni.corePackage.ANNOT_ABSTRACT));
		
		getTypePackage().addFunction(FUNC_ITERATOR = new Function(tni, "iterator", new TemplateType[] {
				
		}, new Parameter[] {
				
		}, new TypeRef[] {
				new TypeRef(tni.corePackage.TYPE_ITERATOR, new TemplateArgument(T))
		}));
		FUNC_ITERATOR.getAnnots().add(new Annotation(tni.corePackage.ANNOT_ABSTRACT));
	}
}
