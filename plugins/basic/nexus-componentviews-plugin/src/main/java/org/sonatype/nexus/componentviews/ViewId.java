package org.sonatype.nexus.componentviews;

import org.sonatype.nexus.component.model.ComponentOriginId;

/**
 * @since 3.0
 */
public class ViewId
    extends ComponentOriginId
{
  public ViewId(final String name, final String identifier) {
    super(name, identifier);
  }

  public ViewId(final String uniqueString) {
    super(uniqueString);
  }
}
