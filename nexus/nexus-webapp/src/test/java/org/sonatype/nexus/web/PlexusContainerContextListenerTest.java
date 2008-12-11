/**
 * Sonatype Nexus™ [Open Source Version].
 * Copyright © 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at ${thirdpartyurl}.
 *
 * This program is licensed to you under Version 3 only of the GNU General
 * Public License as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * Version 3 for more details.
 *
 * You should have received a copy of the GNU General Public License
 * Version 3 along with this program. If not, see http://www.gnu.org/licenses/.
 */
package org.sonatype.nexus.web;

import java.io.File;

import javax.servlet.http.HttpServlet;

import junit.framework.TestCase;

import com.meterware.httpunit.PostMethodWebRequest;
import com.meterware.httpunit.WebRequest;
import com.meterware.servletunit.InvocationContext;
import com.meterware.servletunit.ServletRunner;
import com.meterware.servletunit.ServletUnitClient;

public class PlexusContainerContextListenerTest extends TestCase {

	protected File webXml;
	protected ServletRunner servletRunner;
	
	protected void setUp(){
		webXml = new File("src/test/resources/httpunit/web.xml");
		try{
			servletRunner = new ServletRunner(webXml);
		}
		catch(Exception e){
			System.out.println("Error while initializing servlet runner:");
			e.printStackTrace();
		}
	}
	
	public void testListener() throws Exception{
		ServletUnitClient client = servletRunner.newClient();
		WebRequest request = new PostMethodWebRequest( "http://localhost/dummyServlet" );
		InvocationContext context = client.newInvocation(request);
		HttpServlet servlet = (HttpServlet)context.getServlet();
		assertNotNull(servlet.getServletContext().getAttribute("plexus"));

	}
}
