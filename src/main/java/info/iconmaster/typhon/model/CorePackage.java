package info.iconmaster.typhon.model;

import java.util.Arrays;
import java.util.List;

import info.iconmaster.typhon.TyphonInput;
import info.iconmaster.typhon.model.libs.CoreLibraryMath;
import info.iconmaster.typhon.model.libs.CoreLibraryOperators;
import info.iconmaster.typhon.types.AnyType;
import info.iconmaster.typhon.types.SystemType;
import info.iconmaster.typhon.types.TemplateType;
import info.iconmaster.typhon.types.Type;
import info.iconmaster.typhon.types.UserType;

/**
 * The core package.
 * This package contains data for built-in types and functions.
 * All packages derive from the core package.
 * The core package cannot have a parent.
 * 
 * @author iconmaster
 *
 */
public class CorePackage extends Package {
	/**
	 * Constants for built-in types.
	 */
	public AnyType TYPE_ANY;
	
	/**
	 * Constants for built-in types.
	 */
	public UserType TYPE_NUMBER, TYPE_INTEGER, TYPE_REAL, TYPE_LIST, TYPE_MAP;
	
	/**
	 * Constants for built-in types.
	 */
	public SystemType TYPE_BYTE, TYPE_SHORT, TYPE_INT, TYPE_LONG, TYPE_UBYTE, TYPE_USHORT, TYPE_UINT, TYPE_ULONG, TYPE_CHAR, TYPE_BOOL, TYPE_STRING, TYPE_FLOAT, TYPE_DOUBLE;
	
	/**
	 * Constants for annotations.
	 */
	public AnnotationDefinition ANNOT_MAIN;
	
	/**
	 * Constants for operator annotations.
	 */
	public AnnotationDefinition ANNOT_OP_ADD, ANNOT_OP_SUB, ANNOT_OP_MUL, ANNOT_OP_DIV, ANNOT_OP_MOD;
	
	private UserType makeUserType(String name, Type parent) {
		UserType type = new UserType(name, parent);
		addType(type);
		return type;
	}
	
	private SystemType makeSystemType(String name, Type parent) {
		SystemType type = new SystemType(name, parent);
		addType(type);
		return type;
	}
	
	private AnnotationDefinition makeAnnotDef(String name, Parameter... params) {
		AnnotationDefinition annot = new AnnotationDefinition(tni, name, params);
		addAnnotDef(annot);
		return annot;
	}
	
	public CorePackage(TyphonInput tni) {
		super(tni, "core");
		
		// an awful hack to make sure everything runs smoothly
		tni.corePackage = this;
		
		// form the core type tree
		TYPE_ANY = new AnyType(tni); addType(TYPE_ANY);
		
		TYPE_NUMBER = makeUserType("Number", TYPE_ANY);
		TYPE_INTEGER = makeUserType("Integer", TYPE_NUMBER);
		TYPE_REAL = makeUserType("Real", TYPE_NUMBER);
		
		TYPE_BYTE = makeSystemType("byte", TYPE_INTEGER);
		TYPE_SHORT = makeSystemType("short", TYPE_INTEGER);
		TYPE_INT = makeSystemType("int", TYPE_INTEGER);
		TYPE_LONG = makeSystemType("long", TYPE_INTEGER);
		TYPE_UBYTE = makeSystemType("ubyte", TYPE_INTEGER);
		TYPE_USHORT = makeSystemType("ushort", TYPE_INTEGER);
		TYPE_UINT = makeSystemType("uint", TYPE_INTEGER);
		TYPE_ULONG = makeSystemType("ulong", TYPE_INTEGER);
		TYPE_CHAR = makeSystemType("char", TYPE_INTEGER);
		TYPE_FLOAT = makeSystemType("float", TYPE_REAL);
		TYPE_DOUBLE = makeSystemType("double", TYPE_REAL);
		TYPE_STRING = makeSystemType("string", TYPE_ANY);
		TYPE_BOOL = makeSystemType("bool", TYPE_ANY);
		
		TYPE_LIST = makeUserType("List", TYPE_ANY);
		TYPE_LIST.getTemplates().add(new TemplateType(tni, "T"));
		
		TYPE_MAP = makeUserType("Map", TYPE_ANY);
		TYPE_MAP.getTemplates().add(new TemplateType(tni, "K"));
		TYPE_MAP.getTemplates().add(new TemplateType(tni, "V"));
		
		// add any core libraries
		addSubpackage(new CoreLibraryMath(tni));
		addSubpackage(new CoreLibraryOperators(tni));
		
		// add the annotations defined
		ANNOT_MAIN = makeAnnotDef("main", new Parameter[] {});
	}
	
	/**
	 * This always returns null.
	 * The core package cannot have a parent.
	 */
	@Override
	public Package getParent() {
		return null;
	}
	
	/**
	 * This always throws an {@link IllegalArgumentException}.
	 * The core package cannot have a parent.
	 */
	@Override
	public void setParent(Package parent) {
		throw new IllegalArgumentException("Cannot set parent of core package");
	}
	
	/**
	 * Overriden to make it appear as if there are no subpackages.
	 * This is to make imports mandatory and optimize library inclusions.
	 */
	@Override
	public List<Package> getSubpackges() {
		return Arrays.asList();
	}
	
	/**
	 * Overriden to make it appear as if there are no subpackages.
	 * This is to make imports mandatory and optimize library inclusions.
	 */
	@Override
	public List<Package> getSubpackagesWithName(String name) {
		return Arrays.asList();
	}
	
	/**
	 * @return The actual subpackages for this core package.
	 * This includes libraries that have been imported and user packages.
	 */
	public List<Package> getCoreSubpackages() {
		return super.getSubpackges();
	}
	
	/**
	 * @return The actual subpackages for this core package.
	 * This includes libraries that have been imported and user packages.
	 */
	public List<Package> getCoreSubpackagesWithName(String s) {
		return super.getSubpackagesWithName(s);
	}
}
