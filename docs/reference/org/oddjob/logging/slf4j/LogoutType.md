[HOME](../../../../README.md)
# logout

Provide an output to a logger. With a default
Oddjob configuration log messages will be visible in the Log panel
of Oddjob Explorer.

### Property Summary

| Property | Description |
| -------- | ----------- |
| [level](#propertylevel) | The log log level. | 
| [logger](#propertylogger) | The Log4j logger name to log the output to. | 


### Example Summary

| Title | Description |
| ----- | ----------- |
| [Example 1](#example1) | Copy the contents of a file to the logger. |


### Property Detail
#### level <a name="propertylevel"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ATTRIBUTE</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>No. Defaults to INFO.</td></tr>
</table>

The log log level.

#### logger <a name="propertylogger"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ATTRIBUTE</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>No. Defaults to org.oddjob.logging.log4j.LogoutType</td></tr>
</table>

The Log4j logger name to log the output to.


### Examples
#### Example 1 <a name="example1"></a>

Copy the contents of a file to the logger.

```xml
<oddjob id="this">
    <job>
        <copy>
            <input>
                <file file="${this.args[0]}/test/io/TestFile.txt"/>
            </input>
            <output>
                <logout/>
            </output>
        </copy>
    </job>
</oddjob>
```



-----------------------

<div style='font-size: smaller; text-align: center;'>(c) R Gordon Ltd 2005 - Present</div>
