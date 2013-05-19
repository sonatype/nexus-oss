
Story: Adding a new task

Narrative:

In order to create administrative tasks within Nexus that execute at some scheduled time
As a Nexus Administrator
I want to be able to create new tasks to run via the UI

Scenario: Adding a new task default behavior

Given any task is selected
When the add button is clicked
Then a new row is created in the task list
And the new row is visibly selected
And column values are blank except for Name
And the Name column value is "New Scheduled Task"
And the south panel contains default new task field values

Scenario: Adding a new task while another task is already being added

Given a new task is being created but has not been saved
When the Add button is clicked
Then the default add task behavior takes place

