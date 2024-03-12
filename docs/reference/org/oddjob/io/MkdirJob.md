[HOME](../../../README.md)
# mkdir

Make a directory, including any necessary but
nonexistent parent directories. If there already exists a
file with specified name or the directory cannot be created then
an exception is flagged. If the directory exists alread it is left
intact.

### Property Summary

| Property | Description |
| -------- | ----------- |
| [dir](#propertydir) | The directory to create. | 
| [name](#propertyname) | A name, can be any text. | 


### Example Summary

| Title | Description |
| ----- | ----------- |
| [Example 1](#example1) | Make a directory including missing parent directories. |


### Property Detail
#### dir <a name="propertydir"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ATTRIBUTE</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>Yes.</td></tr>
</table>

The directory to create.

#### name <a name="propertyname"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ATTRIBUTE</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>No.</td></tr>
</table>

A name, can be any text.


### Examples
#### Example 1 <a name="example1"></a>

Make a directory including missing parent directories.

```xml
<oddjob id="this">
    <job>
        <mkdir dir="${this.args[0]}/a/b/c"/>
    </job>
</oddjob>
```



-----------------------

<div style='font-size: smaller; text-align: center;'>(c) R Gordon Ltd 2005 - Present</div>
