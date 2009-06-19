package org.sonatype.nexus.mock.pages;

import org.sonatype.nexus.mock.components.Component;
import org.sonatype.nexus.mock.components.TextField;

import com.thoughtworks.selenium.Selenium;

public class ArtifactInformationPanel
    extends Component
{

    private TextField groupId;

    private TextField artifactId;

    private TextField version;

    public ArtifactInformationPanel( Selenium selenium, String expression )
    {
        super( selenium, expression );

        groupId = new TextField( selenium, expression + ".find('name', 'groupId')[0]" );
        artifactId = new TextField( selenium, expression + ".find('name', 'artifactId')[0]" );
        version = new TextField( selenium, expression + ".find('name', 'version')[0]" );
    }

    public TextField getGroupId()
    {
        return groupId;
    }

    public TextField getArtifactId()
    {
        return artifactId;
    }

    public TextField getVersion()
    {
        return version;
    }

}
