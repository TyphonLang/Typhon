package info.iconmaster.typhon.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import info.iconmaster.typhon.TyphonInput;
import info.iconmaster.typhon.antlr.TyphonParser.TypeContext;
import info.iconmaster.typhon.types.TypeRef;
import info.iconmaster.typhon.util.SourceInfo;

public class Constructor extends Function {

	/**
	 * Parameters for constructors are similar to regular ones, except they can also refer to a field in the type.
	 * 
	 * @author iconmaster
	 *
	 */
	public static class ConstructorParameter extends Parameter {
		/**
		 * True if this is a field rather than a local variable.
		 */
		private boolean isField;
		
		public ConstructorParameter(TyphonInput input) {
			super(input);
		}
		
		public ConstructorParameter(TyphonInput input, SourceInfo source) {
			super(input, source);
		}

		/**
		 * @return True if this is a field rather than a local variable.
		 */
		public boolean isField() {
			return isField;
		}
		
		/**
		 * @param isField True if this is a field rather than a local variable.
		 */
		public void isField(boolean isField) {
			this.isField = isField;
		}
	}
	
	/**
	 * The parameters for this constructor.
	 */
	private List<ConstructorParameter> constParams = new ArrayList<>();
	
	public Constructor(TyphonInput input) {
		super(input, "new");
	}

	public Constructor(TyphonInput input, SourceInfo source) {
		super(input, source, "new");
	}

	/**
	 * @return The parameters for this constructor.
	 */
	@Override
	public List<Parameter> getParams() {
		return (List<Parameter>) (List) getConstParams();
	}

	/**
	 * @return The parameters for this constructor.
	 */
	public List<ConstructorParameter> getConstParams() {
		return constParams;
	}
	
	/**
	 * Sets the raw ANTLR data for this constructor.
	 * 
	 * @param form The form of the constructor as it was declared.
	 * @param rawCode The ANTLR rule representing the constructor's code. See {@link Form} for details.
	 */
	public void setRawData(Form form, List<?> rawCode) {
		super.setRawData(Arrays.asList(), form, rawCode);
	}
}
