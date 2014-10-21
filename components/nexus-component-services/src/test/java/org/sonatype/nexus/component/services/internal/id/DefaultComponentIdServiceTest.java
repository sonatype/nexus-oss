package org.sonatype.nexus.component.services.internal.id;

import org.sonatype.nexus.component.model.ComponentId;
import org.sonatype.nexus.component.services.id.ComponentIdFactory;

import org.junit.Test;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class DefaultComponentIdServiceTest
{
  @Test
  public void restoredIdsAreEqual() {
    final ComponentIdFactory factory = new DefaultComponentIdFactory();

    final ComponentId id = factory.newId();

    final ComponentId restored = factory.fromUniqueString(id.asUniqueString());

    assertThat(id, is(equalTo(restored)));
  }
}
