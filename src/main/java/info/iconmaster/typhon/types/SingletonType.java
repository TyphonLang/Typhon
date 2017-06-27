package info.iconmaster.typhon.types;

public class SingletonType extends Type {
	private SingletonType() {}
	
	public static class AnyType extends SingletonType {}
}
