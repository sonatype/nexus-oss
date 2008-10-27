package org.sonatype.nexus.integrationtests.report;

import java.util.List;

public interface TestReport
{

    public void writeReport( List<ReportBean> beans );
    
}
