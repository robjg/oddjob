[HOME](../../../README.md)
# schedules:list

Provide a schedule based on a list of schedules. All schedules are
evaluated and that schedule which is due to start first is used.

### Property Summary

| Property | Description |
| -------- | ----------- |
| [schedules](#propertyschedules) | The list of schedules. | 


### Example Summary

| Title | Description |
| ----- | ----------- |
| [Example 1](#example1) | Schedule on Monday and a Friday. |


### Property Detail
#### schedules <a name="propertyschedules"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ELEMENT</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>No, but pointless if missing.</td></tr>
</table>

The list of schedules.


### Examples
#### Example 1 <a name="example1"></a>

Schedule on Monday and a Friday.

```xml
<schedules:list xmlns:schedules="http://rgordon.co.uk/oddjob/schedules">
    <schedules>
        <schedules:weekly on="Monday"/>
        <schedules:weekly on="Friday"/>
    </schedules>
</schedules:list>
```



-----------------------

<div style='font-size: smaller; text-align: center;'>(c) R Gordon Ltd 2005 - Present</div>
