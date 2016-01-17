package net.evalcode.services.directory;


import java.net.URL;
import javax.inject.Singleton;
import net.evalcode.services.directory.internal.Directory;
import net.evalcode.services.directory.service.rest.UserResource;
import net.evalcode.services.http.service.HttpServiceServletModule;
import net.evalcode.services.manager.component.ComponentBundleInspector;
import net.evalcode.services.manager.component.ComponentBundleInterface;


/**
 * DirectoryComponentServletModule
 *
 * @author carsten.schipke@gmail.com
 */
@Singleton
public class DirectoryComponentServletModule extends HttpServiceServletModule
{
  // OVERRIDES/IMPLEMENTS
  @Override
  public String getResourcePath()
  {
    final ComponentBundleInterface bundle=injector.getInstance(ComponentBundleInterface.class);
    final ComponentBundleInspector bundleInspector=bundle.getInspector();

    final URL url=bundleInspector.searchResourceInBundleClassPath("resource");

    if(null==url)
      return super.getResourcePath();

    return url.toExternalForm();
  }

  @Override
  public String getContextPath()
  {
    return "/directory";
  }

  @Override
  public String getSecurityRealm()
  {
    return "directory";
  }


  // IMPLEMENTATION
  @Override
  protected void configureServlets()
  {
    super.configureServlets();

    bind(Directory.class);

    bind(UserResource.class);
  }
}
