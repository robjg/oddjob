[HOME](../../../README.md)
# events:watch

Provides a component wrapper around a value type event source such as
[state:watch](../../../org/oddjob/state/expr/StateExpressionType.md).

### Property Summary

| Property | Description |
| -------- | ----------- |
| [eventSource](#propertyeventSource) | The event source being wrapped to be a component. | 
| [name](#propertyname) | A name, can be any text. | 
| [to](#propertyto) | The destination events will be sent to. | 


### Example Summary

| Title | Description |
| ----- | ----------- |
| [Example 1](#example1) | Running a job when two other jobs complete. |


### Property Detail
#### eventSource <a name="propertyeventSource"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ELEMENT</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>Yes.</td></tr>
</table>

The event source being wrapped to be a component.

#### name <a name="propertyname"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ATTRIBUTE</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>No.</td></tr>
</table>

A name, can be any text.

#### to <a name="propertyto"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ELEMENT</td></tr>
      <tr><td><i>Access</i></td><td>WRITE_ONLY</td></tr>
      <tr><td><i>Required</i></td><td>Maybe. Set automatically by some parent components.</td></tr>
</table>

The destination events will be sent to.


### Examples
#### Example 1 <a name="example1"></a>

Running a job when two other jobs complete. Using components allows visibility on the individual event
sources in a UI.

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
