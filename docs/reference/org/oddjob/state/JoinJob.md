[HOME](../../../README.md)
# state:join

Waits for a COMPLETE state from it's child job before allowing
the thread of execution to continue.



### Property Summary

| Property | Description |
| -------- | ----------- |
| [job](#propertyjob) | The child job. | 
| [name](#propertyname) | A name, can be any text. | 
| [stop](#propertystop) | Read only view of the internal stop flag. | 
| [timeout](#propertytimeout) |  | 


### Example Summary

| Title | Description |
| ----- | ----------- |
| [Example 1](#example1) | An join that waits for two triggers. |


### Property Detail
#### job <a name="propertyjob"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ELEMENT</td></tr>
      <tr><td><i>Access</i></td><td>WRITE_ONLY</td></tr>
      <tr><td><i>Required</i></td><td>No, but pointless if missing.</td></tr>
</table>

The child job.

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

#### timeout <a name="propertytimeout"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ATTRIBUTE</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
</table>




### Examples
#### Example 1 <a name="example1"></a>

An join that waits for two triggers. In this example another trigger
to run the last job might be a better solution because it wouldn't hog
a thread - but there are situations when join is just simpler.

```xml
<?xml version="1.0" encoding="UTF-8" standalone="no"?>
<oddjob>
    <job>
        <sequential>
            <jobs>
                <folder>
                    <jobs>
                        <state:flag id="apples" xmlns:state="http://rgordon.co.uk/oddjob/state"/>
                        <state:flag id="oranges" xmlns:state="http://rgordon.co.uk/oddjob/state"/>
                    </jobs>
                </folder>
                <state:join id="our-join" xmlns:state="http://rgordon.co.uk/oddjob/state">
                    <job>
                        <sequential>
                            <jobs>
                                <scheduling:trigger on="${apples}" xmlns:scheduling="http://rgordon.co.uk/oddjob/scheduling">
                                    <job>
                                        <echo>Apples</echo>
                                    </job>
                                </scheduling:trigger>
                                <scheduling:trigger on="${oranges}" xmlns:scheduling="http://rgordon.co.uk/oddjob/scheduling">
                                    <job>
                                        <echo>Oranges</echo>
                                    </job>
                                </scheduling:trigger>
                            </jobs>
                        </sequential>
                    </job>
                </state:join>
                <echo id="last-job">And that's that!</echo>
            </jobs>
        </sequential>
    </job>
</oddjob>
```



-----------------------

<div style='font-size: smaller; text-align: center;'>(c) R Gordon Ltd 2005 - Present</div>
