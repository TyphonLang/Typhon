package info.iconmaster.typhon.types;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import info.iconmaster.typhon.TyphonInput;
import info.iconmaster.typhon.model.Function;
import info.iconmaster.typhon.model.MemberAccess;
import info.iconmaster.typhon.model.Package;
import info.iconmaster.typhon.model.Parameter;
import info.iconmaster.typhon.util.SourceInfo;
import info.iconmaster.typhon.util.TemplateUtils;

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
		if (b.getType() instanceof AnyType) {
			return true;
		}
		
		if (b.getType() instanceof TemplateType) {
			return canCastTo(a, ((TemplateType)b.getType()).getBaseType());
		}
		
		if (!(b.getType() instanceof FunctionType)) return false;
		
		FunctionType other = ((FunctionType)b.getType());
		if (getArgTypes().size() != other.getArgTypes().size()) return false;
		if (getRetTypes().size() != other.getRetTypes().size()) return false;
		
		FunctionType realA = (FunctionType) TemplateUtils.replaceTemplates(a, TemplateUtils.matchAllTemplateArgs(a)).getType();
		FunctionType realB = (FunctionType) TemplateUtils.replaceTemplates(b, TemplateUtils.matchAllTemplateArgs(b)).getType();
		
		for (int i = 0; i < realA.getArgTypes().size(); i++) {
			if (!realB.getArgTypes().get(i).canCastTo(realA.getArgTypes().get(i))) return false;
		}
		
		for (int i = 0; i < realA.getRetTypes().size(); i++) {
			if (!realA.getRetTypes().get(i).canCastTo(realB.getRetTypes().get(i))) return false;
		}
		
		return true;
	}
	
	@Override
	public List<TemplateType> getMemberTemplate() {
		return getTemplate();
	}
	
	@Override
	public Package getTypePackage() {
		if (typePackage == null) {
			typePackage = new Package(source, null, tni.corePackage) {
				@Override
				public MemberAccess getMemberParent() {
					return FunctionType.this;
				}
			};
			for (TemplateType t : template) {
				typePackage.addType(t);
			}
		}
		
		return typePackage;
	}
	
	/**
	 * The scope that this function type was declared in.
	 */
	private MemberAccess lookupLocation;
	
	@Override
	public MemberAccess getMemberParent() {
		return lookupLocation;
	}
	
	/**
	 * @param lookupLocation The scope that this function type was declared in.
	 */
	public void setLookupLocation(MemberAccess lookupLocation) {
		this.lookupLocation = lookupLocation;
	}
	
	/**
	 * Creates a library function type.
	 * 
	 * @param tni
	 * @param args
	 * @param rets
	 */
	public FunctionType(TyphonInput tni, TypeRef[] args, TypeRef[] rets, TemplateType... templates) {
		super(tni);
		
		getArgTypes().addAll(Arrays.asList(args));
		getRetTypes().addAll(Arrays.asList(rets));
		getTemplate().addAll(Arrays.asList(templates));
		
		markAsLibrary();
	}
	
	/**
	 * Creates a library function type.
	 * 
	 * @param tni
	 * @param args
	 * @param rets
	 */
	public FunctionType(TyphonInput tni, Type[] args, Type[] rets, TemplateType... templates) {
		super(tni);
		
		getArgTypes().addAll(Arrays.asList(args).stream().map((a)->new TypeRef(a)).collect(Collectors.toList()));
		getRetTypes().addAll(Arrays.asList(rets).stream().map((a)->new TypeRef(a)).collect(Collectors.toList()));
		getTemplate().addAll(Arrays.asList(templates));
		
		markAsLibrary();
	}
	
	@Override
	public String prettyPrint() {
		StringBuilder sb = new StringBuilder();
		
		sb.append('(');
		if (!getArgTypes().isEmpty()) {
			for (TypeRef arg : getArgTypes()) {
				sb.append(arg.prettyPrint());
				sb.append(',');
			}
			sb.deleteCharAt(sb.length()-1);
		}
		sb.append(") -> (");
		if (!getRetTypes().isEmpty()) {
			for (TypeRef arg : getRetTypes()) {
				sb.append(arg.prettyPrint());
				sb.append(',');
			}
			sb.deleteCharAt(sb.length()-1);
		}
		sb.append(')');
		
		return sb.toString();
	}
	
	public Function asFunction() {
		Function f = new Function(tni, source, null);
		
		for (TypeRef arg : getArgTypes()) {
			f.getParams().add(new Parameter(tni, null, arg, false));
		}
		
		for (TypeRef ret : getRetTypes()) {
			f.getRetType().add(ret);
		}
		
		return f;
	}
}
