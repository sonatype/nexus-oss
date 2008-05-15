package org.sonatype.nexus.gwt.ui.client.repository;

import org.sonatype.gwt.client.handler.StatusResponseHandler;
import org.sonatype.gwt.client.resource.Representation;
import org.sonatype.nexus.gwt.ui.client.NexusUI;
import org.sonatype.nexus.gwt.ui.client.Util;
import org.sonatype.nexus.gwt.ui.client.constants.RepositoryConstants;
import org.sonatype.nexus.gwt.ui.client.constants.RepositoryMessages;
import org.sonatype.nexus.gwt.ui.client.data.JSONResourceParser;
import org.sonatype.nexus.gwt.ui.client.form.CheckBoxInput;
import org.sonatype.nexus.gwt.ui.client.form.Form;
import org.sonatype.nexus.gwt.ui.client.form.FormModel;
import org.sonatype.nexus.gwt.ui.client.form.HiddenInput;
import org.sonatype.nexus.gwt.ui.client.form.NumberBoxInput;
import org.sonatype.nexus.gwt.ui.client.form.RadioButtonGroup;
import org.sonatype.nexus.gwt.ui.client.form.RadioButtonInput;
import org.sonatype.nexus.gwt.ui.client.form.ResourceFormModel;
import org.sonatype.nexus.gwt.ui.client.form.TextBoxInput;
import org.sonatype.nexus.gwt.ui.client.widget.Header;
import org.sonatype.nexus.gwt.ui.client.widget.Span;

import com.google.gwt.core.client.GWT;
import com.google.gwt.http.client.Request;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.DisclosurePanel;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.RadioButton;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 *
 * @author barath
 */
public abstract class RepositoryPage extends VerticalPanel implements Form {

    protected static final RepositoryConstants i18n = (RepositoryConstants) GWT.create(RepositoryConstants.class);
    protected static final RepositoryMessages i18nMessages = (RepositoryMessages) GWT.create(RepositoryMessages.class);

    private final String repoType;

    private FormModel model = new ResourceFormModel(new JSONResourceParser());

    private boolean editMode;

    private Header header = new Header();
    protected FlexTable flexTable = new FlexTable();
    private TextBox idField;
    private TextArea repostoryDetails = new TextArea();

    protected RepositoryPage(String repoType) {
        this.repoType = repoType;
    }

    public RepositoryPage initComponents() {
        add(header);
        add(flexTable);

        // This is the goal
//        fieldSet = createFieldSet();
//        field = fieldSet.createField();
//        field.setLabel(i18n.id());
//        field.setInput(idField = createTextBox("id"));
//        field.setValidator(xxx);

        addRow(i18n.id(), idField = createTextBox("id"));
        addRow(i18n.name(), createTextBox("name"));

        addTypeSpecificInputs();

        model.addInput("repoType", new HiddenInput(repoType));

        FlowPanel row = new FlowPanel();
        row.add(createSaveButton());
        row.add(createCancelButton());
        add(row);

        repostoryDetails.setWidth("100%");
        repostoryDetails.setHeight("200px");
        add(repostoryDetails);

        return this;
    }

    protected void addCommonInputs() {
        addRow(createCheckBox("allowWrite", i18n.allowWrite()));
        addRow(createCheckBox("browseable", i18n.browseable()));
        addRow(createCheckBox("indexable", i18n.indexable()));
        addRow(createRadioButtonGroup("repoPolicy",
               new String[][] {{"release", i18n.repoPolicyRelease()},
                               {"snapshot", i18n.repoPolicySnapshot()}}));

        FlexTable advancedContent = new FlexTable();

        addRow(advancedContent, i18n.defaultLocalStorageUrl(),
               createTextBox("defaultLocalStorageUrl"));
        addRow(advancedContent, i18n.overrideLocalStorageUrl(),
               createTextBox("overrideLocalStorageUrl"));

        FlowPanel row = new FlowPanel();
        row.add(new Span(i18n.notFoundCacheTTLBefore()));
        row.add(createNumberBox("notFoundCacheTTL"));
        row.add(new Span(" " + i18n.notFoundCacheTTLAfter()));
        addRow(advancedContent, row);

        row = new FlowPanel();
        row.add(new Span(i18n.artifactMaxAgeBefore() + " "));
        row.add(createNumberBox("artifactMaxAge"));
        row.add(new Span(" " + i18n.artifactMaxAgeAfter()));
        addRow(advancedContent, row);

        row = new FlowPanel();
        row.add(new Span(i18n.metadataMaxAgeBefore() + " "));
        row.add(createNumberBox("metadataMaxAge"));
        row.add(new Span(" " + i18n.metadataMaxAgeAfter()));
        addRow(advancedContent, row);

        DisclosurePanel advanced = new DisclosurePanel(i18n.advancedSettings());
        advanced.add(advancedContent);
        addRow(advanced);
    }

