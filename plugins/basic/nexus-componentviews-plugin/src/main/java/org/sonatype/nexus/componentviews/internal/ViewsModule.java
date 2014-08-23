package org.sonatype.nexus.componentviews.internal;

import javax.inject.Named;

import org.sonatype.nexus.componentviews.config.ViewConfigStore;
import org.sonatype.nexus.componentviews.internal.orient.OrientViewConfigStore;

import com.google.inject.AbstractModule;
import com.google.inject.servlet.ServletModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @since 3.0
 */
@Named
public class ViewsModule
    extends AbstractModule
{
  private static final Logger log = LoggerFactory.getLogger(ViewsModule.class);

  public static final String MOUNT_POINT = "/views/*";

  @Override
  protected void configure() {
    bind(ViewModuleBooter.class).asEagerSingleton();
    bind(ViewConfigStore.class).to(OrientViewConfigStore.class);
    //bind(ViewConfigRecipeBook.class).to(RecipeBook.class);


    install(new ServletModule()
    {
      @Override
      protected void configureServlets() {
        bind(ComponentViewServlet.class);
        serve("/views").with(ComponentViewServlet.class);
        serve("/views/*").with(ComponentViewServlet.class);
      }
    });
    log.info("Nexus component views configured.");
  }
}
