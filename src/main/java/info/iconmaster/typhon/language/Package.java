package info.iconmaster.typhon.language;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import info.iconmaster.typhon.TyphonInput;
import info.iconmaster.typhon.types.Type;
import info.iconmaster.typhon.util.SourceInfo;

/**
 * A Typhon package contains declarations of all sorts.
 * They have a parent and zero or more children, allowing for a package tree.
 * 
 * @author iconmaster
 *
 */
public class Package extends TyphonLanguageEntity {
	/**
	 * This package's parent.
	 */
	private Package parent;
	
	/**
	 * This package's name. Cannot be null.
	 */
	private String name;

	/**
	 * The functions this package exposes, grouped by name.
	 */
	private Map<String, List<Function>> functions = new HashMap<>();
	
	/**
	 * The fields this package exposes, grouped by name.
	 */
	private Map<String, Field> fields = new HashMap<>();
	
	/**
	 * The child packages this package exposes, grouped by name.
	 */
	private Map<String, List<Package>> subpackages = new HashMap<>();
	
	/**
	 * The imports this package requires.
	 */
	private List<Import> imports = new ArrayList<>();
	
	/**
	 * The types this package exposes, grouped by name.
	 */
	private Map<String, Type> types = new HashMap<>();
	
	/**
	 * The annotations this package exposes, grouped by name.
	 */
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

	/**
	 * @return This package's name. Cannot be null.
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return This package's parent.
	 */
	public Package getParent() {
		return parent;
	}

	/**
	 * @return This package's new parent.
	 */
	public void setParent(Package parent) {
		if (this.parent != null) {
			this.parent.removeSubpackage(this);
		}
		this.parent = parent;
		if (this.parent != null) {
			parent.addSubpackage(this);
		}
	}

	/**
	 * Adds a child package.
	 * @param p The package to add. Cannot be null.
	 */
	public void addSubpackage(Package p) {
		List<Package> a;
		if (subpackages.containsKey(p.getName())) {
			a = subpackages.get(p.getName());
		} else {
			a = new ArrayList<>();
			subpackages.put(p.getName(), a);
		}

		p.setParent(this);
		a.add(p);
	}

	/**
	 * Removes a child package.
	 * @param p The package to remove. Cannot be null.
	 */
	public void removeSubpackage(Package p) {
		if (subpackages.containsKey(p.getName())) {
			List<Package> a = subpackages.get(p.getName());
			a.remove(p);
			if (a.isEmpty()) {
				subpackages.remove(p.getName());
			}
		}
		
		p.setParent(null);
	}

	/**
	 * Lists the child packages.
	 * Note that multiple packages may have the same name.
	 * Do not modify the list this function returns!
	 * 
	 * @return The list of child packages.
	 */
	public List<Package> getSubpackges() {
		return new ArrayList<>(subpackages.values().stream().reduce(new ArrayList<>(), (l1, l2) -> {
			l1.addAll(l2);
			return l1;
		}));
	}

	/**
	 * Lists the child packages with a given name.
	 * Do not modify the list this function returns!
	 * 
	 * @param name
	 * @return The list of child packages.
	 */
	public List<Package> getSubpackagesWithName(String name) {
		return subpackages.get(name);
	}

	/**
	 * Adds a function.
	 * 
	 * @param f The function to add. Cannot be null. Must have a name.
	 */
	public void addFunction(Function f) {
		if (f.getName() == null)
			throw new NullPointerException("Cannot add a function without a name");
		
		List<Function> a;
		if (functions.containsKey(f.getName())) {
			a = functions.get(f.getName());
		} else {
			a = new ArrayList<>();
			functions.put(f.getName(), a);
		}

		a.add(f);
	}

	/**
	 * Removes a function.
	 * 
	 * @param f The function to remove. Cannot be null. Must have a name.
	 */
	public void removeFunction(Function f) {
		if (f.getName() == null)
			throw new NullPointerException("Cannot remove a function without a name");
		
		if (functions.containsKey(f.getName())) {
			List<Function> a = functions.get(f.getName());
			a.remove(f);
			if (a.isEmpty()) {
				functions.remove(f.getName());
			}
		}
	}

