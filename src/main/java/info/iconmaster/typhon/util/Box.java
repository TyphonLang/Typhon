package info.iconmaster.typhon.util;

/**
 * This class is a wrapper around a single value.
 * Used in lambdas to make object closures.
 * 
 * @author iconmaster
 *
 * @param <T> The type you want to put in the box.
 */
public class Box<T> {
	/**
	 * The data in the box.
	 */
	public T data;
	
	public Box() {}
	
	public Box(T data) {
		this.data = data;
	}
}
