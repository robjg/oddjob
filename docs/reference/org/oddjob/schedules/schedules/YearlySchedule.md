[HOME](../../../../README.md)
# schedules:yearly

A schedule for a range of months, or a month. The month
is specified as an integer between 1 and 12 where 1 is January and
12 is December.

### Property Summary

| Property | Description |
| -------- | ----------- |
| [fromDate](#propertyfromdate) | The from month and day. | 
| [fromMonth](#propertyfrommonth) | The from month. | 
| [inMonth](#propertyinmonth) | The month in which this schedule is for. | 
| [onDate](#propertyondate) | The day on which this schedule is for. | 
| [refinement](#propertyrefinement) | Provide a refinement to this schedule. | 
| [toDate](#propertytodate) | The to month and day. | 
| [toMonth](#propertytomonth) | The to month. | 


### Example Summary

| Title | Description |
| ----- | ----------- |
| [Example 1](#example1) | A schedule for two different month ranges. |
| [Example 2](#example2) | A from day of year to day of year example. |
| [Example 3](#example3) | An on day of year example. |


### Property Detail
#### fromDate <a name="propertyfromdate"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ATTRIBUTE</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
</table>

The from month and day.

#### fromMonth <a name="propertyfrommonth"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ATTRIBUTE</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>No, defaults to 1 (January).</td></tr>
</table>

The from month.

#### inMonth <a name="propertyinmonth"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ATTRIBUTE</td></tr>
      <tr><td><i>Access</i></td><td>WRITE_ONLY</td></tr>
      <tr><td><i>Required</i></td><td>No.</td></tr>
</table>

The month in which this schedule is for.
This has the same effect as setting fromMonth and toMonth to the same thing.

#### onDate <a name="propertyondate"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ATTRIBUTE</td></tr>
      <tr><td><i>Access</i></td><td>WRITE_ONLY</td></tr>
      <tr><td><i>Required</i></td><td>No.</td></tr>
</table>

The day on which this schedule is for.
This has the same effect as setting fromDate and toDate to the same thing.

#### refinement <a name="propertyrefinement"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ELEMENT</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>No.</td></tr>
</table>

Provide a refinement to this schedule.

#### toDate <a name="propertytodate"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ATTRIBUTE</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
</table>

The to month and day.

#### toMonth <a name="propertytomonth"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ATTRIBUTE</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>No, defaults to 12 (December).</td></tr>
</table>

The to month.


### Examples
#### Example 1 <a name="example1"></a>

A schedule for two different month ranges. From April to September the
job will run daily at 10 am, and from October to March the job will run
daily at 11 am.

```xml
<schedules:list xmlns:schedules="http://rgordon.co.uk/oddjob/schedules">
    <schedules>
        <schedules:yearly fromMonth="4" toMonth="9">
            <refinement>
                <schedules:daily at="10:00"/>
            </refinement>
        </schedules:yearly>
        <schedules:yearly fromMonth="10" toMonth="3">
            <refinement>
                <schedules:daily at="11:00"/>
            </refinement>
        </schedules:yearly>
    </schedules>
</schedules:list>
```


Instead of month numbers, English month names can be used. This is
equivalent to the above.

```xml
<schedules:list xmlns:schedules="http://rgordon.co.uk/oddjob/schedules">
    <schedules>
        <schedules:yearly fromMonth="April" toMonth="September">
            <refinement>
                <schedules:daily at="10:00"/>
            </refinement>
        </schedules:yearly>
        <schedules:yearly fromMonth="October" toMonth="March">
            <refinement>
                <schedules:daily at="11:00"/>
            </refinement>
        </schedules:yearly>
    </schedules>
</schedules:list>
```


#### Example 2 <a name="example2"></a>

A from day of year to day of year example.

```xml
<schedules:yearly xmlns:schedules="http://rgordon.co.uk/oddjob/schedules"
             fromDate="12-25" toDate="12-26"/>
```


#### Example 3 <a name="example3"></a>

An on day of year example.

```xml
<schedules:yearly xmlns:schedules="http://rgordon.co.uk/oddjob/schedules"
             onDate="01-01"/>
```



-----------------------

<div style='font-size: smaller; text-align: center;'>(c) R Gordon Ltd 2005 - Present</div>