	/**
	 * Lists the available functions.
	 * Note that multiple functions may have the same name.
	 * Do not modify the list this function returns!
	 * 
	 * @return The list of functions.
	 */
	public List<Function> getFunctions() {
		return new ArrayList<>(functions.values().stream().reduce(new ArrayList<>(), (l1, l2) -> {
			l1.addAll(l2);
			return l1;
		}));
	}

	/**
	 * Lists the available functions with a certain name.
	 * Do not modify the list this function returns!
	 * 
	 * @param name
	 * @return The list of functions.
	 */
	public List<Function> getFunctionsWithName(String name) {
		return functions.get(name);
	}

	/**
	 * Adds a field.
	 * 
	 * @param f The field to add. Cannot be null.
	 */
	public void addField(Field f) {
		fields.put(f.name, f);
	}

	/**
	 * Removes a field.
	 * 
	 * @param f The field to remove. Cannot be null.
	 */
	public void removeField(Field f) {
		fields.remove(f.name, f);
	}

	/**
	 * Lists the available fields.
	 * Do not modify the list this function returns!
	 * 
	 * @return The list of fields.
	 */
	public List<Field> getFields() {
		return new ArrayList<>(fields.values());
	}

	/**
	 * Gets a field with a certain name.
	 * 
	 * @param name
	 * @return A field with the given name.
	 */
	public Field getField(String name) {
		return fields.get(name);
	}

	/**
	 * Adds a type.
	 * 
	 * @param t The type to add. Cannot be null. Must have a name.
	 */
	public void addType(Type t) {
		if (t.getName() == null)
			throw new NullPointerException("Cannot add a type without a name");
		types.put(t.getName(), t);
	}

	/**
	 * Removes a type.
	 * 
	 * @param t The type to remove. Cannot be null. Must have a name.
	 */
	public void removeType(Type t) {
		if (t.getName() == null)
			throw new NullPointerException("Cannot remove a type without a name");
		types.remove(t.getName(), t);
	}

	/**
	 * Lists the available types.
	 * Do not modify the list this function returns!
	 * 
	 * @return The list of types.
	 */
	public List<Type> getTypes() {
		return new ArrayList<>(types.values());
	}
	
	/**
	 * Gets a type with a certain name.
	 * 
	 * @param name
	 * @return A type.
	 */
	public Type getType(String name) {
		return types.get(name);
	}

	/**
	 * Adds an import.
	 * 
	 * @param i
	 */
	public void addImport(Import i) {
		imports.add(i);
	}

	/**
	 * Removes an import.
	 * @param i
	 */
	public void removeImport(Import i) {
		imports.remove(i);
	}

	/**
	 * Lists the imports.
	 * Do not modify the list this function returns!
	 * @return
	 */
	public List<Import> getImports() {
		return imports;
	}

	/**
	 * Adds a annotation definition.
	 * 
	 * @param f The annotation definition to add. Cannot be null.
	 */
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

	/**
	 * Removes a annotation definition.
	 * 
	 * @param f The annotation definition to remove. Cannot be null.
	 */
	public void removeAnnotDef(AnnotationDefinition f) {
		if (definedAnnots.containsKey(f.getName())) {
			List<AnnotationDefinition> a = definedAnnots.get(f.getName());
			a.remove(f);
			if (a.isEmpty()) {
				definedAnnots.remove(f.getName());
			}
		}
	}

	/**
	 * Lists the available annotation definitions.
	 * Note that multiple annotation definitions may have the same name.
	 * Do not modify the list this function returns!
	 * 
	 * @return The list of annotation definitions.
	 */
	public List<AnnotationDefinition> getAnnotDefs() {
		return new ArrayList<>(definedAnnots.values().stream().reduce(new ArrayList<>(), (l1, l2) -> {
			l1.addAll(l2);
			return l1;
		}));
	}

	/**
	 * Lists the available annotation definitions with a certain name.
	 * Do not modify the list this function returns!
	 * 
	 * @param name
	 * @return The list of annotation definitions.
	 */
	public List<AnnotationDefinition> getAnnotDefsWithName(String name) {
		return definedAnnots.get(name);
	}
}
