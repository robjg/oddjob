[HOME](../../../README.md)
# sql-persister-service

Persists job state to a database. The database must
have a table which can be created with the following sql.
<pre><code>
CREATE TABLE oddjob(
path VARCHAR(128),
id VARCHAR(32),
job BLOB,
CONSTRAINT oddjob_pk PRIMARY KEY (path, id))
</pre></code>

### Property Summary

| Property | Description |
| -------- | ----------- |
| [connection](#propertyconnection) | The [connection](../../../org/oddjob/sql/ConnectionType.md)to use. | 
| [name](#propertyname) | The name. | 
| [persister](#propertypersister) | The persister. | 
| [serializationFactory](#propertyserializationFactory) | A plugin for providers of the serialization. | 


### Example Summary

| Title | Description |
| ----- | ----------- |
| [Example 1](#example1) | Using a SQL Persister. |


### Property Detail
#### connection <a name="propertyconnection"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ELEMENT</td></tr>
      <tr><td><i>Access</i></td><td>WRITE_ONLY</td></tr>
      <tr><td><i>Required</i></td><td>Yes.</td></tr>
</table>

The [connection](../../../org/oddjob/sql/ConnectionType.md) to use.

#### name <a name="propertyname"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ATTRIBUTE</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>No.</td></tr>
</table>

The name.

#### persister <a name="propertypersister"></a>

<table style='font-size:smaller'>
      <tr><td><i>Access</i></td><td>READ_ONLY</td></tr>
      <tr><td><i>Required</i></td><td>R/O.</td></tr>
</table>

The persister.

#### serializationFactory <a name="propertyserializationFactory"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ELEMENT</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>No.</td></tr>
</table>

A plugin for providers of the serialization.
The default is for HSQL.


### Examples
#### Example 1 <a name="example1"></a>

Using a SQL Persister.

```xml
<oddjob id="this">
    <job>
        <sequential>
            <jobs>
                <sql-persister-service id="sql-persister">
                    <connection>
                        <connection driver="org.hsqldb.jdbcDriver" url="jdbc:hsqldb:mem:test" username="sa"/>
                    </connection>
                </sql-persister-service>
                <oddjob id="oj" file="${this.dir}/SQLPersisterInner.xml">
                    <persister>
                        <value value="${sql-persister.persister(test)}"/>
                    </persister>
                </oddjob>
                <stop job="${sql-persister}"/>
            </jobs>
        </sequential>
    </job>
</oddjob>

```


Note that because this is a service, it must be stopped once the inner Oddjob
has completed it's work. In an Oddjob that was running continually this would
not be necessary.


-----------------------

<div style='font-size: smaller; text-align: center;'>(c) R Gordon Ltd 2005 - Present</div>
