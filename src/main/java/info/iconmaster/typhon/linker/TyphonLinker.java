package info.iconmaster.typhon.linker;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import info.iconmaster.typhon.compiler.TyphonSourceReader;
import info.iconmaster.typhon.errors.UnresolvedImportError;
import info.iconmaster.typhon.language.Import;
import info.iconmaster.typhon.language.Import.PackageImport;
import info.iconmaster.typhon.language.Import.RawImport;
import info.iconmaster.typhon.language.Package;

/**
 * This class contains methods for resolving imports in Typhon code.
 * 
 * @author iconmaster
 *
 */
public class TyphonLinker {
	private TyphonLinker() {}
	
	/**
	 * Resolves all imports in a package.
	 * 
	 * @param p
	 */
	public static void link(Package p) {
		// gather imports
		ArrayList<Import> imports = new ArrayList<>();
		gatherImports(imports, p);
		
		// attempt to resolve imports.
		// to handle cyclic imports, etc. we do this in a loop until they're all either resolved or unresolved.
		boolean allUnresolved = false;
		
		do {
			for (Import i : imports) {
				link(i);
			}
			
			allUnresolved = imports.isEmpty() ? false : imports.stream().allMatch((i)->!i.isResolved());
			
			int n = imports.size();
			imports = imports.stream().filter((i)->!i.isResolved()).collect(Collectors.toCollection(()->new ArrayList<>(n)));
		} while (!imports.isEmpty() && !allUnresolved);
		
		// if allUnresolved, then add the errors for unresolved imports
		if (allUnresolved) {
			for (Import i : imports) {
				p.tni.errors.add(new UnresolvedImportError(i));
			}
		}
	}
	
	/**
	 * Attempts to resolve an import.
	 * This function will update the fields in the import for you, and will move around packages to implement package aliases.
	 * 
	 * @param toResolve The import to resolve.
	 */
	public static void link(Import toResolve) {
		if (toResolve.isResolved()) {
			return;
		}
		
		// resolve the import
		if (toResolve instanceof PackageImport) {
			PackageImport i = (PackageImport) toResolve;
			
			Package base = i.getParent();
			String baseName = i.getPackageName().get(0);
			outOfLoop: {
				while (base != null) {
					for (Package p : base.getSubpackges()) {
						if (baseName.equals(p.getName())) {
							List<Package> matches = new ArrayList<>();
							matches.add(base);
							
							for (String s : i.getPackageName()) {
								List<Package> newMatches = new ArrayList<>();
								
								for (Package match : matches) {
									newMatches.addAll(match.getSubpackagesWithName(s));
								}
								
								matches = newMatches;
							}
							
							if (!matches.isEmpty()) {
								i.getResolvedTo().addAll(matches);
								i.isResolved(true);
								break outOfLoop;
							}
						}
					}
					
					base = base.getParent();
				}
			}
		} else if (toResolve instanceof RawImport) {
			RawImport i = (RawImport) toResolve;
			
			Package resolvedTo = null;
			for (File file : i.tni.rawImportLookupPaths) {
				File toCheck = Paths.get(file.toString(), i.getImportData()).toFile();
				if (toCheck.exists()) {
					try {
						resolvedTo = TyphonSourceReader.parseFile(i.tni, toCheck);
						break;
					} catch (IOException e) {
						// ignore; we'll assume there's another viable candidate in the lookup paths
					}
				}
			}
			
			if (resolvedTo != null) {
				i.getResolvedTo().add(resolvedTo);
				i.isResolved(true);
			}
		} else {
			throw new IllegalArgumentException("Unknown sublclass of Import");
		}
		
		// handle the alias (but only if resolved, so we only do this once)
		if (toResolve.isResolved() && !toResolve.getAliasName().isEmpty()) {
			Package base = toResolve.getParent();
			for (String s : toResolve.getAliasName()) {
				base = new Package(toResolve.source, s, base);
			}
			base.addImport(toResolve);
			toResolve.getAliasName().clear();
		}
	}
	
	/**
	 * Given a package, finds all the imports in the package and all subpackages.
	 * 
	 * @param imports
	 * @param p
	 */
	private static void gatherImports(ArrayList<Import> imports, Package p) {
		for (Import i : p.getImports()) {
			if (!i.isResolved()) {
				imports.add(i);
			}
		}
		
		for (Package pp : p.getSubpackges()) {
			gatherImports(imports, pp);
		}
	}
}
