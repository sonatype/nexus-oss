package org.sonatype.nexus.views.rawbinaries.view;

import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.nexus.componentviews.config.ViewFactory;

import com.google.inject.AbstractModule;

/**
 * @since 3.0
 */
@Named
public class RawBinariesViewModule
    extends AbstractModule
{
  @Override
  protected void configure() {
    bind(ViewFactory.class).to(RawBinaryViewRecipe.class).in(Singleton.class);
  }
}
