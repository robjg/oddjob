[HOME](../../../../README.md)
# task-request

This job requests a task be performed
with optional properties.

### Property Summary

| Property | Description |
| -------- | ----------- |
| [join](#propertyjoin) | Wait for the target job to finish executing. | 
| [name](#propertyname) | A name, can be any text. | 
| [properties](#propertyproperties) | Properties to execute the task with. | 
| [response](#propertyresponse) |  | 
| [stop](#propertystop) | This flag is set by the stop method and should be examined by any Stoppable jobs in their processing loops. | 
| [taskExecutor](#propertytaskExecutor) | The job to start | 


### Example Summary

| Title | Description |
| ----- | ----------- |
| [Example 1](#example1) | Greeting people by name. |


### Property Detail
#### join <a name="propertyjoin"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ATTRIBUTE</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>No, defaults to false.</td></tr>
</table>

Wait for the target job to finish executing.

#### name <a name="propertyname"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ATTRIBUTE</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>No.</td></tr>
</table>

A name, can be any text.

#### properties <a name="propertyproperties"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ELEMENT</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>No.</td></tr>
</table>

Properties to execute the task with.

#### response <a name="propertyresponse"></a>

<table style='font-size:smaller'>
      <tr><td><i>Access</i></td><td>READ_ONLY</td></tr>
</table>



#### stop <a name="propertystop"></a>

<table style='font-size:smaller'>
      <tr><td><i>Access</i></td><td>READ_ONLY</td></tr>
      <tr><td><i>Required</i></td><td>Read Only.</td></tr>
</table>

This flag is set by the stop method and should
be examined by any Stoppable jobs in their processing loops.

#### taskExecutor <a name="propertytaskExecutor"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ATTRIBUTE</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>Yes.</td></tr>
</table>

The job to start


### Examples
#### Example 1 <a name="example1"></a>

Greeting people by name. Three Task Requests call the
[task-service](../../../../org/oddjob/jobs/tasks/TaskExecutionService.md) with different names.

```xml
<?xml version="1.0" encoding="UTF-8" standalone="no"?>
<oddjob>
    <job>
        <sequential>
            <jobs>
                <task-service id="hello-service">
                    <requests>
                        <input-text prompt="Name" property="some.name"/>
                    </requests>
                    <job>
                        <echo><![CDATA[Hello ${some.name}.]]></echo>
                    </job>
                </task-service>
                <task-request taskExecutor="${hello-service}">
                    <properties>
                        <properties>
                            <values>
                                <value key="some.name" value="Rod"/>
                            </values>
                        </properties>
                    </properties>
                </task-request>
                <task-request taskExecutor="${hello-service}">
                    <properties>
                        <properties>
                            <values>
                                <value key="some.name" value="Jane"/>
                            </values>
                        </properties>
                    </properties>
                </task-request>
                <task-request taskExecutor="${hello-service}">
                    <properties>
                        <properties>
                            <values>
                                <value key="some.name" value="Freddy"/>
                            </values>
                        </properties>
                    </properties>
                </task-request>
            </jobs>
        </sequential>
    </job>
</oddjob>

```



-----------------------

<div style='font-size: smaller; text-align: center;'>(c) R Gordon Ltd 2005 - Present</div>
