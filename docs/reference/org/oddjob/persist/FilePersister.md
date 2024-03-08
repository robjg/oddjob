[HOME](../../../README.md)
# file-persister

Persist and load jobs from and to a file. The file
the job is persisted to is the jobs id with a .ser extension.


A new sub directory is created for each nested Oddjob with an id. The
job of the nested Oddjob are persisted to the sub directory. Thus the
directory structure mirrors the structure of the Oddjobs.

### Property Summary

| Property | Description |
| -------- | ----------- |
| [dir](#propertydir) | The directory in which the files will be created. | 
| [path](#propertypath) | A '/' delimited path to the location for the persister. | 


### Example Summary

| Title | Description |
| ----- | ----------- |
| [Example 1](#example1) | Using a file persister with Oddjob. |


### Property Detail
#### dir <a name="propertydir"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ATTRIBUTE</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>No.</td></tr>
</table>

The directory in which the files will be created.

#### path <a name="propertypath"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ATTRIBUTE</td></tr>
      <tr><td><i>Access</i></td><td>WRITE_ONLY</td></tr>
      <tr><td><i>Required</i></td><td>No.</td></tr>
</table>

A '/' delimited path to the location for the
persister. Normally this is set by nested persisters to be the id
of the Oddjob that created them.


### Examples
#### Example 1 <a name="example1"></a>

Using a file persister with Oddjob. The persist directory is passed
in as an argument from the command line. The state of child jobs will
be saved in a child directory relative to the given directory of the name
'important-jobs'.

```xml
<oddjob id="this">
    <job>
        <oddjob id="important-jobs" file="${this.dir}/FilePersisterExampleInner.xml">
            <persister>
                <file-persister dir="${this.args[0]}"/>
            </persister>
        </oddjob>
    </job>
</oddjob>
```



-----------------------

<div style='font-size: smaller; text-align: center;'>(c) R Gordon Ltd 2005 - Present</div>
