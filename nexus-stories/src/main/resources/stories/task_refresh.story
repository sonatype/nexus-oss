Story: refreshing the task ui to reflect current server state

Narrative:
In order to see the current state of scheduled tasks as known by the server
As a Nexus Administrator
I want to refresh the scheduled tasks ui manually

Scenario: refreshing default behavior

Given a User is viewing the Scheduled tasks ui
When a user clicks Refresh button
Then no tasks are selected
And the Scheduled Task Configuration south panel displays "Select a scheduled task to edit it, or click "Add" to create a new one."
And the task toolbar buttons return to their default enabled / disabled state
And the information displayed in the tasks table is the current information known to the server at the time of refresh

Scenario: refreshing when one or more unsaved edited tasks are present in task list

Given one or more existing edited unsaved tasks are present in task list
When a user clicks Refresh Button
Then any unsaved edits are discarded

Scenario: refreshing when one or more new, never saved tasks are present in task list

Given one or more new, never saved tasks are present in task list
When a user clicks the Refresh Button
Then any unsaved new tasks are removed from the task list