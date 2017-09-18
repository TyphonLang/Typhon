package info.iconmaster.typhon.plugins;

public class TyphonPluginException extends RuntimeException {
	Class<?> plugin;
	String message;
	Throwable cause;
	
	public TyphonPluginException(Class<?> plugin, String message) {
		this.message = message;
		this.plugin = plugin;
	}
	
	public TyphonPluginException(Class<?> plugin, String message, Throwable cause) {
		this.message = message;
		this.plugin = plugin;
		this.cause = cause;
	}
	
	@Override
	public synchronized Throwable getCause() {
		return cause;
	}
	
	@Override
	public String getMessage() {
		String name = PluginLoader.getPluginName(plugin);
		return "error in plugin "+(name == null ? plugin.getName() : name)+": "+message;
	}
}
