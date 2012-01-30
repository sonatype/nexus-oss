/**
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2012 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.test.utils;

import org.hamcrest.Matcher;
import org.hamcrest.MatcherAssert;
import org.restlet.data.Response;
import org.sonatype.nexus.integrationtests.RequestFacade;

/**
 * Assertions on {@link Response}.
 *
 * @see Response
 * @see NexusRequestMatchers
 * @see ResponseMatchers
 * @since 2.0
 *
 */
public class ResponseAssert
{

    /**
     * Delegates directly to {@link MatcherAssert#assertThat(java.lang.Object, org.hamcrest.Matcher) }
     * ensuring {@link Response#release} is always called, even if matcher fails.
     *
     * @see MatcherAssert#assertThat(java.lang.Object, org.hamcrest.Matcher)
     * @param response the response to match and release
     * @param matcher the matcher to apply to the response
     * @throws AssertionError if matcher fails
     */
    public static void assertThenRelease(final Response actual, final Matcher<Response> matcher) {
        try
        {
            MatcherAssert.assertThat(actual, matcher);
        }
        finally
        {
            RequestFacade.releaseResponse(actual);
        }
    }

    /**
     * Delegates directly to {@link MatcherAssert#assertThat(java.lang.String, java.lang.Object, org.hamcrest.Matcher)
     * ensuring {@link Response#release} is always called, even if matcher fails.
     *
     * @see MatcherAssert#assertThat(java.lang.String, java.lang.Object, org.hamcrest.Matcher)
     * @param reason reason message if matcher fails
     * @param response the response to match and release
     * @param matcher the matcher to apply to the response
     * @throws AssertionError if matcher fails
     */
    public static void assertThenRelease(final String reason, final Response actual, final Matcher<Response> matcher) {
        try
        {
            MatcherAssert.assertThat(reason, actual, matcher);
        }
        finally
        {
            RequestFacade.releaseResponse(actual);
        }
    }

}
