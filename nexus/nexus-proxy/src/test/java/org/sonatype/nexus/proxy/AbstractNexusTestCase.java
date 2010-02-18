/**
 * Sonatype Nexus (TM) Open Source Version.
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at http://nexus.sonatype.org/dev/attributions.html
 * This program is licensed to you under Version 3 only of the GNU General Public License as published by the Free Software Foundation.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License Version 3 for more details.
 * You should have received a copy of the GNU General Public License Version 3 along with this program.
 * If not, see http://www.gnu.org/licenses/.
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc.
 * "Sonatype" and "Sonatype Nexus" are trademarks of Sonatype, Inc.
 */
package org.sonatype.nexus.proxy;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;

import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.codehaus.plexus.context.Context;
import org.codehaus.plexus.logging.LoggerManager;
import org.codehaus.plexus.util.IOUtil;

/**
 * Abstract test case for nexus tests. It is customizing the context and helps with nexus configurations.
 *
 * @author cstamas
 */
public abstract class AbstractNexusTestCase
    extends org.sonatype.nexus.configuration.AbstractNexusTestCase
{
    public static final String PROXY_SERVER_PORT = "proxy.server.port";
    public static final String SECURITY_XML_FILE = "security-xml-file";
    
    @Override
    protected void customizeContext( Context ctx )
    {
        super.customizeContext( ctx );
        
        ctx.put( PROXY_SERVER_PORT, String.valueOf( allocatePort() ) );
        ctx.put( SECURITY_XML_FILE, new File( getConfHomeDir(), "security.xml" ).getAbsolutePath() );
    }
    
    protected String getNexusSecurityConfiguration()
    {
        return getConfHomeDir().getAbsolutePath() + "/security.xml";
    }
    
    protected void copyDefaultSecurityConfigToPlace()
        throws IOException
    {
        this.copyResource( "/META-INF/security/security.xml", getNexusSecurityConfiguration() );
    }

    private int allocatePort()
    {
        ServerSocket ss;
        try
        {
            ss = new ServerSocket( 0 );
        }
        catch ( IOException e )
        {
            return 0;
        }
        int port = ss.getLocalPort();
        try
        {
            ss.close();
        }
        catch ( IOException e )
        {
            // does it matter?
            fail( "Error allocating port " + e.getMessage() );
        }
        return port;
    }

    protected LoggerManager getLoggerManager()
        throws ComponentLookupException
    {
        return getContainer().lookup( LoggerManager.class );
    }

    protected boolean contentEquals( File f1, File f2 )
        throws IOException
    {
        return contentEquals( new FileInputStream( f1 ), new FileInputStream( f2 ) );
    }

    /**
     * Both s1 and s2 will be closed.
     */
    protected boolean contentEquals( InputStream s1, InputStream s2 )
        throws IOException
    {
        try
        {
            return IOUtil.contentEquals( s1, s2 );
        }
        finally
        {
            IOUtil.close( s1 );
            IOUtil.close( s2 );
        }
    }

}
