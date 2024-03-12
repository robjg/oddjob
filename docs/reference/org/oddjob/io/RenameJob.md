[HOME](../../../README.md)
# rename

Rename a file or directory.


This is a simple wrapper for Java's File.rename method and so is very
limited.

### Property Summary

| Property | Description |
| -------- | ----------- |
| [from](#propertyfrom) | The from file. | 
| [name](#propertyname) | A name, can be any text. | 
| [to](#propertyto) | The to file. | 


### Example Summary

| Title | Description |
| ----- | ----------- |
| [Example 1](#example1) | Rename a file and rename it back. |


### Property Detail
#### from <a name="propertyfrom"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ATTRIBUTE</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>Yes.</td></tr>
</table>

The from file.

#### name <a name="propertyname"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ATTRIBUTE</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>No.</td></tr>
</table>

A name, can be any text.

#### to <a name="propertyto"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ATTRIBUTE</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>Yes.</td></tr>
</table>

The to file.


### Examples
#### Example 1 <a name="example1"></a>

Rename a file and rename it back.

```xml
<oddjob>
    <job>
        <folder>
            <jobs>
                <rename id="from"
                        name="Rename"
                        from="${work.dir}/a.txt"
                        to="${work.dir}/b.txt"/>
                <rename id="back"
                        name="Rename Back"
                        from="${work.dir}/b.txt"
                        to="${work.dir}/a.txt"/>
            </jobs>
        </folder>
    </job>
</oddjob>
```



-----------------------

<div style='font-size: smaller; text-align: center;'>(c) R Gordon Ltd 2005 - Present</div>
