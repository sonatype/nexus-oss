/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2013 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */

package org.sonatype.nexus.rest;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Collections;

import org.junit.Test;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Preference;
import org.restlet.data.Reference;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.resource.Variant;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

/**
 * Tests for {@link ContentPlexusResource}.
 *
 * @since 2.0
 */
public class ContentPlexusResourceTest
{
  /**
   * Test the basic content types.
   */
  @Test
  public void testPreferredVariant() {
    verifyPreferredVariant(MediaType.TEXT_PLAIN, MediaType.TEXT_PLAIN);
    verifyPreferredVariant(MediaType.TEXT_CSS, MediaType.TEXT_CSS);
    verifyPreferredVariant(MediaType.TEXT_HTML, MediaType.TEXT_HTML);
    verifyPreferredVariant(MediaType.ALL, MediaType.TEXT_HTML);
    verifyPreferredVariant(MediaType.APPLICATION_JAVASCRIPT, MediaType.APPLICATION_JAVASCRIPT);
    verifyPreferredVariant(MediaType.TEXT_JAVASCRIPT, MediaType.TEXT_JAVASCRIPT);
    verifyPreferredVariant(null, MediaType.TEXT_HTML);
  }

  private void verifyPreferredVariant(MediaType mediaTypeInRequest, MediaType expectedMediaType) {
    Request request = new Request();
    if (mediaTypeInRequest != null) {
      request.getClientInfo().setAcceptedMediaTypes(
          Collections.singletonList(new Preference<MediaType>(mediaTypeInRequest)));
    }
    Response response = new Response(request);

    NexusRestletResource resource =
        new NexusRestletResource(new Context(), request, response, new ContentPlexusResource());

    Variant preferredVariant = resource.getPreferredVariant();
    assertThat("Preferred Variant is null for media type: " + mediaTypeInRequest + " expected: "
        + expectedMediaType, preferredVariant, notNullValue());
    assertThat(preferredVariant.getMediaType(), equalTo(expectedMediaType));
  }

  @Test
  public void testNexus5043UrlEncodedCharsInPath()
      throws UnsupportedEncodingException
  {
    final String tilde = "~";
    // as made with lightweight wagon
    final Request nonEncodedRequest = new Request(Method.GET, new Reference(tilde));
    // as made with http wagon
    final Request encodedRequest = new Request(Method.GET, new Reference(URLEncoder.encode(tilde, "UTF-8")));

    final ContentPlexusResource contentPlexusResource = new ContentPlexusResource();

    assertThat(contentPlexusResource.getResourceStorePath(nonEncodedRequest), equalTo(tilde));
    assertThat(contentPlexusResource.getResourceStorePath(encodedRequest), equalTo(tilde));
  }

}
