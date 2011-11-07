/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions
 *
 * This program is free software: you can redistribute it and/or modify it only under the terms of the GNU Affero General
 * Public License Version 3 as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Affero General Public License Version 3
 * for more details.
 *
 * You should have received a copy of the GNU Affero General Public License Version 3 along with this program.  If not, see
 * http://www.gnu.org/licenses.
 *
 * Sonatype Nexus (TM) Open Source Version is available from Sonatype, Inc. Sonatype and Sonatype Nexus are trademarks of
 * Sonatype, Inc. Apache Maven is a trademark of the Apache Foundation. M2Eclipse is a trademark of the Eclipse Foundation.
 * All other trademarks are the property of their respective owners.
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
 * @since 1.10.0
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
