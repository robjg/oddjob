[HOME](../../../../README.md)
# task-service

Provide a very simple task execution service.


The task to be executed is defined by the nested jobs which may use the properties.
which will be defined when executing the tasks.


This implementation only supports the single execution of a task at one time. If
the task is running additional requests to execute the task will be ignored.


Future version will support multiple parallel executions of tasks.

### Property Summary

| Property | Description |
| -------- | ----------- |
| [arooaSession](#propertyarooaSession) |  | 
| [job](#propertyjob) | The job to pass resets on to. | 
| [name](#propertyname) | A name, can be any text. | 
| [parameterInfo](#propertyparameterInfo) |  | 
| [properties](#propertyproperties) |  | 
| [requests](#propertyrequests) |  | 
| [reset](#propertyreset) |  | 
| [response](#propertyresponse) |  | 


### Example Summary

| Title | Description |
| ----- | ----------- |
| [Example 1](#example1) | A Task Service that greets people by name. |


### Property Detail
#### arooaSession <a name="propertyarooaSession"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ELEMENT</td></tr>
      <tr><td><i>Access</i></td><td>WRITE_ONLY</td></tr>
</table>



#### job <a name="propertyjob"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ELEMENT</td></tr>
      <tr><td><i>Access</i></td><td>WRITE_ONLY</td></tr>
      <tr><td><i>Required</i></td><td>Yes.</td></tr>
</table>

The job to pass resets on to.

#### name <a name="propertyname"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ATTRIBUTE</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>No.</td></tr>
</table>

A name, can be any text.

#### parameterInfo <a name="propertyparameterInfo"></a>

<table style='font-size:smaller'>
      <tr><td><i>Access</i></td><td>READ_ONLY</td></tr>
</table>



#### properties <a name="propertyproperties"></a>

<table style='font-size:smaller'>
      <tr><td><i>Access</i></td><td>READ_ONLY</td></tr>
</table>



#### requests <a name="propertyrequests"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ELEMENT</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
</table>



#### reset <a name="propertyreset"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ATTRIBUTE</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
</table>



#### response <a name="propertyresponse"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ATTRIBUTE</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
</table>




### Examples
#### Example 1 <a name="example1"></a>

A Task Service that greets people by name. Three [task-request](../../../../org/oddjob/jobs/tasks/TaskRequest.md)s call the
service with different names.

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
