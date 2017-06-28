package info.iconmaster.typhon.language;

import java.util.ArrayList;
import java.util.List;

import info.iconmaster.typhon.TyphonInput;
import info.iconmaster.typhon.types.Type;
import info.iconmaster.typhon.util.SourceInfo;

public class Package extends TyphonLanguageEntity {
	public Package parent;
	public String name;
	public List<Function> functions = new ArrayList<>();
	public List<Field> fields = new ArrayList<>();
	public List<Package> subpackages = new ArrayList<>();
	public List<Import> imports = new ArrayList<>();
	public List<Type> types = new ArrayList<>();
	
	public Package(TyphonInput tni) {
		super(tni);
	}
	
	public Package(Package parent) {
		super(parent.tni);
		setParent(parent);
	}
	
	public Package(TyphonInput tni, SourceInfo source) {
		super(tni, source);
	}
	
	public Package(Package parent, SourceInfo source) {
		super(parent.tni, source);
		setParent(parent);
	}
	
	public void setParent(Package parent) {
		if (this.parent != null) {
			this.parent.subpackages.remove(this);
		}
		this.parent = parent;
		parent.subpackages.add(this);
	}
}
