[HOME](../../../README.md)
# append

Specify a file for appending to.



### Property Summary

| Property | Description |
| -------- | ----------- |
| [file](#propertyfile) | The file path. | 


### Example Summary

| Title | Description |
| ----- | ----------- |
| [Example 1](#example1) | Append a line to a file. |


### Property Detail
#### file <a name="propertyfile"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ATTRIBUTE</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>Yes.</td></tr>
</table>

The file path.


### Examples
#### Example 1 <a name="example1"></a>

Append a line to a file.

```xml
<?xml version="1.0" encoding="UTF-8" standalone="no"?>
<oddjob id="this">
    <job>
        <sequential>
            <jobs>
                <variables id="vars">
                    <myFile>
                        <append file="${this.args[0]}/messages.txt"/>
                    </myFile>
                </variables>
                <echo>
                    <output>
                        <value value="${vars.myFile}"/>
                    </output><![CDATA[Hello World]]></echo>
                <echo>
                    <output>
                        <value value="${vars.myFile}"/>
                    </output><![CDATA[Goodbye World]]></echo>
            </jobs>
        </sequential>
    </job>
</oddjob>

```



-----------------------

<div style='font-size: smaller; text-align: center;'>(c) R Gordon Ltd 2005 - Present</div>
