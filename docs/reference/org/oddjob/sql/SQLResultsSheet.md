[HOME](../../../README.md)
# sql-results-sheet

Writes SQL results to an output stream.

### Property Summary

| Property | Description |
| -------- | ----------- |
| [dataOnly](#propertydataonly) | Don't display headings. | 
| [name](#propertyname) | The name of this component. | 
| [output](#propertyoutput) | The output stream to write results to. | 
| [to](#propertyto) | The next component in a bus. | 


### Example Summary

| Title | Description |
| ----- | ----------- |
| [Example 1](#example1) | A result sheet for multiple statements. |


### Property Detail
#### dataOnly <a name="propertydataonly"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ATTRIBUTE</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>No. Defaults to showing headings.</td></tr>
</table>

Don't display headings.

#### name <a name="propertyname"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ATTRIBUTE</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>No.</td></tr>
</table>

The name of this component.

#### output <a name="propertyoutput"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ELEMENT</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>No. Defaults to stdout.</td></tr>
</table>

The output stream to write results to.

#### to <a name="propertyto"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ELEMENT</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>No.</td></tr>
</table>

The next component in a bus. Set automatically in a
[bus:bus](../../../org/oddjob/beanbus/bus/BasicBusService.md).


### Examples
#### Example 1 <a name="example1"></a>

A result sheet for multiple statements.


```xml
<oddjob xmlns:s="http://rgordon.co.uk/oddjob/schedules">
  <job>
    <sql>
      <connection>
        <connection driver="org.hsqldb.jdbcDriver" url="jdbc:hsqldb:mem:SQLResultsSheet_Example"
          username="sa" password="" />
      </connection>
      <input>
        <buffer>
create table FRUIT(
  TYPE varchar(16),
  VARIETY varchar(16),
  COLOUR varchar(32),
  SIZE double);
insert into FRUIT values ('Apple', 'Cox', 'Red and Green', 7.6);
insert into FRUIT values ('Orange', 'Jaffa', 'Orange', 9.245);

select * from FRUIT;

shutdown;
        </buffer>
      </input>
      <results>
        <sql-results-sheet />
      </results>
    </sql>
  </job>
</oddjob>
```



This writes the following to the console:

<code><pre>
[0 rows affected, 16 ms.]

[1 rows affected, 0 ms.]

[1 rows affected, 0 ms.]

TYPE    VARIETY  COLOUR         SIZE
------  -------  -------------  -----
Apple   Cox      Red and Green  7.6
Orange  Jaffa    Orange         9.245

[2 rows, 212 ms.]

[0 rows affected, 0 ms.]

</pre></code>


-----------------------

<div style='font-size: smaller; text-align: center;'>(c) R Gordon Ltd 2005 - Present</div>
