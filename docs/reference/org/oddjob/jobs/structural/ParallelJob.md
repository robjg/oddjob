[HOME](../../../../README.md)
# parallel

A job which executes it's child jobs in parallel.


Once the child jobs are submitted, Oddjob's thread of execution continues
on out of this job. The state is set to ACTIVE and will continue to
change depending on the state of the child Jobs. The <code>join</code>
property can be used to hold the thread of execution until the
submitted jobs have finished executing - but it's use is discouraged.
See the property documentation below for more information.

The state of job, including its modification by the
<code>stateOperator</code> property is identical to [sequential](../../../../org/oddjob/jobs/structural/SequentialJob.md)
and is well documented there. Likewise with the transient property.

### Property Summary

| Property | Description |
| -------- | ----------- |
| [executorService](#propertyexecutorservice) | The ExecutorService to use. | 
| [jobs](#propertyjobs) | The child jobs. | 
| [join](#propertyjoin) | Should the execution thread of this job wait for the execution threads of the child jobs. | 
| [name](#propertyname) | A name, can be any text. | 
| [stateOperator](#propertystateoperator) | Set the way the children's state is evaluated and reflected by the parent. | 
| [stop](#propertystop) | Read only view of the internal stop flag. | 
| [transient](#propertytransient) | Is this job transient. | 


### Example Summary

| Title | Description |
| ----- | ----------- |
| [Example 1](#example1) | Two jobs running in parallel. |
| [Example 2](#example2) | Two services started in parallel. |
| [Example 3](#example3) | Examples elsewhere. |


### Property Detail
#### executorService <a name="propertyexecutorservice"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ELEMENT</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>No.</td></tr>
</table>

The ExecutorService to use. This will
be automatically set by Oddjob.

#### jobs <a name="propertyjobs"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ELEMENT</td></tr>
      <tr><td><i>Access</i></td><td>WRITE_ONLY</td></tr>
      <tr><td><i>Required</i></td><td>No, but pointless if missing.</td></tr>
</table>

The child jobs.

#### join <a name="propertyjoin"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ATTRIBUTE</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>No. Defaults to false</td></tr>
</table>

Should the execution thread of this job wait
for the execution threads of the child jobs.


This property
re-introduces the default behaviour of parallel before version 1.0.
Behaviour was changed to encourage the use of event driven
configuration that didn't cause a thread to wait by using
[cascade](../../../../org/oddjob/state/CascadeJob.md) or
[scheduling:trigger](../../../../org/oddjob/scheduling/Trigger.md).


There are situations where this is really convenient as otherwise
large reworking of the configuration is required. If possible -
it is better practice to try and use the job state.

#### name <a name="propertyname"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ATTRIBUTE</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>No.</td></tr>
</table>

A name, can be any text.

#### stateOperator <a name="propertystateoperator"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ATTRIBUTE</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>No, default is ACTIVE.</td></tr>
</table>

Set the way the children's state is
evaluated and reflected by the parent. Values can be WORST,
ACTIVE, or SERVICES.

#### stop <a name="propertystop"></a>

<table style='font-size:smaller'>
      <tr><td><i>Access</i></td><td>READ_ONLY</td></tr>
</table>

Read only view of the internal stop flag.
This flag is cleared with a reset.

#### transient <a name="propertytransient"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ATTRIBUTE</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>No, default is false.</td></tr>
</table>

Is this job transient. If true state will not
be persisted.


### Examples
#### Example 1 <a name="example1"></a>

Two jobs running in parallel. Note that the order of execution of the
two child jobs is not predictable.

```xml
<oddjob>
    <job>
        <parallel>
            <jobs>
                <echo>This runs in parallel</echo>
                <echo>With this which could be displayed first!</echo>
            </jobs>
        </parallel>
    </job>
</oddjob>
```


#### Example 2 <a name="example2"></a>

Two services started in parallel. This might be quite useful if the
services took a long time to start - maybe because they loaded a lot
of data into a cache for instance.

```xml
<?xml version="1.0" encoding="UTF-8" standalone="no"?>
<oddjob>
    <job>
        <cascade cascadeOn="COMPLETE">
            <jobs>
                <parallel>
                    <jobs>
                        <bean class="org.oddjob.jobs.structural.ServiceManagerTest$Lights" id="lights"/>
                        <bean class="org.oddjob.jobs.structural.ServiceManagerTest$MachineThatGoes" goes="ping" id="machine"/>
                    </jobs>
                </parallel>
                <echo><![CDATA[The lights are ${lights.are} and the machine goes ${machine.goes}.]]></echo>
            </jobs>
        </cascade>
    </job>
</oddjob>
```


The [cascade](../../../../org/oddjob/state/CascadeJob.md) will execute the final job only once both services
have started, and it will continue be in a STARTED after execution has
completed.


Adding a SERVICES stateOperator property will mean that parallel is
COMPLETE once the services have started and so the whole cascade shows
as complete.

```xml
<?xml version="1.0" encoding="UTF-8" standalone="no"?>
<oddjob>
    <job>
        <cascade>
            <jobs>
                <parallel stateOperator="SERVICES">
                    <jobs>
                        <bean class="org.oddjob.jobs.structural.ServiceManagerTest$Lights" id="lights"/>
                        <bean class="org.oddjob.jobs.structural.ServiceManagerTest$MachineThatGoes" goes="ping" id="machine"/>
                    </jobs>
                </parallel>
                <echo><![CDATA[The lights are ${lights.are} and the machine goes ${machine.goes}.]]></echo>
            </jobs>
        </cascade>
    </job>
</oddjob>
```


#### Example 3 <a name="example3"></a>

Examples elsewhere.

- [throttle](../../../../org/oddjob/scheduling/ExecutorThrottleType.md) has an example of limiting the number of concurrently executing jobs.



-----------------------

<div style='font-size: smaller; text-align: center;'>(c) R Gordon Ltd 2005 - Present</div>
