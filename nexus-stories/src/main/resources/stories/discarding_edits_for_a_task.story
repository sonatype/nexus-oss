Story: Discarding (cancelling) edit operations for tasks

Scenario: default Cancel button display

Given a task is displayed in task list
When a task is selected
Then the south panel configuration screen displays a Cancel button
And the Cancel button is enabled

Scenario: cancelling edits when new unsaved tasks are in the task list

Given a previously saved task is selected
And no new, unsaved tasks are in the task list
When the user clicks the configuration form Cancel button
Then all edits to the selected task are discarded
And the top most new, unsaved task is selected

Given a new, unsaved task is selected
And another new, unsaved, unselected task is in the task list
When the user clicks the configuration form Cancel button
Then the selected new unsaved task is removed from the task list
And the top most new, unsaved task is selected







