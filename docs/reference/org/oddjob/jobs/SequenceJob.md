[HOME](../../../README.md)
# sequence

Provide a sequence number which is
incremented each time the job is executed.

### Property Summary

| Property | Description |
| -------- | ----------- |
| [current](#propertycurrent) | The current sequence number. | 
| [from](#propertyfrom) | The sequence number to start from. | 
| [name](#propertyname) | The name of this job. | 
| [watch](#propertywatch) | This can be any object which will be watched, and when it changes the sequence will be reset. | 


### Example Summary

| Title | Description |
| ----- | ----------- |
| [Example 1](#example1) | Using a sequence in a file name. |


### Property Detail
#### current <a name="propertycurrent"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ATTRIBUTE</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>Set automatically, but can be changed manually.</td></tr>
</table>

The current sequence number.

#### from <a name="propertyfrom"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ATTRIBUTE</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>No, defaults to 0.</td></tr>
</table>

The sequence number to start from.

#### name <a name="propertyname"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ATTRIBUTE</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>No.</td></tr>
</table>

The name of this job.

#### watch <a name="propertywatch"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ATTRIBUTE</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
</table>

This can be any object which
will be watched, and when it changes the sequence
will be reset. This will most likely be a date.


### Examples
#### Example 1 <a name="example1"></a>

Using a sequence in a file name.

```xml
<?xml version="1.0" encoding="UTF-8" standalone="no"?>
<oddjob id="this"
        xmlns:scheduling="http://rgordon.co.uk/oddjob/scheduling"
        xmlns:schedules="http://rgordon.co.uk/oddjob/schedules">
    <job>
        <scheduling:timer id="daily">
            <schedule>
                <schedules:daily/>
            </schedule>
            <job>
                <scheduling:timer>
                    <schedule>
                        <schedules:count count="10"/>
                    </schedule>
                    <job>
                        <sequential>
                            <jobs>
                                <sequence id="seqnum" watch="${daily.current}"/>
                                <variables id="vars">
                                    <seqnumFormatted>
                                        <format format="0000" number="${seqnum.current}"/>
                                    </seqnumFormatted>
                                </variables>
                                <copy name="Create file" to="${work.dir}/sequence${vars.seqnumFormatted}.txt">
                                    <input>
                                        <value value="This text will be in every file."/>
                                    </input>
                                </copy>
                            </jobs>
                        </sequential>
                    </job>
                </scheduling:timer>
            </job>
        </scheduling:timer>
    </job>
</oddjob>
```


The watch property is set to watch when the daily scheule move forward
so the sequence is restarted.


-----------------------

<div style='font-size: smaller; text-align: center;'>(c) R Gordon Ltd 2005 - Present</div>
