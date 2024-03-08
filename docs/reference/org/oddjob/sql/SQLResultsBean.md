[HOME](../../../README.md)
# sql-results-bean

Captures SQL results in a bean that
has properties to provide those results to other jobs.


The properties `row`, `rows`, `rowSets` properties
in turn expose the results as beans so the colums can be accessed as
properties.


If a single query result set consisted of a single row:

<code><pre>
NAME        AGE
John        47
</pre></code>

then:

<code><pre>
row.NAME == rows[0].NAME == rowSets[0][0].NAME == 'John'
row.AGE == rows[0].AGE == rowSets[0][0].AGE == 47
</pre></code>

If a single query result set consisted of more than a single row:

<code><pre>
NAME        AGE
John        47
Jane        72
</pre></code>

then the `row` property is unavailable and any attempt to access
it would result in an exception, and:

<code><pre>
rows[1].NAME == rowSets[0][1].NAME == 'Jane'
rows[1].AGE == rowSets[0][1].AGE == '72'
</pre></code>

If the query results in a multiple query result set:

<code><pre>
NAME        AGE
John        47
Jane        72
</pre></code>

<code><pre>
FRUIT       COLOUR
Apple       Green
</pre></code>

then the `row` property and the `rows` properties are
unavailable and any attempt to access either would result in an exception.
The rowSets property can be used as follows:

<code><pre>
rowSets[0][1].NAME == 'Jane'
rowSets[0][1].AGE == '72'
rowSets[1][0].FRUIT == 'Apple'
rowSets[1][0].COLOUR == 'Green'
</pre></code>

The case of the properties depends on the database used.



Any attempt to access a row or row set that doesn't exist will result
in an exception.



### Property Summary

| Property | Description |
| -------- | ----------- |
| [name](#propertyname) | The name of this component. | 
| [row](#propertyrow) | The result of a query when only one result is expected. | 
| [rowCount](#propertyrowCount) | The total number of rows returned by all the queries. | 
| [rowSetCount](#propertyrowSetCount) | The number of rows sets, which will be the same as the number of queries that returned results. | 
| [rowSets](#propertyrowSets) | A two dimensional array of all of the rows that each individual query returned. | 
| [rows](#propertyrows) | An array of the rows when the query set contains only one result returning query. | 
| [to](#propertyto) | The next component in a bus. | 
| [updateCount](#propertyupdateCount) | The total update count for all queries. | 
| [updateCounts](#propertyupdateCounts) | An Array of the update counts, one element per data modification statement. | 


### Example Summary

| Title | Description |
| ----- | ----------- |
| [Example 1](#example1) | See [sql](../../../org/oddjob/sql/SQLJob.md)for an example. |


### Property Detail
#### name <a name="propertyname"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ATTRIBUTE</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>No.</td></tr>
</table>

The name of this component.

#### row <a name="propertyrow"></a>

<table style='font-size:smaller'>
      <tr><td><i>Access</i></td><td>READ_ONLY</td></tr>
      <tr><td><i>Required</i></td><td>Read only.</td></tr>
</table>

The result of a query when only one result is expected.
If no results were returned by the queries this property is null. If
there are more than one row an exception will occur.

#### rowCount <a name="propertyrowCount"></a>

<table style='font-size:smaller'>
      <tr><td><i>Access</i></td><td>READ_ONLY</td></tr>
      <tr><td><i>Required</i></td><td>Read only.</td></tr>
</table>

The total number of rows returned by all the
queries.

#### rowSetCount <a name="propertyrowSetCount"></a>

<table style='font-size:smaller'>
      <tr><td><i>Access</i></td><td>READ_ONLY</td></tr>
      <tr><td><i>Required</i></td><td>Read only.</td></tr>
</table>

The number of rows sets, which will be the same
as the number of queries that returned results.

#### rowSets <a name="propertyrowSets"></a>

<table style='font-size:smaller'>
      <tr><td><i>Access</i></td><td>READ_ONLY</td></tr>
      <tr><td><i>Required</i></td><td>Read only.</td></tr>
</table>

A two dimensional array of all of the rows
that each individual query returned.

#### rows <a name="propertyrows"></a>

<table style='font-size:smaller'>
      <tr><td><i>Access</i></td><td>READ_ONLY</td></tr>
      <tr><td><i>Required</i></td><td>Read only.</td></tr>
</table>

An array of the rows when the query set contains only
one result returning query.
If no results were returned by the queries this property is null. If
there are more than one result sets an exception will occur.

#### to <a name="propertyto"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ELEMENT</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>No.</td></tr>
</table>

The next component in a bus. Set automatically in a
[bus:bus](../../../org/oddjob/beanbus/bus/BasicBusService.md).

#### updateCount <a name="propertyupdateCount"></a>

<table style='font-size:smaller'>
      <tr><td><i>Access</i></td><td>READ_ONLY</td></tr>
      <tr><td><i>Required</i></td><td>Read only.</td></tr>
</table>

The total update count for all queries.

#### updateCounts <a name="propertyupdateCounts"></a>

<table style='font-size:smaller'>
      <tr><td><i>Access</i></td><td>READ_ONLY</td></tr>
      <tr><td><i>Required</i></td><td>Read only.</td></tr>
</table>

An Array of the update counts, one element per
data modification statement.


### Examples
#### Example 1 <a name="example1"></a>

See [sql](../../../org/oddjob/sql/SQLJob.md) for an example.


-----------------------

<div style='font-size: smaller; text-align: center;'>(c) R Gordon Ltd 2005 - Present</div>
