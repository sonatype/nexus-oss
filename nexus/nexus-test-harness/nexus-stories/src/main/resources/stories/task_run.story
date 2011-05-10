Story: Run (execute) a task

Narrative:
In order to execute a manually created task at a unforseen moment in time.
As a task administrator
I want to execute a non-running task manually

Scenario: Run button state

Given the UI displays a RUNNING task
When a RUNNING task is selected
Then the Run button becomes disabled

Given a User is viewing the Scheuled Tasks UI
And one or more tasks are listed
When all tasks become unselected
Then the Run button is disabled

Given a User is viewing the Scheuled Tasks UI
When all no tasks are listed
Then the Run button is disabled

