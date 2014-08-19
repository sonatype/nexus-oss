package org.sonatype.nexus.componentviews;

import org.sonatype.nexus.componentviews.internal.ComponentViewServlet;

import com.google.inject.AbstractModule;
import com.google.inject.servlet.ServletModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @since 3.0
 */
public class ViewsModule
    extends AbstractModule
{
  private static final Logger log = LoggerFactory.getLogger(ViewsModule.class);

  public static final String MOUNT_POINT = "/views";

  @Override
  protected void configure() {
    install(new ServletModule()
    {
      @Override
      protected void configureServlets() {
        serve(MOUNT_POINT).with(ComponentViewServlet.class);
      }
    });
    log.info("Nexus component views configured.");
  }
}
