[HOME](../../../README.md)
# events:trigger

Trigger on an event. This is a work in progress replacement
for [scheduling:trigger](../../../org/oddjob/scheduling/Trigger.md). The intention being that it has the ability
to fire off any event, not just a state change.


The job has two children; the first being the source of the event that causes
the trigger, and the second is the job that is run as the result of the trigger
firing.



### Property Summary

| Property | Description |
| -------- | ----------- |
| [beDestination](#propertybedestination) | This is to be a destination. | 
| [eventSource](#propertyeventsource) | The source of events. | 
| [jobs](#propertyjobs) | The child jobs. | 
| [name](#propertyname) | A name, can be any text. | 
| [stop](#propertystop) | Read only view of the internal stop flag. | 
| [to](#propertyto) | Provide the event to a Bean Bus style consumer. | 
| [trigger](#propertytrigger) | The trigger event. | 


### Example Summary

| Title | Description |
| ----- | ----------- |
| [Example 1](#example1) | A trigger expression based on the state of some jobs. |
| [Example 2](#example2) | A trigger as a destination in Bean Bus. |
| [Example 3](#example3) | Trigger with the first result of a Bean Bus pipeline. |


### Property Detail
#### beDestination <a name="propertybedestination"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ATTRIBUTE</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>No.</td></tr>
</table>

This is to be a destination. A destination is a component in a
[bus:bus](../../../org/oddjob/beanbus/bus/BasicBusService.md) pipeline.

#### eventSource <a name="propertyeventsource"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ELEMENT</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>No.</td></tr>
</table>

The source of events. If this is not set the first child component is assumed
to be the Event Source, unless [org.oddjob.events.EventJobBase](http://rgordon.co.uk/oddjob/1.6.0/api/org/oddjob/events/EventJobBase.html) is set.

#### jobs <a name="propertyjobs"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ELEMENT</td></tr>
      <tr><td><i>Access</i></td><td>WRITE_ONLY</td></tr>
      <tr><td><i>Required</i></td><td>No, but pointless if missing.</td></tr>
</table>

The child jobs.

#### name <a name="propertyname"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ATTRIBUTE</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>No.</td></tr>
</table>

A name, can be any text.

#### stop <a name="propertystop"></a>

<table style='font-size:smaller'>
      <tr><td><i>Access</i></td><td>READ_ONLY</td></tr>
</table>

Read only view of the internal stop flag.
This flag is cleared with a reset.

#### to <a name="propertyto"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ELEMENT</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>No.</td></tr>
</table>

Provide the event to a Bean Bus style consumer.

#### trigger <a name="propertytrigger"></a>

<table style='font-size:smaller'>
      <tr><td><i>Access</i></td><td>READ_ONLY</td></tr>
      <tr><td><i>Required</i></td><td>Read only.</td></tr>
</table>

The trigger event.


### Examples
#### Example 1 <a name="example1"></a>

A trigger expression based on the state of some jobs.

```xml
<oddjob
>
    <job>
        <sequential name="Trigger on Two Things">
            <jobs>
                <bean class="org.oddjob.events.Trigger" id="trigger">
                    <jobs>
                        <bean class="org.oddjob.state.expr.StateExpressionType">
thing1 is success and thing2 is success and not (thing3 is success or thing4 is success)
                        </bean>
                        <echo id="notify"
                              name="Triggered Job">You ran two things!</echo>
                    </jobs>
                </bean>
                <folder name="A Folder of Jobs">
                    <jobs>
                        <echo id="thing1"
                              name="Run me!">Thank you</echo>
                        <echo id="thing2"
                              name="Run me!">Thank you</echo>
                        <echo id="thing3"
                              name="Don't Run me!">Uh oh!</echo>
                        <echo id="thing4"
                              name="Don't Run me!">Uh oh!</echo>
                    </jobs>
                </folder>
            </jobs>
        </sequential>
    </job>
</oddjob>
```


#### Example 2 <a name="example2"></a>

A trigger as a destination in Bean Bus. The queue is required to keep the bus open while the triggered
job completes. Using just a bus driver would cause the bus to be closed when the driver completes and this
might not give time for the triggered job to complete because it happens asynchronously. The solution is to
make Trigger flushable and not let flush complete until the triggered job completes.

```xml
<?xml version="1.0" encoding="UTF-8" standalone="no"?>
<oddjob>
    <job>
        <parallel>
            <jobs>
                <bus:queue id="queue" xmlns:bus="oddjob:beanbus"/>
                <bus:bus xmlns:bus="oddjob:beanbus">
                    <of>
                        <bus:driver>
                            <values>
                                <value value="${queue}"/>
                            </values>
                        </bus:driver>
                        <bus:filter id="filter">
                            <predicate>
                                <bean class="org.oddjob.events.TriggerTest$OnlyApple"/>
                            </predicate>
                        </bus:filter>
                        <events:trigger beDestination="true" id="trigger" xmlns:events="oddjob:events">
                            <jobs>
                                <sequential>
                                    <jobs>
                                        <echo id="result">
                                            <![CDATA[Result: ${trigger.trigger}]]>
                                        </echo>
                                        <stop job="${queue}" name="Stop Queue"/>
                                    </jobs>
                                </sequential>
                            </jobs>
                        </events:trigger>
                    </of>
                </bus:bus>
                <folder>
                    <jobs>
                        <set id="put1" name="Put Banana">
                            <values>
                                <value key="queue.put" value="banana"/>
                            </values>
                        </set>
                        <set id="put2" name="Put Apple">
                            <values>
                                <value key="queue.put" value="Apple"/>
                            </values>
                        </set>
                    </jobs>
                </folder>
            </jobs>
        </parallel>
    </job>
</oddjob>
```


#### Example 3 <a name="example3"></a>

Trigger with the first result of a Bean Bus pipeline.

```xml
<?xml version="1.0" encoding="UTF-8" standalone="no"?>
<oddjob>
    <job>
        <events:trigger id="trigger" xmlns:events="oddjob:events">
            <jobs>
                <bus:driver xmlns:bus="oddjob:beanbus">
                    <values>
                        <list>
                            <values>
                                <value value="foo"/>
                            </values>
                        </list>
                    </values>
                </bus:driver>
                <echo id="echo">
                    <![CDATA[${trigger.trigger}]]>
                </echo>
            </jobs>
        </events:trigger>
    </job>
</oddjob>
```



-----------------------

<div style='font-size: smaller; text-align: center;'>(c) R Gordon Ltd 2005 - Present</div>
