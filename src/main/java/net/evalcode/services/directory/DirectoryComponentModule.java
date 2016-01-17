package net.evalcode.services.directory;


import javax.inject.Singleton;
import net.evalcode.services.manager.component.ServiceComponentModule;


/**
 * DirectoryComponentModule
 *
 * @author carsten.schipke@gmail.com
 */
@Singleton
public class DirectoryComponentModule extends ServiceComponentModule
{
  // IMPLEMENTATION
  @Override
  protected void configure()
  {
    super.configure();

    bind(DirectoryComponent.class);
    bind(DirectoryComponentServletModule.class);
  }
}
