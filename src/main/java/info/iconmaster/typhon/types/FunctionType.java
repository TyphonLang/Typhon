package info.iconmaster.typhon.types;

import java.util.ArrayList;
import java.util.List;

import info.iconmaster.typhon.TyphonInput;
import info.iconmaster.typhon.util.SourceInfo;

public class FunctionType extends Type {
	private List<TypeRef> argTypes = new ArrayList<>();
	private List<TypeRef> retTypes = new ArrayList<>();
	private List<TemplateType> template = new ArrayList<>();

	public FunctionType(TyphonInput input) {
		super(input);
	}

	public FunctionType(TyphonInput input, SourceInfo source) {
		super(input, source);
	}

	public List<TypeRef> getArgTypes() {
		return argTypes;
	}

	public List<TypeRef> getRetTypes() {
		return retTypes;
	}

	public List<TemplateType> getTemplate() {
		return template;
	}
	
	@Override
	public List<TemplateType> getMemberTemplate() {
		return getTemplate();
	}
}
