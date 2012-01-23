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
package org.sonatype.nexus.selenium.nexus2203;

import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import java.io.ByteArrayInputStream;

import org.codehaus.plexus.component.annotations.Component;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Status;
import org.sonatype.nexus.mock.MockListener;
import org.sonatype.nexus.mock.MockResponse;
import org.sonatype.nexus.mock.SeleniumTest;
import org.sonatype.nexus.mock.pages.LogsViewTab;
import org.sonatype.nexus.mock.rest.MockHelper;
import org.sonatype.nexus.rest.model.LogsListResource;
import org.sonatype.nexus.rest.model.LogsListResourceResponse;
import org.sonatype.plexus.rest.representation.InputStreamRepresentation;
import org.testng.Assert;
import org.testng.annotations.Test;

@Component( role = Nexus2203LogViewTest.class )
public class Nexus2203LogViewTest
    extends SeleniumTest
{

    @SuppressWarnings( "unchecked" )
    @Test
    public void contentLoading()
        throws InterruptedException
    {
        doLogin();

        MockHelper.expect( "/logs", new MockResponse( Status.SUCCESS_OK, logsResponse(), Method.GET ) );
        LogsViewTab logs = main.openViewLogs();
        String content = logs.getContent();
        assertThat( content, anyOf( nullValue(), equalTo( "" ), equalTo( "null" ) ) );

        // load log
        MockResponse mr =
            MockHelper.expect( "/logs/{fileName}", new MockResponse( Status.SUCCESS_OK, nexusLog(), Method.GET ) );
        logs.selectFile( "nexus.log" );
        mr.waitForExecution();
        MockHelper.checkAndClean();
        content = logs.getContent();
        assertThat( content, not( anyOf( nullValue(), equalTo( "" ), equalTo( "null" ) ) ) );

        // reload log
        mr = MockHelper.expect( "/logs/{fileName}", new MockResponse( Status.SUCCESS_OK, nexusLog(), Method.GET ) );
        logs.getReload().click();
        mr.waitForExecution();
        MockHelper.checkAndClean();
        content = logs.getContent();
        assertThat( content, not( anyOf( nullValue(), equalTo( "" ), equalTo( "null" ) ) ) );

        // load configuration
        MockListener<InputStreamRepresentation> ml =
            MockHelper.listen( "/configs/{configName}", new MockListener<InputStreamRepresentation>() );
        logs.selectFile( "nexus.xml" );
        ml.waitForResult( InputStreamRepresentation.class );
        MockHelper.checkAndClean();
        String nexusXmlContent = logs.getContent();
        assertThat( nexusXmlContent, not( anyOf( nullValue(), equalTo( "" ), equalTo( "null" ), equalTo( content ) ) ) );
    }

    private InputStreamRepresentation nexusLog()
    {
        String text =
            "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Vivamus tempor tincidunt massa et scelerisque. Donec convallis tincidunt mattis. Mauris non massa sit amet leo lacinia elementum in ut leo. Donec semper, nisi ut egestas sagittis, felis sem fermentum risus, sed aliquam metus enim at dolor. Pellentesque ac ipsum a lorem consectetur lacinia vitae in nibh. Phasellus euismod mollis lacus vel rutrum. Nam id nisi felis. Nunc in quam ligula. Quisque quis risus a mauris aliquam eleifend in in nisl. Curabitur auctor iaculis justo ac dignissim. Curabitur ligula est, rhoncus ac ullamcorper sed, varius sagittis velit. Ut id erat nisl. Pellentesque at lectus in lectus eleifend bibendum in non dolor. Donec nisi nisl, fringilla et pellentesque vitae, rutrum feugiat tortor. Proin nisl erat, bibendum id mattis luctus, posuere vitae magna. Donec aliquet lorem vitae erat facilisis sagittis. Sed fermentum, mauris ac semper congue, ligula purus porta urna, a pharetra nisl orci vel dui. Donec id aliquam lectus. Integer auctor justo eu augue cursus porttitor ac ac purus. Donec ultrices, erat nec rutrum congue, magna dui varius diam, eget facilisis tortor magna sit amet dui.\n"
                + "Curabitur a vestibulum nibh. Vestibulum eu augue non nisi consequat tincidunt. Nullam facilisis, nunc eu condimentum tristique, odio lectus pretium ante, non aliquam purus nunc in urna. Nulla interdum aliquet nibh quis tristique. Maecenas ac dolor ac arcu tincidunt ultricies at ut sem. In posuere leo eget quam malesuada porttitor. Pellentesque malesuada congue massa eget euismod. Donec in justo ac tortor egestas sagittis a et elit. Sed dui lectus, rutrum sit amet iaculis non, ornare sed arcu. Fusce hendrerit laoreet orci, vel ultricies magna ullamcorper ut. Quisque nec est nunc. Cras et justo et dui suscipit porta eget ac lacus. Sed suscipit, neque et laoreet consectetur, turpis mi tincidunt augue, sed ullamcorper turpis velit vitae mi. Integer sed mauris ut dui euismod cursus. Nulla in tortor ac ipsum facilisis placerat consectetur at risus. Nullam lacus diam, sagittis porttitor dignissim vel, dignissim quis mauris.\n"
                + "Vestibulum porta condimentum ante sed tempus. Vestibulum vitae sapien lorem. Nulla venenatis nibh sed risus fringilla porta. Pellentesque ut risus orci. Vestibulum ligula nisi, vehicula et viverra ac, luctus a tellus. Maecenas blandit magna ut eros laoreet ut congue lacus laoreet. Donec justo magna, adipiscing at sagittis sed, consectetur eu erat. Integer rutrum sagittis nisl, ut gravida erat luctus et. Praesent hendrerit metus id massa hendrerit mollis. Nunc in arcu sagittis sapien facilisis tempor eu ut nibh. Nam venenatis volutpat nunc, at mattis orci consectetur et. Ut tincidunt nibh nec odio auctor faucibus. Aliquam tristique nunc non augue dignissim tristique. Donec feugiat, odio vel aliquam luctus, ligula leo iaculis dolor, et volutpat mauris nunc sed tortor. Etiam vel dapibus turpis. Aenean vestibulum gravida libero commodo fringilla.\n"
                + "Mauris facilisis adipiscing tortor, eget gravida elit vestibulum nec. Quisque convallis ornare gravida. Maecenas rutrum justo ut nulla rhoncus vitae bibendum mauris ultrices. Nullam sed urna quis turpis egestas accumsan eget eu lectus. Duis leo arcu, semper venenatis blandit sit amet, luctus eget justo. Maecenas scelerisque consequat libero eu lacinia. Aenean eget lobortis nisi. Quisque magna purus, facilisis non consectetur sit amet, gravida ac tellus. Duis euismod tempus nulla, ut sodales elit tristique in. Morbi turpis metus, aliquam at feugiat eget, bibendum non sapien. Duis eget elit nec dui rhoncus dictum. Sed mauris magna, egestas vitae vehicula ac, vulputate consequat erat. Donec luctus nulla vel sapien facilisis interdum. Donec ac mauris velit. Nunc vel pretium diam. Curabitur condimentum diam in magna mollis volutpat. Nam varius, lacus sit amet tincidunt ultricies, elit diam dapibus lorem, quis semper turpis urna sed justo.\n"
                + "Ut vulputate tincidunt turpis nec ultricies. Vestibulum adipiscing adipiscing lorem, id faucibus est ultricies ac. Nulla tempor risus et mauris vehicula quis gravida erat ornare. Class aptent taciti sociosqu ad litora torquent per conubia nostra, per inceptos himenaeos. Phasellus sagittis libero sed nisi pellentesque faucibus. Cras sollicitudin pharetra vehicula. Curabitur id felis non elit rhoncus cursus. Aliquam eget dapibus lacus. Pellentesque habitant morbi tristique senectus et netus et malesuada fames ac turpis egestas. In id dui sed nisl euismod vulputate.\n"
                + "Praesent cursus, sem egestas iaculis fermentum, turpis orci viverra tellus, sit amet eleifend felis enim sed felis. Nulla facilisi. Donec a leo urna. Praesent risus eros, interdum sed mattis sodales, lacinia a lacus. Nullam velit quam, mattis et consectetur eget, porta tempor orci. Phasellus metus eros, viverra et bibendum viverra, porta sed tellus. Donec molestie congue lobortis. Curabitur varius porttitor mattis. Nulla molestie neque vel dui posuere eu blandit enim lobortis. Etiam lorem dui, aliquet et volutpat metus.";
        return new InputStreamRepresentation( MediaType.TEXT_PLAIN, new ByteArrayInputStream( text.getBytes() ) );
    }

    private LogsListResourceResponse logsResponse()
    {
        LogsListResource logList = new LogsListResource();
        logList.setName( "nexus.log" );
        logList.setMimeType( "text/plain" );
        logList.setResourceURI( nexusBaseURL + "service/local/logs/nexus.log" );
        logList.setSize( 4098L );

        LogsListResourceResponse logsResponse = new LogsListResourceResponse();
        logsResponse.addData( logList );
        return logsResponse;
    }

    @SuppressWarnings( "unchecked" )
    @Test
    public void tail()
        throws InterruptedException
    {
        doLogin();

        LogsViewTab logs = main.openViewLogs();
        String content = logs.getContent();
        assertThat( content, anyOf( nullValue(), equalTo( "" ), equalTo( "null" ) ) );

        logs.selectFile( "selenium.log" );
        content = logs.getContent();
        assertThat( content, not( anyOf( nullValue(), equalTo( "" ), equalTo( "null" ) ) ) );

        Thread.sleep( 1000 );

        logs.getTailUpdate().click();

        String contentReloaded = logs.getContent();
        assertThat( contentReloaded, not( anyOf( nullValue(), equalTo( "" ), equalTo( "null" ) ) ) );
        Assert.assertTrue( contentReloaded.startsWith( content ) );
    }

}
