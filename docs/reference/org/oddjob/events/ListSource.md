[HOME](../../../README.md)
# events:list

An event source that aggregates a list of child event sources. The
events are aggregated according to the provided [org.oddjob.events.EventOperator](http://rgordon.co.uk/oddjob/1.6.0/api/org/oddjob/events/EventOperator.html) which defaults to
[org.oddjob.events.AllEvents](http://rgordon.co.uk/oddjob/1.6.0/api/org/oddjob/events/AllEvents.html).

### Property Summary

| Property | Description |
| -------- | ----------- |
| [eventOperator](#propertyeventoperator) | Event Operator to filter events. | 
| [last](#propertylast) | The last event to be passed to a consumer. | 
| [name](#propertyname) | A name, can be any text. | 
| [of](#propertyof) | The event sources to combine. | 
| [to](#propertyto) | The destination events will be sent to. | 


### Example Summary

| Title | Description |
| ----- | ----------- |
| [Example 1](#example1) | Trigger on a list of state expressions. |


### Property Detail
#### eventOperator <a name="propertyeventoperator"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ATTRIBUTE</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>No, default to ALL.</td></tr>
</table>

Event Operator to filter events. ANY/ALL.

#### last <a name="propertylast"></a>

<table style='font-size:smaller'>
      <tr><td><i>Access</i></td><td>READ_ONLY</td></tr>
      <tr><td><i>Required</i></td><td>Read only.</td></tr>
</table>

The last event to be passed to a consumer.

#### name <a name="propertyname"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ATTRIBUTE</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>No.</td></tr>
</table>

A name, can be any text.

#### of <a name="propertyof"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ELEMENT</td></tr>
      <tr><td><i>Access</i></td><td>WRITE_ONLY</td></tr>
      <tr><td><i>Required</i></td><td>No, but pointless without.</td></tr>
</table>

The event sources to combine.

#### to <a name="propertyto"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ELEMENT</td></tr>
      <tr><td><i>Access</i></td><td>WRITE_ONLY</td></tr>
      <tr><td><i>Required</i></td><td>Maybe. Set automatically by some parent components.</td></tr>
</table>

The destination events will be sent to.


### Examples
#### Example 1 <a name="example1"></a>

Trigger on a list of state expressions.

```xml
<?xml version="1.0" encoding="UTF-8" standalone="no"?>
<oddjob>
    <job>
        <sequential>
            <jobs>
                <events:when id="when" xmlns:events="oddjob:events">
                    <jobs>
                        <events:list>
                            <of>
                                <events:watch name="Is Job 1 Complete">
                                    <eventSource>
                                        <state:watch xmlns:state="http://rgordon.co.uk/oddjob/state">
                                            <![CDATA[job1 is COMPLETE]]>
                                        </state:watch>
                                    </eventSource>
                                </events:watch>
                                <events:watch name="Is Job 2 Complete">
                                    <eventSource>
                                        <state:watch xmlns:state="http://rgordon.co.uk/oddjob/state">
                                            <![CDATA[job2 is COMPLETE]]>
                                        </state:watch>
                                    </eventSource>
                                </events:watch>
                            </of>
                        </events:list>
                        <echo id="job3" name="Job 3">
                            <![CDATA[It's Done on ${when.trigger}]]>
                        </echo>
                    </jobs>
                </events:when>
                <folder>
                    <jobs>
                        <echo id="job1" name="Job 1">
                            <![CDATA[Hello]]>
                        </echo>
                        <echo id="job2" name="Job 2">
                            <![CDATA[World]]>
                        </echo>
                    </jobs>
                </folder>
            </jobs>
        </sequential>
    </job>
</oddjob>
```



-----------------------

<div style='font-size: smaller; text-align: center;'>(c) R Gordon Ltd 2005 - Present</div>
