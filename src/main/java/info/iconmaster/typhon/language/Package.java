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

	private Map<String, List<Function>> functions = new HashMap<>();
	private Map<String, Field> fields = new HashMap<>();
	private Map<String, List<Package>> subpackages = new HashMap<>();
	private List<Import> imports = new ArrayList<>();
	private Map<String, Type> types = new HashMap<>();
	private Map<String, List<AnnotationDefinition>> definedAnnots = new HashMap<>();

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
		List<Package> a;
		if (subpackages.containsKey(p.getName())) {
			a = subpackages.get(p.getName());
		} else {
			a = new ArrayList<>();
			subpackages.put(p.getName(), a);
		}

		a.add(p);
	}

	public void removeSubpackage(Package p) {
		if (subpackages.containsKey(p.getName())) {
			List<Package> a = subpackages.get(p.getName());
			a.remove(p);
			if (a.isEmpty()) {
				subpackages.remove(p.getName());
			}
		}
	}

	public List<Package> getSubpackges() {
		return new ArrayList<>(subpackages.values().stream().reduce(new ArrayList<>(), (l1, l2) -> {
			l1.addAll(l2);
			return l1;
		}));
	}

	public List<Package> getSubpackagesWithName(String name) {
		return subpackages.get(name);
	}

	public void addFunction(Function f) {
		List<Function> a;
		if (functions.containsKey(f.getName())) {
			a = functions.get(f.getName());
		} else {
			a = new ArrayList<>();
			functions.put(f.getName(), a);
		}

		a.add(f);
	}

	public void removeFunction(Function f) {
		if (functions.containsKey(f.getName())) {
			List<Function> a = functions.get(f.getName());
			a.remove(f);
			if (a.isEmpty()) {
				functions.remove(f.getName());
			}
		}
	}

	public List<Function> getFunctions() {
		return new ArrayList<>(functions.values().stream().reduce(new ArrayList<>(), (l1, l2) -> {
			l1.addAll(l2);
			return l1;
		}));
	}

	public List<Function> getFunctionsWithName(String name) {
		return functions.get(name);
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

	public Field getField(String name) {
		return fields.get(name);
	}

	public void addType(Type t) {
		if (t.getName() == null)
			throw new NullPointerException("Cannot add a type without a name");
		types.put(t.getName(), t);
	}

	public void removeType(Type t) {
		if (t.getName() == null)
			throw new NullPointerException("Cannot remove a type without a name");
		types.remove(t.getName(), t);
	}

	public List<Type> getTypes() {
		return new ArrayList<>(types.values());
	}

	public void addImport(Import i) {
		imports.add(i);
	}

	public void removeImport(Import i) {
		imports.remove(i);
	}

	public List<Import> getImports() {
		return imports;
	}

	public void addAnnotDef(AnnotationDefinition f) {
		List<AnnotationDefinition> a;
		if (definedAnnots.containsKey(f.getName())) {
			a = definedAnnots.get(f.getName());
		} else {
			a = new ArrayList<>();
			definedAnnots.put(f.getName(), a);
		}

		a.add(f);
	}

	public void removeAnnotDef(AnnotationDefinition f) {
		if (definedAnnots.containsKey(f.getName())) {
			List<AnnotationDefinition> a = definedAnnots.get(f.getName());
			a.remove(f);
			if (a.isEmpty()) {
				definedAnnots.remove(f.getName());
			}
		}
	}

	public List<AnnotationDefinition> getAnnotDefs() {
		return new ArrayList<>(definedAnnots.values().stream().reduce(new ArrayList<>(), (l1, l2) -> {
			l1.addAll(l2);
			return l1;
		}));
	}

	public List<AnnotationDefinition> getAnnotDefsWithName(String name) {
		return definedAnnots.get(name);
	}
}
