package info.iconmaster.typhon.language;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import info.iconmaster.typhon.TyphonInput;
import info.iconmaster.typhon.types.Type;
import info.iconmaster.typhon.util.SourceInfo;

public class Package extends TyphonLanguageEntity {
	private Package parent;
	private String name;
	
	private List<Function> functions = new ArrayList<>();
	private Map<String,Field> fields = new HashMap<>();
	private Map<String,Package> subpackages = new HashMap<>();
	private List<Import> imports = new ArrayList<>();
	private Map<String,Type> types = new HashMap<>();
	
	public Package(TyphonInput tni, String name) {
		super(tni);
		this.name = name;
	}
	
	public Package(String name, Package parent) {
		super(parent.tni);
		this.name = name;
		setParent(parent);
	}
	
	public Package(TyphonInput tni, SourceInfo source, String name) {
		super(tni, source);
		this.name = name;
	}
	
	public Package(SourceInfo source, String name, Package parent) {
		super(parent != null ? parent.tni : null, source);
		this.name = name;
		setParent(parent);
	}
	
	public String getName() {
		return name;
	}
	
	public Package getParent() {
		return parent;
	}
	
	public void setParent(Package parent) {
		if (this.parent != null) {
			this.parent.removeSubpackage(this);
		}
		this.parent = parent;
		if (this.parent != null) {
			parent.addSubpackage(this);
		}
	}
	
	public void addSubpackage(Package p) {
		subpackages.put(p.getName(), p);
	}
	
	public void removeSubpackage(Package p) {
		subpackages.remove(p.getName(), p);
	}
	
	public List<Package> getSubpackges() {
		return new ArrayList<>(subpackages.values());
	}
	
	public void addFunction(Function f) {
		functions.add(f);
	}
	
	public void removeFunction(Function f) {
		functions.remove(f);
	}
	
	public List<Function> getFunctions() {
		return functions;
	}
	
	public void addField(Field f) {
		fields.put(f.name, f);
	}
	
	public void removeField(Field f) {
		fields.remove(f.name, f);
	}
	
	public List<Field> getFields() {
		return new ArrayList<>(fields.values());
	}
	
	public void addType(Type t) {
		if (t.getName() == null) throw new NullPointerException("Cannot add a type without a name");
		types.put(t.getName(), t);
	}
	
	public void removeType(Type t) {
		if (t.getName() == null) throw new NullPointerException("Cannot remove a type without a name");
		types.remove(t.getName(), t);
	}
	
	public List<Type> getTypes() {
		return new ArrayList<>(types.values());
	}
	
	public List<Import> getImports() {
		return imports;
	}
}
