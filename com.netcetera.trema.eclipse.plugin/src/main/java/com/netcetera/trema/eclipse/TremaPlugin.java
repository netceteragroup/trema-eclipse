package com.netcetera.trema.eclipse;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;



/**
 * The Trema plugin.
 */
public class TremaPlugin extends AbstractUIPlugin {
  
  // the shared instance
  private static TremaPlugin plugin = null;
  
  // the resource bundle
  private ResourceBundle resourceBundle = null;
  
  /**
   * The constructor.
   */
  public TremaPlugin() {
    plugin = this;
    try {
      resourceBundle = ResourceBundle.getBundle("com.netcetera.trema.eclipse.plugin.TremaPluginResources");
    } catch (MissingResourceException x) {
      resourceBundle = null;
    }
  }

  /** {@inheritDoc} */
  public void start(BundleContext context) throws Exception {
    super.start(context);
  }

  /** {@inheritDoc} */
  public void stop(BundleContext context) throws Exception {
    super.stop(context);
  }

  /**
   * Gets the Trema Plugin.
   * 
   * @return the default instance.
   */
  public static TremaPlugin getDefault() {
    return plugin;
  }

  /**
   * Resource String lookup.
   * 
   * @param key the key
   * @return the string from the plugin's resource bundle,
   * or 'key' if not found.
   */
  public static String getResourceString(String key) {
    ResourceBundle bundle = TremaPlugin.getDefault().getResourceBundle();
    try {
      return (bundle != null) ? bundle.getString(key) : key;
    } catch (MissingResourceException e) {
      return key;
    }
  }

  /**
   * Gets the resource bundle.
   * 
   * @return the plugin's resource bundle.
   */
  public ResourceBundle getResourceBundle() {
    return resourceBundle;
  }
  
  /**
   * Gets an image descriptor to a given image file path. Uses an
   * <code>ImageRegistry</code> for caching.
   * @param imageFilePath the image file path
   * @return the image descriptor or <code>null</code> if none could be
   * found
   */
  public ImageDescriptor getImageDescriptor(String imageFilePath) {
    ImageRegistry imageRegistry = getImageRegistry();
    ImageDescriptor descriptor = imageRegistry.getDescriptor(imageFilePath);
    if (descriptor == null) {
      descriptor = imageDescriptorFromPlugin(getId(), imageFilePath);
      imageRegistry.put(imageFilePath, descriptor);
    }
    return descriptor;
  }
  
  /**
   * Gets an image to a given image file path. Uses an
   * <code>ImageRegistry</code> for caching.
   * @param imageFilePath the image file path
   * @return the image or <code>null</code> if none could be found
   */
  public Image getImage(String imageFilePath) {
    ImageRegistry imageRegistry = getImageRegistry();
    Image image = imageRegistry.get(imageFilePath);
    if (image == null) {
      // put the descriptor
      ImageDescriptor descriptor = imageDescriptorFromPlugin(getId(), imageFilePath);
      imageRegistry.put(imageFilePath, descriptor);
      image = imageRegistry.get(imageFilePath);
    }
    return image;
  }
  
  /**
   * Gets the Plugin id.
   * 
   * @return the unique id of this plugin.
   */
  public static String getId() {
    return getDefault().getBundle().getSymbolicName();
  }
  
  /**
   * Convenience method to log an error. Uses
   * <code>TremaUtil.createErrorStatus(String)</code> to create an
   * <code>IStatus</code>.
   * @param message the error message to be logged
   */
  public static void logError(String message) {
    getDefault().getLog().log(TremaUtilEclipse.createErrorStatus(message));
  }
  
  /**
   * Convenience method to log an error. Uses
   * <code>TremaUtil.createErrorStatus(Throwable)</code> to create an
   * <code>IStatus</code>.
   * @param throwable the throwable to be logged
   */
  public static void logError(Throwable throwable) {
    getDefault().getLog().log(TremaUtilEclipse.createErrorStatus(throwable));
  }

}
