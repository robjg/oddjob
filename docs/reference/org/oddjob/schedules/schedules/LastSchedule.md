[HOME](../../../../README.md)
# schedules:last

This schedule will return it's last due nested
schedule within the given parent interval.

### Property Summary

| Property | Description |
| -------- | ----------- |
| [refinement](#propertyrefinement) | Provide a refinement to this schedule. | 


### Example Summary

| Title | Description |
| ----- | ----------- |
| [Example 1](#example1) | Last Tuesday or Wednesday of the month, whichever is last. |


### Property Detail
#### refinement <a name="propertyrefinement"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ELEMENT</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>No.</td></tr>
</table>

Provide a refinement to this schedule.


### Examples
#### Example 1 <a name="example1"></a>

Last Tuesday or Wednesday of the month, whichever is last.

```xml
<schedules:monthly xmlns:schedules="http://rgordon.co.uk/oddjob/schedules"
    fromDay="-6" toDay="0">
    <refinement>
        <schedules:last>
            <refinement>
                <schedules:list>
                    <schedules>
                        <schedules:weekly on="Tuesday"/>
                        <schedules:weekly on="Wednesday"/>
                    </schedules>
                </schedules:list>
            </refinement>
        </schedules:last>
    </refinement>
</schedules:monthly>

```



-----------------------

<div style='font-size: smaller; text-align: center;'>(c) R Gordon Ltd 2005 - Present</div>
