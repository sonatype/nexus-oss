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

    private static final String NEXUS_SCM_URL_BASE =
        "http://svn.sonatype.org/nexus/trunk/nexus/nexus-test-harness/nexus-test-harness-launcher/src/test/java/";

    public void writeReport( List<ReportBean> beans )
    {

        System.out.println( "|| Test Name || Description || JIRA  || Implemented ||" );
        for ( ReportBean bean : beans )
        {
            String scmUrl =
                "[" + bean.getJavaClass().getName() + "|" + NEXUS_SCM_URL_BASE
                    + bean.getJavaClass().getPackage().replaceAll( "\\.", "/" ) + "/" + bean.getJavaClass().getName()
                    + ".java]";

            String jiraUrl = "[" + bean.getTestId() + "|" + NEXUS_BUG_URL_BASE + bean.getTestId() + "]";
            String row =
                " | " + scmUrl + " | " + convertAnchor( bean.getJavaClass().getComment() ) + " | " + jiraUrl
                    + " | (/) |";

            System.out.println( row );
        }
        System.out.flush();
    }

    /**
     * If this was actually going to be used, this would need a better solution.
     * 
     * @param stringWithAnchor
     * @return
     */
    private static String convertAnchor( String stringWithAnchor )
    {
        if ( stringWithAnchor.contains( "<a href='" ) )
        {

            int start = stringWithAnchor.indexOf( "<a href='" ) + 9;
            int end = stringWithAnchor.lastIndexOf( "</a>" );

            String url = stringWithAnchor.substring( start, end ).split( "'>" )[0];
            String text = stringWithAnchor.substring( start, end ).split( "'>" )[1];

            String wikiLink = "[" + text + "|" + url + "]";

            String result = stringWithAnchor.replaceAll( "<a href='.*'>.*</a>", wikiLink );
            return result;

        }
        return stringWithAnchor;

    }

    public static void main( String[] args )
    {
        String data =
            "asdfa <a href='https://docs.sonatype.com/display/NX/Nexus+Test-Harness'>Nexus Test-Harness</a> asdf.";

        System.out.println( convertAnchor( data ) );
    }

}
