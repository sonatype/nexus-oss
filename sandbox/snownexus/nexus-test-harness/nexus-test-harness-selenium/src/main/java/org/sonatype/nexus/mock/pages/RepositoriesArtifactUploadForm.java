package org.sonatype.nexus.mock.pages;

import java.io.File;

import org.sonatype.nexus.mock.components.Button;
import org.sonatype.nexus.mock.components.Checkbox;
import org.sonatype.nexus.mock.components.Combobox;
import org.sonatype.nexus.mock.components.Component;
import org.sonatype.nexus.mock.components.TextField;

import com.thoughtworks.selenium.Selenium;

public class RepositoriesArtifactUploadForm
    extends Component
{

    public enum Definition
    {
        POM( 0 ), GAV( 1 );

        private int position;

        private Definition( int position )
        {
            this.position = position;
        }
    }

    private Combobox definition;

    private Button uploadPomButton;

    private TextField pomFile;

    private Checkbox autoguess;

    private TextField groupId;

    private TextField artifactId;

    private TextField version;

    private Combobox packaging;

    private Button uploadArtifactButton;

    private TextField artifactFile;

    private TextField classifier;

    private TextField extension;

    private Button addButton;

    private Button removeButton;

    private Button removeAllButton;

    private Button uploadButton;

    private Button resetButton;

    private TextField uploadPomInput;

    public RepositoriesArtifactUploadForm( Selenium selenium, String expression )
    {
        super( selenium, expression );

        definition = new Combobox( selenium, expression + ".find('name', 'gavDefinition')[0]" );

        uploadPomButton = new Button( selenium, expression + ".find('name', 'uploadPomButton')[0]" );
        uploadPomInput = new TextField( selenium, uploadPomButton.getExpression() + ".inputFileEl" );
        uploadPomInput.idFunction = ".id";
        pomFile = new TextField( selenium, expression + ".find('name', 'pomnameField')[0]" );

        autoguess = new Checkbox( selenium, expression + ".find{'name', 'autoguess'}[0]" );
        groupId = new TextField( selenium, expression + ".find('name', 'g')[0]" );
        artifactId = new TextField( selenium, expression + ".find('name', 'a')[0]" );
        version = new TextField( selenium, expression + ".find('name', 'v')[0]" );
        packaging = new Combobox( selenium, expression + ".find('name', 'p')[0]" );

        uploadArtifactButton = new Button( selenium, expression + ".find('name', 'uploadArtifactButton')[0]" );
        artifactFile = new TextField( selenium, expression + ".find('name', 'filenameField')[0]" );
        classifier = new TextField( selenium, expression + ".find('name', 'classifier')[0]" );
        extension = new TextField( selenium, expression + ".find('name', 'extension')[0]" );

        addButton = new Button( selenium, expression + ".findById('add-button')" );
        removeButton = new Button( selenium, expression + ".findById('button-remove')" );
        removeAllButton = new Button( selenium, expression + ".findById('button-remove-all')" );

        uploadButton = new Button( selenium, expression + ".findById('upload-button')" );
        resetButton = new Button( selenium, expression + ".findById('reset-all-button')" );
    }

    public RepositoriesArtifactUploadForm selectDefinition( Definition def )
    {
        definition.select( def.position );

        return this;
    }

    public RepositoriesArtifactUploadForm uploadPom( File pom )
    {
        selenium.answerOnNextPrompt( pom.getAbsolutePath() );
        // selenium.type( uploadPomInput.getXPath(), pom.getAbsolutePath() );
        selenium.click( uploadPomInput.getXPath() );
        // uploadPomButton.click();

        if ( selenium.isPromptPresent() )
        {
            return this;
        }

        return null;
    }

    public RepositoriesArtifactUploadForm uploadArtifact( File artifact, String classifier, String extension )
    {
        selenium.answerOnNextPrompt( artifact.getAbsolutePath() );

        uploadArtifactButton.click();

        if ( !selenium.isPromptPresent() )
        {
            return null;
        }

        if ( classifier != null )
        {
            this.classifier.type( classifier );
        }
        if ( extension != null )
        {
            this.extension.type( extension );
        }

        this.addButton.click();

        return this;
    }

    public Combobox getDefinition()
    {
        return definition;
    }

    public void setDefinition( Combobox definition )
    {
        this.definition = definition;
    }

    public Button getUploadPomButton()
    {
        return uploadPomButton;
    }

    public void setUploadPomButton( Button uploadPomButton )
    {
        this.uploadPomButton = uploadPomButton;
    }

    public TextField getPomFile()
    {
        return pomFile;
    }

    public void setPomFile( TextField pomFile )
    {
        this.pomFile = pomFile;
    }

    public Checkbox getAutoguess()
    {
        return autoguess;
    }

    public void setAutoguess( Checkbox autoguess )
    {
        this.autoguess = autoguess;
    }

    public TextField getGroupId()
    {
        return groupId;
    }

    public void setGroupId( TextField groupId )
    {
        this.groupId = groupId;
    }

    public TextField getArtifactId()
    {
        return artifactId;
    }

    public void setArtifactId( TextField artifactId )
    {
        this.artifactId = artifactId;
    }

    public TextField getVersion()
    {
        return version;
    }

    public void setVersion( TextField version )
    {
        this.version = version;
    }

    public Combobox getPackaging()
    {
        return packaging;
    }

    public void setPackaging( Combobox packaging )
    {
        this.packaging = packaging;
    }

    public Button getUploadArtifactButton()
    {
        return uploadArtifactButton;
    }

    public void setUploadArtifactButton( Button uploadArtifactButton )
    {
        this.uploadArtifactButton = uploadArtifactButton;
    }

    public TextField getArtifactFile()
    {
        return artifactFile;
    }

    public void setArtifactFile( TextField artifactFile )
    {
        this.artifactFile = artifactFile;
    }

    public TextField getClassifier()
    {
        return classifier;
    }

    public void setClassifier( TextField classifier )
    {
        this.classifier = classifier;
    }

    public TextField getExtension()
    {
        return extension;
    }

    public void setExtension( TextField extension )
    {
        this.extension = extension;
    }

    public Button getAddButton()
    {
        return addButton;
    }

    public void setAddButton( Button addButton )
    {
        this.addButton = addButton;
    }

    public Button getRemoveButton()
    {
        return removeButton;
    }

    public void setRemoveButton( Button removeButton )
    {
        this.removeButton = removeButton;
    }

    public Button getRemoveAllButton()
    {
        return removeAllButton;
    }

    public void setRemoveAllButton( Button removeAllButton )
    {
        this.removeAllButton = removeAllButton;
    }

    public Button getUploadButton()
    {
        return uploadButton;
    }

    public void setUploadButton( Button uploadButton )
    {
        this.uploadButton = uploadButton;
    }

    public Button getResetButton()
    {
        return resetButton;
    }

    public void setResetButton( Button resetButton )
    {
        this.resetButton = resetButton;
    }

}
