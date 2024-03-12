[HOME](../../../../README.md)
# schedules:count

This schedule returns up to count
number of child schedules. It is typically
used to count a number intervals for re-trying something.


If there is more than one count in a schedule a key must be provided
to differentiate their internally store numbers, otherwise the count value
would be shared.


If the nested schedule isn't specified it defaults to
[schedules:now](../../../../org/oddjob/schedules/schedules/NowSchedule.md)

### Property Summary

| Property | Description |
| -------- | ----------- |
| [count](#propertycount) | The number to count to. | 
| [identifier](#propertyidentifier) | If there are more than one count schedules in a schedule then this key is required to differentiate them. | 
| [refinement](#propertyrefinement) | Provide a refinement to this schedule. | 


### Example Summary

| Title | Description |
| ----- | ----------- |
| [Example 1](#example1) | A schedule for 5 times at intervals of 15 minutes. |
| [Example 2](#example2) | A schedule for 3 times each day at 5 minute intervals. |
| [Example 3](#example3) | Nested count schedules. |


### Property Detail
#### count <a name="propertycount"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ATTRIBUTE</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>Yes.</td></tr>
</table>

The number to count to.

#### identifier <a name="propertyidentifier"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ATTRIBUTE</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>No.</td></tr>
</table>

If there are more than one count schedules in a
schedule then this key is required to differentiate them. It can be any
text.

#### refinement <a name="propertyrefinement"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ELEMENT</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>No.</td></tr>
</table>

Provide a refinement to this schedule.


### Examples
#### Example 1 <a name="example1"></a>

A schedule for 5 times at intervals of 15 minutes.

```xml
<schedules:count xmlns:schedules="http://rgordon.co.uk/oddjob/schedules"
    count="5">
    <refinement>
        <schedules:interval interval="00:15"/>
    </refinement>
</schedules:count>
```


#### Example 2 <a name="example2"></a>

A schedule for 3 times each day at 5 minute intervals.

```xml
<schedules:daily xmlns:schedules="http://rgordon.co.uk/oddjob/schedules">
    <refinement>
        <schedules:time from="11:00">
            <refinement>
                <schedules:count count="3">
                    <refinement>
                        <schedules:interval interval="00:05"/>
                    </refinement>
                </schedules:count>
            </refinement>
        </schedules:time>
    </refinement>
</schedules:daily>
```


#### Example 3 <a name="example3"></a>

Nested count schedules. This slightly contrived example would cause a timer
to run a job twice at 1 minute intervals for 3 days. Note that we need a
key on one of the schedules to differentiate them.

```xml
<schedules:count xmlns:schedules="http://rgordon.co.uk/oddjob/schedules"
    identifier="outer" count="6">
    <refinement>
        <schedules:daily from="11:00">
            <refinement>
                <schedules:count count="2">
                    <refinement>
                        <schedules:interval interval="00:01"/>
                    </refinement>
                </schedules:count>
            </refinement>
        </schedules:daily>
    </refinement>
</schedules:count>
```



-----------------------

<div style='font-size: smaller; text-align: center;'>(c) R Gordon Ltd 2005 - Present</div>
