package org.sonatype.nexus.repository.raw.internal.proxy;

import javax.annotation.Nonnull;

import org.sonatype.nexus.repository.httpbridge.HttpResponses;
import org.sonatype.nexus.repository.view.Context;
import org.sonatype.nexus.repository.view.Handler;
import org.sonatype.nexus.repository.view.Payload;
import org.sonatype.nexus.repository.view.Response;
import org.sonatype.sisu.goodies.common.ComponentSupport;

/**
 * A format-neutral proxy handler.
 *
 * @since 3.0
 */
public class ProxyHandler
    extends ComponentSupport
    implements Handler
{
  @Nonnull
  @Override
  public Response handle(@Nonnull final Context context) throws Exception {

    // ensure GET
    final Locator locator = locator(context);

    final Payload payload = payloadStorage().get(locator);

    if (payload != null) {
      return HttpResponses.ok(payload);
    }

    return HttpResponses.notFound(locator.describe());
  }

  private PayloadStorage payloadStorage() {
    return null;
  }

  private Locator locator(final Context context) {
    final LocatorFacet facet = context.getRepository().facet(LocatorFacet.class);
    return facet.locator(context);
  }

}
