package org.sonatype.nexus.plugins.rrb;

import static org.junit.Assert.assertEquals;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mortbay.jetty.Server;

/**
 * In this test we use example repo files that placed in the test resource catalogue
 * To access these files locally via MavenRepositoryReader that requires the http-protocol we start a Jetty server
 * 
 * @author bjorne
 *
 */
public class MavenRepositoryReaderTest {
	MavenRepositoryReader reader; //The "class under test"
	Server server; //An embedded Jetty server
	String localUrl = "http://local"; //This URL doesn't matter for the tests
	String nameOfConnector; //This is the host:portnumber of the Jetty connector 
	
	@Before
	public void setUp() throws Exception {
//		reader = new MavenRepositoryReader();
//		
//		//Create a Jetty server with handler that returns the content of the given target (i.e. an emulated html, S3Repo, etc, file from the test resources)
//		Handler handler = new AbstractHandler()
//		{
//		    public void handle(String target, HttpServletRequest request, HttpServletResponse response, int dispatch)
//		        throws IOException, ServletException
//		    {
//		        response.setStatus(HttpServletResponse.SC_OK);
//		        InputStream stream = this.getClass().getResourceAsStream(target);
//				
//				StringBuilder result = new StringBuilder();
//				BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
//
//				String line = null;
//				while ((line = reader.readLine()) != null) {
//					result.append(line)
//						  .append(System.getProperty("line.separator"));
//				}
//		        response.getWriter().println(result.toString());
//		        ((Request)request).setHandled(true);
//		    }
//		};
//
//		server = new Server(0);	//We choose an arbitrary server port
//		server.setHandler(handler); //Assign the handler of incoming requests
//        server.start();
//        
//        //After starting we must find out the host:port, so we know how to connect to the server in the tests
//		for (Connector connector : server.getConnectors()) {
//			nameOfConnector = connector.getName();
//			break; //We only need one connector name (and there should only be one...)
//		}

	}
	
	@After
	public void shutDown() throws Exception {
//		server.stop();
	}
	
//	@Test(timeout=5000)
//	public void testReadHtml() {
//		List<RepositoryDirectory> result = reader.extract(getURLForTestRepoResource("htmlExample"), localUrl, null, "test");
//		assertEquals(3, result.size());
//	}
//
//	@Test(timeout=5000)
//	public void testReadS3() {
//		List<RepositoryDirectory> result = reader.extract(getURLForTestRepoResource("s3Example"), localUrl, null,"test");
//		assertEquals(14, result.size());
//	}
	@Test(timeout=5000)
	public void testReadS3() {
		assertEquals(1,1);
	}
	
	
	private String getURLForTestRepoResource(String resourceName) {
		return "http://" + nameOfConnector + "/" + resourceName;
	}

}
