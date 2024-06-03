[HOME](../../../../README.md)
# format

A type which can either format a
number or a date into the given text format.


Form more information on the number format see [java.text.DecimalFormat](https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/text/DecimalFormat.html)


For more information on the date format see [java.text.SimpleDateFormat](https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/text/SimpleDateFormat.html)

### Property Summary

| Property | Description |
| -------- | ----------- |
| [date](#propertydate) | A date to format. | 
| [format](#propertyformat) | The format. | 
| [number](#propertynumber) | A number to format. | 
| [timeZone](#propertytimeZone) | The time zone to use for a date format. | 


### Example Summary

| Title | Description |
| ----- | ----------- |
| [Example 1](#example1) | Formatting a date and number to create a file name. |
| [Example 2](#example2) | Format the current time as a property. |


### Property Detail
#### date <a name="propertydate"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ATTRIBUTE</td></tr>
      <tr><td><i>Access</i></td><td>WRITE_ONLY</td></tr>
      <tr><td><i>Required</i></td><td>Yes if number isn't supplied.</td></tr>
</table>

A date to format.

#### format <a name="propertyformat"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ATTRIBUTE</td></tr>
      <tr><td><i>Access</i></td><td>WRITE_ONLY</td></tr>
      <tr><td><i>Required</i></td><td>Yes.</td></tr>
</table>

The format.

#### number <a name="propertynumber"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ATTRIBUTE</td></tr>
      <tr><td><i>Access</i></td><td>WRITE_ONLY</td></tr>
      <tr><td><i>Required</i></td><td>Yes if date isn't supplied.</td></tr>
</table>

A number to format.

#### timeZone <a name="propertytimeZone"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ATTRIBUTE</td></tr>
      <tr><td><i>Access</i></td><td>WRITE_ONLY</td></tr>
      <tr><td><i>Required</i></td><td>No.</td></tr>
</table>

The time zone to use for a date format.


### Examples
#### Example 1 <a name="example1"></a>

Formatting a date and number to create a file name.

```xml
<oddjob>
    <job>
        <sequential>
            <jobs>
                <variables id='vars'>
                    <businessDate>
                        <format format='yyyyMMdd' date='2005-12-25'/>
                    </businessDate>
                    <sequence>
                        <format format='000000' number='123'/>
                    </sequence>
                </variables>
                <exists id="file-check"
                    file="Data-${vars.businessDate}-${vars.sequence}.dat"/>
            </jobs>
        </sequential>
    </job>
</oddjob>
```


#### Example 2 <a name="example2"></a>

Format the current time as a property.

```xml
<?xml version="1.0" encoding="UTF-8" standalone="no"?>
<oddjob>
    <job>
        <sequential>
            <jobs>
                <properties>
                    <values>
                        <date date="NOW" key="date-time.now">
                            <clock>
                                <value value="${our-clock}"/>
                            </clock>
                        </date>
                        <format date="${date-time.now}" format="h:mm a" key="time.now.formatted"/>
                    </values>
                </properties>
                <echo><![CDATA[The time is ${time.now.formatted}.]]></echo>
            </jobs>
        </sequential>
    </job>
</oddjob>
```


Note that the <code>our-clock</code> reference is provided for the tests
but when not provided (if this is run as is) the current time is used.


An example of the output is:

```
The time is 8:17 AM.
```



-----------------------

<div style='font-size: smaller; text-align: center;'>(c) R Gordon Ltd 2005 - Present</div>
