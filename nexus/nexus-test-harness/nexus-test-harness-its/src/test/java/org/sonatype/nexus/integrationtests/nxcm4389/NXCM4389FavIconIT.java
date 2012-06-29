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
package org.sonatype.nexus.integrationtests.nxcm4389;

import static org.hamcrest.MatcherAssert.assertThat;

import java.io.IOException;

import org.hamcrest.Matchers;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.sonatype.nexus.integrationtests.AbstractNexusIntegrationTest;
import org.sonatype.nexus.integrationtests.RequestFacade;
import org.sonatype.nexus.test.utils.NexusRequestMatchers;
import org.testng.annotations.Test;

/**
 * Check that index.html favicons are available.
 */
public class NXCM4389FavIconIT
    extends AbstractNexusIntegrationTest
{

    @Test
    public void testFavicons()
        throws IOException
    {
        // assert that shortcut icon mentioned in the HTML is actually available
        final String text = RequestFacade.doGetForText( "index.html" );
        Document doc = Jsoup.parse( text );
        RequestFacade.doGetForStatus( doc.select("link[rel=icon]").attr("href"), NexusRequestMatchers.isSuccess() );

        // assert that IE elements are in there as well
        final String head = doc.select( "head" ).outerHtml();
        final int start = head.indexOf( "IE]>" );
        final int end = head.lastIndexOf( "endif" );
        doc = Jsoup.parse( head.substring( start + 4, end ) );
        RequestFacade.doGetForStatus( doc.select("link[rel=shortcut icon]").attr("href"), NexusRequestMatchers.isSuccess() );
    }

}
