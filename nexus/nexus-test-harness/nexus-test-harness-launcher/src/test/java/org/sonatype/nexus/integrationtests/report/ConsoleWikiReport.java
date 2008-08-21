package org.sonatype.nexus.integrationtests.report;

import java.util.List;

/**
 * The default implementation used to print Confluence wiki markup to the console. I am sick of needed to update <a
 * href='https://docs.sonatype.com/display/Nexus/Automated+Integration+Tests'>this</a> manually.
 */
public class ConsoleWikiReport
    implements TestReport
{

    private static final String NEXUS_BUG_URL_BASE = "http://issues.sonatype.org/browse/";
    private static final String NEXUS_SCM_URL_BASE = "http://svn.sonatype.org/nexus/trunk/nexus/nexus-test-harness/nexus-test-harness-launcher/src/test/java/";
    
    public void writeReport( List<ReportBean> beans )
    {

        System.out.println( "|| Test Name || Description || JIRA  || Implemented ||" );
        for ( ReportBean bean : beans )
        {
            String scmUrl = "["+ bean.getJavaClass().getName() + "|" + NEXUS_SCM_URL_BASE + bean.getJavaClass().getPackage().replaceAll( "\\.", "/" )  + "/" + bean.getJavaClass().getName() + ".java]";
            
            String jiraUrl = "[" + bean.getTestId() + "|"+ NEXUS_BUG_URL_BASE + bean.getTestId() + "]";
            String row =
                " | " + scmUrl + " | " + bean.getJavaClass().getComment() + " | " + jiraUrl
                    + " | (/) |";

            System.out.println( row );

        }

    }

}
