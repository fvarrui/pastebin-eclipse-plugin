package io.github.fvarrui.eclipse.plugin.pastebin;

import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;


public class PluginActivator extends AbstractUIPlugin {

	public static final String PLUGIN_NAME = "PasteBin Plugin";
	public static final String PLUGIN_ID = "io.github.fvarrui.eclipse.plugin.pastebin";
	
	private static PluginActivator plugin;

	public PluginActivator() {
		PluginActivator.plugin = this;
	}

	public void start(BundleContext context) throws Exception {
		super.start(context);
	}

	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	public static PluginActivator getDefault() {
		return plugin;
	}

}
