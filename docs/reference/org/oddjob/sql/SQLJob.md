[HOME](../../../README.md)
# sql

Runs one or more SQL statements.


<h3>Parsing</h3>
The SQL will be parsed and broken into individual statements
before being executed using JDBC. The statements are broken according
to the <code>delimiter</code> and <code>delimiterType</code> properties.
Setting the <code>expandProperties</code> property to true will cause
Oddjob to expand ${} expressions within the SQL. Comments are achieved
by starting a line with <code>--</code> or <code>//</code> or
<code>REM</code>. Note that <code>/* &#42;/</code> is not yet supported.

<h3>Result Processing</h3>
An optional result processor may be provided. [sql-results-sheet](../../../org/oddjob/sql/SQLResultsSheet.md)
allows the results to be displayed on a result sheet in a similar style
to an SQL query tool. [sql-results-bean](../../../org/oddjob/sql/SQLResultsBean.md) allows results to be
captured as beans who's properties can be used elsewhere in Oddjob.

<h3>Errors and Auto Commit</h3>
The <code>onError</code> property controls what to do if a statement fails.
By default it is ABORT. Auto commit is false by default so the changes
are rolled back. If auto commit is true the ABORT has the same affect as
STOP which commits statements already executed.

<h3>Parameterised Statements and Procedures</h3>
SQL statements can be parameterised, and can be stored procedure or
function calls. Out parameter values can also be accessed and used
elsewhere in Oddjob by wrapping them with an
[identify](../../../org/oddjob/arooa/types/IdentifiableValueType.md). See example 2 for an example of this.

<h3>Caveats</h3>
SQLServer stored procedures with parameters must be made using the JDBC
style call. E.g. { call sp_help(?) } otherwise an exception is thrown
from <code>getParameterMetaData</code>.

### Property Summary

| Property | Description |
| -------- | ----------- |
| [autocommit](#propertyautocommit) | Autocommit statements once executed. | 
| [callable](#propertycallable) | If the statement calls a stored procedure. | 
| [connection](#propertyconnection) | The connection to use. | 
| [delimiter](#propertydelimiter) | Set the delimiter that separates SQL statements. | 
| [delimiterType](#propertydelimiterType) | Set the delimiter type: NORMAL or ROW. | 
| [dialect](#propertydialect) | Allows a [org.oddjob.sql.DatabaseDialect](http://rgordon.co.uk/oddjob/1.6.0/api/org/oddjob/sql/DatabaseDialect.html) to be provided that can tune the way the result set is processed. | 
| [encoding](#propertyencoding) | Set the string encoding to use on the SQL read in. | 
| [escapeProcessing](#propertyescapeProcessing) | Set escape processing for statements. | 
| [executedSQLCount](#propertyexecutedSQLCount) | The number of SQL statements executed. | 
| [expandProperties](#propertyexpandProperties) | Enable property expansion inside the SQL statements read from the input. | 
| [input](#propertyinput) | The input from where to read the SQL query or DML statement(s) to run. | 
| [keepFormat](#propertykeepFormat) | Whether or not the format of the SQL should be preserved. | 
| [name](#propertyname) | A name, can be any text. | 
| [onError](#propertyonError) | What to do when a statement fails: <dl> <dt>CONTINUE</dt> <dd>Ignore the failure and continue executing.</dd> <dt>STOP</dt> <dd>Commit what has been executed but don't execute any more.</dd> <dt>ABORT</dt> <dd>Rollback what has been executed and don't execute any more.</dd> </dl> Note that if <code>autocommit</code> is true then ABORT behaves like STOP as no roll back is possible. | 
| [parameters](#propertyparameters) | Parameters to be bound to statement(s). | 
| [results](#propertyresults) | Optional result processor. | 
| [successfulSQLCount](#propertysuccessfulSQLCount) | The number of SQL statements successfully executed. | 


### Example Summary

| Title | Description |
| ----- | ----------- |
| [Example 1](#example1) | A simple example shows first the execution of multiple statements, then a simple parameterised query. |
| [Example 2](#example2) | An Callable Statement example. |


### Property Detail
#### autocommit <a name="propertyautocommit"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ATTRIBUTE</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>No, defaults to false.</td></tr>
</table>

Autocommit statements once executed.

#### callable <a name="propertycallable"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ATTRIBUTE</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>No, defaults to false.</td></tr>
</table>

If the statement calls a stored procedure.

#### connection <a name="propertyconnection"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ELEMENT</td></tr>
      <tr><td><i>Access</i></td><td>WRITE_ONLY</td></tr>
      <tr><td><i>Required</i></td><td>Yes.</td></tr>
</table>

The connection to use. This can be provided
by a [connection](../../../org/oddjob/sql/ConnectionType.md) or by some other means such as custom
data source. This SQL job will always close the connection once
it has run.

#### delimiter <a name="propertydelimiter"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ATTRIBUTE</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>No. Defaults to ;</td></tr>
</table>

Set the delimiter that separates SQL statements.
Defaults to a semicolon.


For scripts that use a separate line delimiter like "GO"
also set the <code>delimiterType</code> to "ROW".


The delimiter is case insensitive so either "GO" or "go" can be
used interchangeably.

#### delimiterType <a name="propertydelimiterType"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ATTRIBUTE</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>No, defaults to NORMAL.</td></tr>
</table>

Set the delimiter type: NORMAL or ROW.


NORMAL means that any occurrence of the delimiter terminates the SQL
command whereas with ROW, only a line containing just the
delimiter is recognised as the end of the command.


ROW is used with delimiters such as GO.

#### dialect <a name="propertydialect"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ELEMENT</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>No. A default is used.</td></tr>
</table>

Allows a [org.oddjob.sql.DatabaseDialect](http://rgordon.co.uk/oddjob/1.6.0/api/org/oddjob/sql/DatabaseDialect.html) to be provided
that can tune the way the result set is processed.

#### encoding <a name="propertyencoding"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ATTRIBUTE</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>No.</td></tr>
</table>

Set the string encoding to use on the SQL read in.

#### escapeProcessing <a name="propertyescapeProcessing"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ATTRIBUTE</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>No, defaults to false.</td></tr>
</table>

Set escape processing for statements. See the java doc for
<code>Statement.setEscapeProcessing</code> for more information.

#### executedSQLCount <a name="propertyexecutedSQLCount"></a>

<table style='font-size:smaller'>
      <tr><td><i>Access</i></td><td>READ_ONLY</td></tr>
</table>

The number of SQL statements executed.

#### expandProperties <a name="propertyexpandProperties"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ATTRIBUTE</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>No, defaults to false.</td></tr>
</table>

Enable property expansion inside the SQL statements
read from the input.

#### input <a name="propertyinput"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ELEMENT</td></tr>
      <tr><td><i>Access</i></td><td>WRITE_ONLY</td></tr>
      <tr><td><i>Required</i></td><td>Yes.</td></tr>
</table>

The input from where to read the SQL query
or DML statement(s) to run. Probably either [file](../../../org/oddjob/io/FileType.md) for
reading the SQL from a file or [buffer](../../../org/oddjob/io/BufferType.md) for configuring
the SQL in line.

#### keepFormat <a name="propertykeepFormat"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ATTRIBUTE</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>No. Defaults to false.</td></tr>
</table>

Whether or not the format of the
SQL should be preserved.

#### name <a name="propertyname"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ATTRIBUTE</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>No.</td></tr>
</table>

A name, can be any text.

#### onError <a name="propertyonError"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ATTRIBUTE</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>No, defaults to ABORT.</td></tr>
</table>

What to do when a statement fails:
<dl>
<dt>CONTINUE</dt>
<dd>Ignore the failure and continue executing.</dd>
<dt>STOP</dt>
<dd>Commit what has been executed but don't execute any more.</dd>
<dt>ABORT</dt>
<dd>Rollback what has been executed and don't execute any more.</dd>
</dl>
Note that if <code>autocommit</code> is true then ABORT behaves
like STOP as no roll back is possible.

#### parameters <a name="propertyparameters"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ELEMENT</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>No.</td></tr>
</table>

Parameters to be bound to statement(s). This
is either a [value](../../../org/oddjob/arooa/types/ValueType.md) or an [identify](../../../org/oddjob/arooa/types/IdentifiableValueType.md)
if the parameter is an out parameter that is to be identifiable by
an id for other jobs to access.

#### results <a name="propertyresults"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ELEMENT</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>No, defaults to none.</td></tr>
</table>

Optional result processor. Probably one of
[sql-results-bean](../../../org/oddjob/sql/SQLResultsBean.md) or [sql-results-sheet](../../../org/oddjob/sql/SQLResultsSheet.md).

#### successfulSQLCount <a name="propertysuccessfulSQLCount"></a>

<table style='font-size:smaller'>
      <tr><td><i>Access</i></td><td>READ_ONLY</td></tr>
</table>

The number of SQL statements successfully executed.


### Examples
#### Example 1 <a name="example1"></a>

A simple example shows first the execution of multiple statements,
then a simple parameterised query.

```xml
<oddjob>
  <job>
    <sequential>
      <jobs>
        <variables id="vars">
          <connection>
            <connection driver="org.hsqldb.jdbcDriver" url="jdbc:hsqldb:mem:test"
              username="sa" />
          </connection>
        </variables>
        <sql name="Create table">
          <connection>
            <value value="${vars.connection}" />
          </connection>
          <input>
            <buffer>
	        	            <![CDATA[
create table GREETINGS(STYLE varchar(20),
       TEXT varchar(20))
       
insert into GREETINGS values('nice', 'Hello')

insert into GREETINGS values('grumpy', 'Bah Humbug')
]]>
            </buffer>
          </input>
        </sql>
        <sql id="query">
          <connection>
            <value value="${vars.connection}" />
          </connection>
          <input>
            <buffer>
	        	            <![CDATA[
select TEXT from GREETINGS where STYLE = ?
]]>
            </buffer>
          </input>
          <parameters>
            <value value='nice' />
          </parameters>
          <results>
            <sql-results-bean />
          </results>
        </sql>
        <echo name="Single Row Result">${query.results.row.TEXT}</echo>
        <echo name="Result By Row Index">${query.results.rows[0].TEXT}</echo>
      </jobs>
    </sequential>
  </job>
</oddjob>
```


The results are made available to the echo jobs using a
[sql-results-bean](../../../org/oddjob/sql/SQLResultsBean.md).

#### Example 2 <a name="example2"></a>

An Callable Statement example. Showing support for IN, INOUT, and OUT
parameters. Note that declaring the stored procedure requires a change
in delimiter otherwise the semicolon is interrupted as an end of
statement.

```xml
<oddjob>
  <job>
    <sequential>
      <jobs>
        <variables id='vars'>
          <connection>
            <connection driver="org.hsqldb.jdbcDriver" url="jdbc:hsqldb:mem:test"
              username="sa" />
          </connection>
        </variables>
        <sql callable='true' delimiterType='ROW'>
          <connection>
            <value value="${vars.connection}" />
          </connection>
          <input>
            <buffer>
              create procedure TEST (out a int, inout b int, in c int)
              MODIFIES SQL DATA
              begin atomic
              set a = b;
              set b = c;
              end
            </buffer>
          </input>
        </sql>
        <sql id='sql-call' callable='true'>
          <connection>
            <value value='${vars.connection}' />
          </connection>
          <parameters>
            <identify id='a'>
              <value>
                <value />
              </value>
            </identify>
            <identify id='b'>
              <value>
                <value value='2' />
              </value>
            </identify>
            <value value='3' />
          </parameters>
          <input>
            <buffer>
              call TEST (?, ?, ?)
            </buffer>
          </input>
        </sql>
        <echo>a=${a}, b=${b}.</echo>
      </jobs>
    </sequential>
  </job>
</oddjob>
```



-----------------------

<div style='font-size: smaller; text-align: center;'>(c) R Gordon Ltd 2005 - Present</div>