    protected void addRow() {
        addRow(flexTable);
    }

    protected void addRow(Widget widget) {
        addRow(flexTable, widget);
    }

    protected void addRow(String label, Widget widget) {
        addRow(flexTable, label, widget);
    }

    protected void addRow(FlexTable table) {
        table.insertRow(table.getRowCount());
    }

    protected void addRow(FlexTable table, Widget widget) {
        addRow(table);
        addCell(table, widget, 2);
    }

    protected void addRow(FlexTable table, String label, Widget widget) {
        addRow(table);
        addCell(table, label);
        addCell(table, widget);
    }

    protected void addCell(String string) {
        addCell(flexTable, string, 1);
    }

    protected void addCell(String string, int span) {
        addCell(flexTable, string, span);
    }

    protected void addCell(FlexTable table, String string) {
        addCell(table, string, 1);
    }

    protected void addCell(FlexTable table, String string, int span) {
        int row = table.getRowCount() - 1;
        int column = table.getCellCount(row);
        table.setText(row, column, string);
        if (span > 1) {
            ((FlexTable.FlexCellFormatter)table.getCellFormatter()).setColSpan(row, column, span);
        }
    }

    protected void addCell(Widget widget) {
        addCell(flexTable, widget, 1);
    }

    protected void addCell(Widget widget, int span) {
        addCell(flexTable, widget, span);
    }

    protected void addCell(FlexTable table, Widget widget) {
        addCell(table, widget, 1);
    }

    protected void addCell(FlexTable table, Widget widget, int span) {
        int row = table.getRowCount() - 1;
        int column = table.getCellCount(row);
        table.setWidget(row, column, widget);
        if (span > 1) {
            ((FlexTable.FlexCellFormatter)table.getCellFormatter()).setColSpan(row, column, span);
        }
    }

    protected TextBox createTextBox(String name) {
        TextBox tb = new TextBox();
        tb.setName(name);
        model.addInput(name, new TextBoxInput(tb));
        return tb;
    }

    protected TextBox createNumberBox(String name) {
        TextBox tb = new TextBox();
        tb.setName(name);
        model.addInput(name, new NumberBoxInput(tb));
        return tb;
    }

    protected CheckBox createCheckBox(String name, String label) {
        CheckBox cb = new CheckBox(label);
        cb.setName(name);
        model.addInput(name, new CheckBoxInput(cb));
        return cb;
    }

    protected RadioButtonGroup createRadioButtonGroup(String name, String[][] options) {
        RadioButton[] buttons = new RadioButton[options.length];
        for (int i = 0; i < options.length; i++) {
            buttons[i] = new RadioButton(options[i][0], options[i][1]);
        }
        model.addInput(name, new RadioButtonInput(buttons));
        return new RadioButtonGroup(buttons);
    }

    protected Button createSaveButton() {
        Button button = new Button(i18n.save());
        button.addClickListener(new ClickListener() {
            public void onClick(Widget sender) {
                JSONObject repo = (JSONObject) model.getFormData();
                repostoryDetails.setText(repo.toString());

                if (editMode) {
                    NexusUI.server.getLocalInstance()
                            .getRepositoriesService().getRepositoryById(idField.getText())
                            .update(new Representation(repo), new StatusResponseHandler() {
                        public void onSuccess() {
                            Window.alert("Repository modified");
                        }
                        public void onError(Request request, Throwable error) {
                            Window.alert("Modify repository failed");
                        }
                    });
                } else {
                    NexusUI.server.getLocalInstance().getRepositoriesService()
                            .createRepository(idField.getText(), new Representation(repo), new StatusResponseHandler() {
                        public void onSuccess() {
                            Window.alert("Repository created");
                        }
                        public void onError(Request request, Throwable error) {
                            Window.alert("Create repository failed");
                        }
                    });
                }
            }
        });
        return button;
    }

    protected Button createCancelButton() {
        Button button = new Button(i18n.cancel());
        button.addClickListener(new ClickListener() {
            public void onClick(Widget sender) {
                NexusUI.openRepositoriesPage();
            }
        });
        return button;
    }

    public void setEditMode(boolean edit) {
        String repoType = i18n.getString(Util.convertToJavaStyle("repoType." + model.getInput("repoType").getValue()));

        this.editMode = edit;
        if (edit) {
            header.setText(i18nMessages.editing(repoType, idField.getText()));
        } else {
            header.setText(i18nMessages.creating(repoType));
            model.reset();
        }

        idField.setEnabled(!edit);
        ((FlexTable)idField.getParent()).getRowFormatter().setVisible(0, !edit);
    }

    public void load(Object formData) {
        model.setFormData(formData);
        repostoryDetails.setText(formData.toString());
    }

    protected FormModel getModel() {
        return model;
    }

    protected abstract void addTypeSpecificInputs();

}
