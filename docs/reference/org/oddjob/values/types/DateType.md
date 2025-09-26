[HOME](../../../../README.md)
# date

Define a Date.


Oddjob's inbuilt conversion allows a date to be specified as text in
any of these formats:

<dl>
<dt>yyyy-MM-dd</dt><dd>Just the date.</dd>
<dt>yyyy-MM-dd HH:mm</dt><dd>The date, hours and minutes.</dd>
<dt>yyyy-MM-dd HH:mm:ss</dt><dd>The date, hours, minutes and seconds.</dd>
<dt>yyyy-MM-dd HH:mm:ss.SSS</dt><dd>The date, hours, minutes, seconds
and milliseconds.</dd>
</dl>

Because of this a date property of a job can be specified perfectly
easily as a [value](../../../../org/oddjob/arooa/types/ValueType.md) or a property. However there are two situations
when this is inadequate:


- The text format of the date is not in one of the formats above.
- The date must be specified in a different time zone.


In either or both of these cases the date type can be used.



This date type can also be used to specify a java Calendar property which
Oddjob's inbuilt conversion will currently not do from text.


Since v1.3 The date can also be specified using one of these shortcuts:
<dl>
<dt>NOW</dt><dd>The date and time now.</dd>
<dt>TODAY</dt><dd>The date as of midnight.</dd>
<dt>YESTERDAY</dt><dd>The date yesterday at midnight.</dd>
<dt>TOMORROW</dt><dd>The date tomorrow at midnight.</dd>
</dl>

### Property Summary

| Property | Description |
| -------- | ----------- |
| [clock](#propertyclock) | The clock to use if a date shortcut is specified. | 
| [date](#propertydate) | A date in text, if a format is specified it is expected to be in the format provide, otherwise it is expected to be in the default format.. | 
| [format](#propertyformat) | The format the date is in. | 
| [timeZone](#propertytimezone) | The time zone the date is for. | 


### Example Summary

| Title | Description |
| ----- | ----------- |
| [Example 1](#example1) | A simple example of specifying a date. |
| [Example 2](#example2) | Specifying a date in a different format. |
| [Example 3](#example3) | Adjusting a date by Time Zone. |
| [Example 4](#example4) | Date shortcuts. |


### Property Detail
#### clock <a name="propertyclock"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ELEMENT</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>No, defaults to the current time clock.</td></tr>
</table>

The clock to use if a date shortcut is
specified. This is mainly here for tests.

#### date <a name="propertydate"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ATTRIBUTE</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>Yes.</td></tr>
</table>

A date in text, if a format is specified it is
expected to be in the format provide, otherwise it is expected
to be in the default format..

#### format <a name="propertyformat"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ATTRIBUTE</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>No.</td></tr>
</table>

The format the date is in.

#### timeZone <a name="propertytimezone"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ATTRIBUTE</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>No.</td></tr>
</table>

The time zone the date is for.


### Examples
#### Example 1 <a name="example1"></a>

A simple example of specifying a date.

```xml
<date date="2009-12-25 12:30"/>
```


#### Example 2 <a name="example2"></a>

Specifying a date in a different format.

```xml
<date date="25/12/2009 12:30" format="dd/MM/yyyy HH:mm"/>
```


#### Example 3 <a name="example3"></a>

Adjusting a date by Time Zone.

```xml
    <sequential>
      <jobs>
        <variables id="vars">
          <xmas>
            <date timeZone="US/Hawaii" date="2009-12-25" />
          </xmas>
        </variables>
        <echo>Christmas in Hawaii starts at ${vars.xmas}.</echo>
      </jobs>
    </sequential>
```


#### Example 4 <a name="example4"></a>

Date shortcuts.

```xml
<oddjob>
  <job>
    <sequential>
      <jobs>
        <variables id="vars">
          <now>
            <date date="NOW" />
          </now>
          <today>
            <date date="TODAY" />
          </today>
          <yesterday>
            <date date="YESTERDAY" />
          </yesterday>
          <tomorrow>
            <date date="TOMORROW" />
          </tomorrow>
        </variables>
        <echo>Today is ${vars.today}. 
The date/time now is ${vars.now}.
Yesterday was ${vars.yesterday}.
Tomorrow is ${vars.tomorrow}.</echo>
      </jobs>
    </sequential>
  </job>
</oddjob>
```



-----------------------

<div style='font-size: smaller; text-align: center;'>(c) R Gordon Ltd 2005 - Present</div>
