package org.sonatype.nexus.component.metadata.memory;

import org.sonatype.nexus.component.metadata.api.ComponentMetadataService;
import org.sonatype.nexus.component.metadata.testsupport.AbstractComponentMetadataServiceTest;

public class MemoryComponentMetadataServiceTest
    extends AbstractComponentMetadataServiceTest
{
  @Override
  public ComponentMetadataService createTestInstance() {
    return new MemoryComponentMetadataService();
  }

  @Override
  public void destroyTestInstance(ComponentMetadataService service) {
    // memory impl; nothing to do
  }
}
