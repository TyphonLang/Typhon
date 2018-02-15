package info.iconmaster.typhon.types;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import info.iconmaster.typhon.TyphonInput;
import info.iconmaster.typhon.util.SourceInfo;

/**
 * This is a category of types that can have parent types.
 * By default, this includes UserType, SystemType, and ComboType.
 * 
 * @author iconmaster
 *
 */
public abstract class ExtendableType extends Type {
	/**
	 * The parent type. Cannot be null.
	 */
	private List<TypeRef> parentTypes = new ArrayList<>();

	public ExtendableType(TyphonInput input, SourceInfo source) {
		super(input, source);
	}

	public ExtendableType(TyphonInput input) {
		super(input);
	}
	
	/**
	 * @param name The name of this type.
	 * @param parentType One or more parent types.
	 */
	public ExtendableType(Type parentType, Type... otherParentTypes) {
		super(parentType.tni);
		getParentTypes().add(new TypeRef(parentType));
		getParentTypes().addAll(Arrays.asList(otherParentTypes).stream().map((t)->new TypeRef(t)).collect(Collectors.toList()));
	}
	
	/**
	 * @param name The name of this type.
	 * @param parentType One or more parent types.
	 */
	public ExtendableType(TypeRef parentType, TypeRef... otherParentTypes) {
		super(parentType.tni);
		getParentTypes().add(parentType);
		getParentTypes().addAll(Arrays.asList(otherParentTypes));
	}
	
	/**
	 * @return The parent types of this type.
	 */
	public List<TypeRef> getParentTypes() {
		return parentTypes;
	}
	
	public List<TypeRef> getAllParents() {
		List<TypeRef> result = new ArrayList<>();
		result.addAll(getParentTypes());
		
		boolean addedToResult = true;
		while (addedToResult) {
			if (addedToResult) {
				addedToResult = false;
			}
			
			for (TypeRef type : new ArrayList<>(result)) {
				if (type.getType() instanceof ExtendableType) {
					for (TypeRef parentType : ((ExtendableType)type.getType()).getParentTypes()) {
						if (!result.contains(parentType)) {
							result.add(parentType);
							addedToResult = true;
						}
					}
				}
			}
		}
		
		return result;
	}
}
