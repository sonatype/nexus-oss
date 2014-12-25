package org.sonatype.nexus.repository.simple;

import javax.inject.Named;

import com.google.inject.AbstractModule;
import org.eclipse.sisu.bean.LifecycleModule;

/**
 * Simple repository module.
 *
 * @since 3.0
 */
@Named
public class SimpleModule
  extends AbstractModule
{
  @Override
  protected void configure() {
    install(new LifecycleModule());
  }
}
