package info.iconmaster.typhon.language;

import java.util.ArrayList;
import java.util.List;

import info.iconmaster.typhon.types.Type;

public class Package {
	public Package parent;
	public String name;
	public List<Function> functions = new ArrayList<>();
	public List<Field> fields = new ArrayList<>();
	public List<Package> subpackages = new ArrayList<>();
	public List<Import> imports = new ArrayList<>();
	public List<Type> types = new ArrayList<>();
}
