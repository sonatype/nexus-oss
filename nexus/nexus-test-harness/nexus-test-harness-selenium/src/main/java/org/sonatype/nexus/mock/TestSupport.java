package org.sonatype.nexus.mock;

/**
 * Support for tests.
 * 
 * @since 1.9.2
 */
public abstract class TestSupport {
 
    /**
     * @return true if JSCoverage reporting enabled?
     */
    public static boolean isJSCoverageEnabled(){
        final String enable = System.getProperty("jscoverage");
        return enable == null || enable.equals("true");
    }
}
