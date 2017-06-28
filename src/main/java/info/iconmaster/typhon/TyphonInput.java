package info.iconmaster.typhon;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import info.iconmaster.typhon.language.CorePackage;
import info.iconmaster.typhon.language.Package;

public class TyphonInput {
	public List<File> inputFiles = new ArrayList<>();
	public List<Package> inputPackages = new ArrayList<>();
	
	public CorePackage corePackage = new CorePackage(this);
}
