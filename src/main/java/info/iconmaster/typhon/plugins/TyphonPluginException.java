package info.iconmaster.typhon.plugins;

public class TyphonPluginException extends RuntimeException {
	Class<?> plugin;
	String message;
	
	public TyphonPluginException(Class<?> plugin, String message) {
		this.message = message;
		this.plugin = plugin;
	}
	
	@Override
	public String getMessage() {
		String name = PluginLoader.getPluginName(plugin);
		return "error in plugin "+(name == null ? plugin.getName() : name)+": "+message;
	}
}
