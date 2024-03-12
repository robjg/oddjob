[HOME](../../../README.md)
# delete

Delete a file or directory, or files
and directories.


Unless the force property is set, this job will cause an
exception if an attempt is made to delete a non empty directory.

### Property Summary

| Property | Description |
| -------- | ----------- |
| [dirCount](#propertydirCount) | Count of the directories deleted. | 
| [errorCount](#propertyerrorCount) | Count of the errors. | 
| [fileCount](#propertyfileCount) | Count of the files deleted. | 
| [files](#propertyfiles) | The file, directory, or files and directories to delete. | 
| [force](#propertyforce) | Forceably delete non empty directories. | 
| [logEvery](#propertylogEvery) | Logs the number of files and directories deleted every n number of items. | 
| [maxErrors](#propertymaxErrors) | The maximum number of errors to allow before failing. | 
| [name](#propertyname) | A name, can be any text. | 
| [reallyRoot](#propertyreallyRoot) | Flag to indicate that it is the intention to delete files at the root level. | 


### Example Summary

| Title | Description |
| ----- | ----------- |
| [Example 1](#example1) | Delete all files from a directory. |


### Property Detail
#### dirCount <a name="propertydirCount"></a>

<table style='font-size:smaller'>
      <tr><td><i>Access</i></td><td>READ_ONLY</td></tr>
</table>

Count of the directories deleted.

#### errorCount <a name="propertyerrorCount"></a>

<table style='font-size:smaller'>
      <tr><td><i>Access</i></td><td>READ_ONLY</td></tr>
</table>

Count of the errors.

#### fileCount <a name="propertyfileCount"></a>

<table style='font-size:smaller'>
      <tr><td><i>Access</i></td><td>READ_ONLY</td></tr>
</table>

Count of the files deleted.

#### files <a name="propertyfiles"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ELEMENT</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>Yes.</td></tr>
</table>

The file, directory, or files and directories
to delete. Note the files must be valid file name, they can not
contain wildcard characters. This will be the case by default if
the [files](../../../org/oddjob/io/FilesType.md) is used to specify the files.

#### force <a name="propertyforce"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ATTRIBUTE</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>No, defaults to false.</td></tr>
</table>

Forceably delete non empty directories.

#### logEvery <a name="propertylogEvery"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ATTRIBUTE</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>No, defaults to 0.</td></tr>
</table>

Logs the number of files and directories deleted
every n number of items. If this property is 1 then the file or
directory path is logged every delete. If this property is less than
one then the counts are logged only at the end.

#### maxErrors <a name="propertymaxErrors"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ATTRIBUTE</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>No, defaults to 0.</td></tr>
</table>

The maximum number of errors to allow before
failing. Sometimes when deleting a large number of files, it is not
desirable to have one or two locked files from stopping all the other
files from being deleted.

#### name <a name="propertyname"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ATTRIBUTE</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>No.</td></tr>
</table>

A name, can be any text.

#### reallyRoot <a name="propertyreallyRoot"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ATTRIBUTE</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>No, defaults to false.</td></tr>
</table>

Flag to indicate that it is the intention to
delete files at the root level. This is to catch the situation
where variable substitution is used to specify the file path but
the variable doesn't exists - e.g. The file specification is
<code>${some.dir}/*</code> but <code>some.dir</code> has not been
defined.


### Examples
#### Example 1 <a name="example1"></a>

Delete all files from a directory. The directory is the first of
Oddjob's arguments.

```xml
<oddjob id="this">
    <job>
        <delete id="delete">
            <files>
                <files files="${this.args[0]}/*"/>
            </files>
        </delete>
    </job>
</oddjob>
```



-----------------------

<div style='font-size: smaller; text-align: center;'>(c) R Gordon Ltd 2005 - Present</div>
