package org.sonatype.nexus.component.source.internal;

import org.sonatype.nexus.component.source.api.ComponentSourceId;

import org.junit.Test;

public class InMemorySourceRegistryTest
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