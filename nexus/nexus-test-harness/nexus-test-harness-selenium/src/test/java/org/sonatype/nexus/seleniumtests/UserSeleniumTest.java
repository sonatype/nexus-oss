/**
 * Sonatype Nexus (TM) [Open Source Version].
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at ${thirdPartyUrl}.
 *
 * This program is licensed to you under Version 3 only of the GNU
 * General Public License as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License Version 3 for more details.
 *
 * You should have received a copy of the GNU General Public License
 * Version 3 along with this program. If not, see http://www.gnu.org/licenses/.
 */
package org.sonatype.nexus.seleniumtests;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;

import org.apache.log4j.Logger;
import org.codehaus.plexus.util.FileUtils;

import com.thoughtworks.selenium.SeleneseTestCase;

public class UserSeleniumTest
    extends SeleneseTestCase
{
    private Logger log = Logger.getLogger( getClass() );
    
    public void setUp()
        throws Exception
    {
        setUp( TestProperties.getString( "nexus.base.url" ), "*chrome" );
    }

    public void testNew()
        throws Throwable
    {
        Nexus.start();

        try
        {

            selenium.setBrowserLogLevel( "INFO" );
            selenium.getEval( "LOG.info('TEST LOG');" );

            selenium.setSpeed( "400" );
            selenium.open( "/nexus/" );

            // login
            this.login( "admin", "admin123" );

            // get the javascript from a file (so its readable)
            String js = this.getJavaScript( "scripts/test.js");
            selenium.getEval( js );

            // open security tab
            selenium.mouseDown( "open-security-users" );
            selenium.mouseUp( "open-security-users" );

            // click the Add user button
            selenium.click( "//table[@id='user-add-btn']/tbody/tr/td[2]/em/button" );

            // populate user info
            selenium.type( "userId", "TestUser" );
            selenium.type( "name", "TestName" );
            selenium.type( "email", "Foo@bar.com" );
            // change the combo box
            selenium.mouseDown( "//input[@name='status']" );
            selenium.click( "//div/div[text()='Active']" );
            // select the admin role
            selenium.click( "//a/span" );
            selenium.click( "document.forms[0].elements[4]" );

            // hack the send
            // TODO:

            // click the save button
            selenium.click( "//div[2]/div/div/div/div/div/table/tbody/tr/td[1]/table/tbody/tr/td[2]/em/button" );
            Thread.sleep( 2000 );
            
            // We don't actually check if the User was added/saved, to be a valid test we would need to do that,
            // but for now, we are just trying to get selenium to hack the request method.

        }
        catch ( Throwable t )
        {
            Nexus.stop();
            throw t;
        }
    }

    protected void login( String username, String password )
    {
        selenium.click( "login-link" );
        selenium.type( "usernamefield", username );
        selenium.type( "passwordfield", password );
        selenium.click( "loginbutton" );
    }

    private String getJavaScript( String resource )
        throws IOException
    {
        
        
        return FileUtils.fileRead( this.getResource( resource ) );
    }
    
    private File getResource( String resource )
    {
        log.debug( "Looking for resource: " + resource );
        URL classURL = Thread.currentThread().getContextClassLoader().getResource( resource );
        log.debug( "found: " + classURL );

        try
        {
            return classURL == null ? null : new File( URLDecoder.decode( classURL.getFile(), "UTF-8" ) );
        }
        catch ( UnsupportedEncodingException e )
        {
            throw new RuntimeException( "This test assumes the use of default encoding: " + e.getMessage(), e );
        }
    }

    @Override
    public void tearDown()
        throws Exception
    {
        super.tearDown();
        
        Nexus.stop();
    }

    
    
    
}
