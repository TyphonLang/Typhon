package info.iconmaster.typhon.types;

import java.util.ArrayList;
import java.util.List;

import info.iconmaster.typhon.TyphonInput;
import info.iconmaster.typhon.language.Argument;
import info.iconmaster.typhon.language.TyphonLanguageEntity;
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
	public static class EnumChoice extends TyphonLanguageEntity {
		/**
		 * The choice's name. Must be a valid Typhon identifier.
		 */
		private String name;
		
		/**
		 * The choice's arguments for one of its constructors.
		 */
		private List<Argument> args = new ArrayList<>();
		
		public EnumChoice(TyphonInput tni, SourceInfo source, String name) {
			super(tni, source);
			this.name = name;
		}
		
		public EnumChoice(TyphonInput tni, String name) {
			super(tni);
			this.name = name;
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
}
