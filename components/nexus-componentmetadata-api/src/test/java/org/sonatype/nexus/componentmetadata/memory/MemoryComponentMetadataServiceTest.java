package org.sonatype.nexus.componentmetadata.memory;

import org.sonatype.nexus.componentmetadata.api.ComponentMetadataService;
import org.sonatype.nexus.componentmetadata.testsupport.AbstractComponentMetadataServiceTest;

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
