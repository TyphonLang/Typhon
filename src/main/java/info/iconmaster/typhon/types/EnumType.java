package info.iconmaster.typhon.types;

import java.util.ArrayList;
import java.util.List;

import info.iconmaster.typhon.TyphonInput;
import info.iconmaster.typhon.model.Argument;
import info.iconmaster.typhon.model.MemberAccess;
import info.iconmaster.typhon.model.TyphonModelEntity;
import info.iconmaster.typhon.util.SourceInfo;

/**
 * This is an enumeration type.
 * It can have static fields and methods. It can have constructors, runnable only at initialization.
 * It can only extend types without fields (i.e., interface types). It cannot be extended from.
 * 
 * @author iconmaster
 *
 */
public class EnumType extends UserType {
	/**
	 * One of multiple possible forms of an EnumType.
	 * 
	 * @author iconmaster
	 *
	 */
	public static class EnumChoice extends TyphonModelEntity implements MemberAccess {
		/**
		 * The choice's name. Must be a valid Typhon identifier.
		 */
		private String name;
		
		/**
		 * The choice's arguments for one of its constructors.
		 */
		private List<Argument> args = new ArrayList<>();
		
		/**
		 * The type this choice is a part of.
		 */
		private EnumType parent;
		
		public EnumChoice(TyphonInput tni, SourceInfo source, String name, EnumType parent) {
			super(tni, source);
			this.name = name;
			this.parent = parent;
		}
		
		public EnumChoice(TyphonInput tni, String name, EnumType parent) {
			super(tni);
			this.name = name;
			this.parent = parent;
		}

		/**
		 * @return The choice's name. Must be a valid Typhon identifier.
		 */
		public String getName() {
			return name;
		}

		/**
		 * @return The choice's arguments for one of its constructors.
		 */
		public List<Argument> getArgs() {
			return args;
		}
		
		/**
		 * @return The type this choice is a part of.
		 */
		public EnumType getParent() {
			return parent;
		}
		
		@Override
		public List<MemberAccess> getMembers() {
			return parent.getMembers();
		}

		@Override
		public MemberAccess getMemberParent() {
			// TODO Auto-generated method stub
			return null;
		}
	}
	
	/**
	 * The possible forms this enum can take.
	 */
	private List<EnumChoice> choices = new ArrayList<>();

	public EnumType(TyphonInput input, SourceInfo source, String name) {
		super(input, source, name);
	}

	public EnumType(TyphonInput input, String name) {
		super(input, name);
	}
	
	/**
	 * Overriden to ensure nobody can add templates to an EnumType.
	 */
	@Override
	public List<TemplateType> getTemplates() {
		return new ArrayList<>();
	}

	/**
	 * @return The possible forms this enum can take.
	 */
	public List<EnumChoice> getChoices() {
		return choices;
	}
	
	@Override
	public List<MemberAccess> getMembers() {
		List<MemberAccess> a = super.getMembers();
		
		a.addAll(getChoices());
		
		return a;
	}
}
