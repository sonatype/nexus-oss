package org.sonatype.nexus.component.source.internal;

import org.sonatype.nexus.component.source.ComponentSourceId;
import org.sonatype.sisu.litmus.testsupport.TestSupport;

import org.junit.Test;

public class InMemorySourceRegistryTest
    extends TestSupport
{

  @Test(expected = RuntimeException.class)
  public void nameNotFoundCausesException() {
    final InMemorySourceRegistry registry = new InMemorySourceRegistry();

    registry.getSource("not found");
  }


  @Test(expected = RuntimeException.class)
  public void idNotFoundCausesException() {
    final InMemorySourceRegistry registry = new InMemorySourceRegistry();

    registry.getSource(new ComponentSourceId("not found", "uuid"));
  }
}