package net.evalcode.services.directory;


import javax.inject.Inject;
import javax.inject.Singleton;
import net.evalcode.services.http.service.HttpService;
import net.evalcode.services.http.service.HttpServiceServletModule;
import net.evalcode.services.manager.component.annotation.Activate;
import net.evalcode.services.manager.component.annotation.Component;
import net.evalcode.services.manager.component.annotation.Deactivate;


/**
 * DirectoryComponent
 *
 * @author carsten.schipke@gmail.com
 */
@Singleton
@Component(module=DirectoryComponentModule.class)
public class DirectoryComponent implements HttpService
{
  // MEMBERS
  @Inject
  DirectoryComponentServletModule componentServletModule;


  // ACCESSORS/MUTATORS
  @Activate
  public void activate()
  {

  }

  @Deactivate
  public void deactivate()
  {

  }


  // OVERRIDES
  @Override
  public HttpServiceServletModule getServletModule()
  {
    return componentServletModule;
  }
}
