package org.sonatype.nexus.mock;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

import org.apache.commons.codec.binary.Base64;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.runner.Description;
import org.junit.runner.RunWith;
import org.sonatype.nexus.mock.pages.MainPage;
import org.sonatype.nexus.mock.rest.MockHelper;
import org.sonatype.nexus.mock.util.PropUtil;
import org.sonatype.nexus.mock.util.SocketTestWaitCondition;
import org.sonatype.spice.jscoverage.JsonReportHandler;

import ch.ethz.ssh2.Connection;

import com.thoughtworks.selenium.DefaultSelenium;
import com.thoughtworks.selenium.Selenium;

@Ignore
@RunWith(SeleniumJUnitRunner.class)
public abstract class SeleniumTest extends NexusTestCase {
    protected Selenium selenium;
    protected MainPage main;
    protected Description description;
    private static Connection sshConn;

    private static String getLocalIp() throws SocketException {
        Enumeration<NetworkInterface> e = NetworkInterface.getNetworkInterfaces();
        while (e.hasMoreElements()) {
            NetworkInterface ni = e.nextElement();

            if (!ni.getDisplayName().startsWith("vmnet")) {
                Enumeration<InetAddress> i = ni.getInetAddresses();
                while (i.hasMoreElements()) {
                    InetAddress ia = i.nextElement();
                    if (ia instanceof Inet4Address) {
                        if (!ia.getHostAddress().startsWith("127.0.")) {
                            return ia.getHostAddress();
                        }
                    }
                }
            }
        }

        return "localhost";
    }

    @BeforeClass
    public static void openTunnel() throws Exception {
        NexusTestCase.startNexus();

        if (!new SocketTestWaitCondition("localhost", 4444, 250).checkCondition(0)) {
            if (sshConn == null) {
                int port = PropUtil.get("jettyPort", 12345);

                // spin up SSH connection
                sshConn = new Connection("grid.sonatype.org", PropUtil.get("serverPort", 10023));
                sshConn.connect();

                // authenticate
                boolean usingPersonal = false;
                File pemFile = new File(System.getenv().get("HOME") + "/.ssh/nexus_selenium_rsa");
                String password = null;
                if (!pemFile.exists()) {
                    pemFile = new File(System.getenv().get("HOME") + "/.ssh/id_rsa");
                    usingPersonal = true;
                    password = System.getProperty("sshPassword");
                }

                boolean isAuthenticated = false;
                try {
                    isAuthenticated = sshConn.authenticateWithPublicKey("hudson", pemFile, password);
                } catch (IOException e) {
                    // ignore
                }

                if (!isAuthenticated) {
                    System.err.println("**************************************************************");
                    System.err.println("**************************************************************");
                    System.err.println("");
                    System.err.println("Could not authenticate SSH using key:");
                    System.err.println(pemFile.getPath());
                    if (usingPersonal) {
                        System.err.println("");
                        System.err.println("Perhaps you need to specify the password using -DsshPassword=... ?");
                        System.err.println("");
                        System.err.println("Alternatively, grab the nexus_selenium_rsa private key and put it in ~/.ssh");
                        System.err.println("");
                    }
                    System.err.println("");
                    System.err.println("**************************************************************");
                    System.err.println("**************************************************************");
                }

                System.out.println("Requesting remote port forwarding for port " + port);
                sshConn.requestRemotePortForwarding("", port, "localhost", port);
                sshConn.createLocalPortForwarder(4444, "localhost", 4444);

                Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
                    public void run() {
                        sshConn.close();
                    }
                }));
            }
        }
    }

    @Before
    public void seleniumSetup() throws Exception {
        final String ip = getLocalIp();
        final String seleniumServer = PropUtil.get("seleniumServer", "localhost");
        final int seleniumPort = PropUtil.get("seleniumPort", 4444);
        final String seleniumBrowser = PropUtil.get("seleniumBrowser", "*firefox");
        final Selenium original = new DefaultSelenium(seleniumServer, seleniumPort, seleniumBrowser, "http://localhost:" + PropUtil.get("jettyPort", 12345));

        selenium = (Selenium) Proxy.newProxyInstance(Selenium.class.getClassLoader(), new Class<?>[] { Selenium.class }, new InvocationHandler() {
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                // check assertions on every remote call we do!
                MockHelper.checkAssertions();
                return method.invoke(original, args);
            }
        });
        selenium.start("captureNetworkTraffic=true");
        selenium.getEval("window.moveTo(1,1); window.resizeTo(1021,737);");
        main = new MainPage(selenium);
    }

    @After
    public void seleniumCleanup() throws Exception {
        getCoverage();

        selenium.stop();
    }

    /**
     * Sets the JUnit description for the currently running test.
     *
     * @param description The JUnit description.
     * @see SeleniumJUnitRunner
     */
    public void setDescription(Description description) {
        this.description = description;
    }

    /**
     * Takes a screenshot of the browser and saves it to the target/screenshots directory. The exact name of the file is
     * based on the currently executing test class and method name plus the line number of the source code that called
     * this method.
     *
     * @throws java.io.IOException If the screenshot could not be taken.
     */
    protected void takeScreenshot() throws IOException {
        @SuppressWarnings({"ThrowableInstanceNeverThrown"})
        StackTraceElement ste = new Exception().getStackTrace()[1];
        takeScreenshot("line-" + ste.getLineNumber());
    }

    /**
     * Takes a screenshot of the browser and saves it to the target/screenshots directory. The name is a combination
     * of the currently executing test class and method name, plus the name parameterized supplied when calling this
     * method.
     *
     * @param name A specific name to append to the screenshot file name.
     * @throws IOException If the screenshot could not be taken.
     */
    protected void takeScreenshot(String name) throws IOException {
        File parent = new File("target/screenshots/");
        //noinspection ResultOfMethodCallIgnored
        parent.mkdirs();

        String screen = selenium.captureScreenshotToString();
        FileOutputStream fos = new FileOutputStream(new File(parent, description.getDisplayName() + "-" + name + ".png"));
        fos.write(Base64.decodeBase64(screen.getBytes()));
        fos.close();
    }

    public void captureNetworkTraffic() {
        try {
            File parent = new File("target/network-traffic/");
            //noinspection ResultOfMethodCallIgnored
            parent.mkdirs();

            FileOutputStream fos = new FileOutputStream(new File(parent, description.getDisplayName() + ".txt"));
            fos.write(selenium.captureNetworkTraffic("TODO").getBytes("UTF-8"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void getCoverage()
        throws ComponentLookupException, IOException
    {
        JsonReportHandler handler = lookup( JsonReportHandler.class );
        handler.appendResults( selenium.getEval( "window.jscoverage_serializeCoverageToJSON()" ) );
        handler.persist();
    }
}
