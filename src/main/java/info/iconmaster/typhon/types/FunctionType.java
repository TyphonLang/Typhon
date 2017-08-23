package info.iconmaster.typhon.types;

import java.util.ArrayList;
import java.util.List;

import info.iconmaster.typhon.TyphonInput;
import info.iconmaster.typhon.model.MemberAccess;
import info.iconmaster.typhon.model.Package;
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
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((argTypes == null) ? 0 : argTypes.hashCode());
		result = prime * result + ((retTypes == null) ? 0 : retTypes.hashCode());
		result = prime * result + ((template == null) ? 0 : template.hashCode());
		return result;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		FunctionType other = (FunctionType) obj;
		if (argTypes == null) {
			if (other.argTypes != null)
				return false;
		} else if (!argTypes.equals(other.argTypes))
			return false;
		if (retTypes == null) {
			if (other.retTypes != null)
				return false;
		} else if (!retTypes.equals(other.retTypes))
			return false;
		if (template == null) {
			if (other.template != null)
				return false;
		} else if (!template.equals(other.template))
			return false;
		return true;
	}
	
	@Override
	public boolean canCastTo(TypeRef a, TypeRef b) {
		// TODO: function types need to be castable to each other
		return super.canCastTo(a, b);
	}
	
	@Override
	public List<TemplateType> getMemberTemplate() {
		return getTemplate();
	}
	
	@Override
	public Package getTypePackage() {
		if (typePackage == null) {
			typePackage = new Package(source, null, tni.corePackage);
			for (TemplateType t : template) {
				typePackage.addType(t);
			}
		}
		
		return typePackage;
	}
}
