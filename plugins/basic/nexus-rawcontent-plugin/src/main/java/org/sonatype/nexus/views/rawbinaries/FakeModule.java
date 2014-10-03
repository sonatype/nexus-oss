package org.sonatype.nexus.views.rawbinaries;

import javax.inject.Named;

import com.google.inject.AbstractModule;

/**
 * TODO: Remove this class.
 *
 * @since 3.0
 */
@Named
public class FakeModule extends AbstractModule
{
  @Override
  protected void configure() {
    System.err.println("Running.");
  }
}
