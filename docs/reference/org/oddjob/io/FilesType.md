[HOME](../../../README.md)
# files

Specify files using a wild card pattern, or a
a list. The list can contain [file](../../../org/oddjob/io/FileType.md) or other types that
can be converted into a java File object or array including this type.
In this way this type can be used to build complicated collections of
files.

### Property Summary

| Property | Description |
| -------- | ----------- |
| [files](#propertyfiles) | The files | 
| [list](#propertylist) | More files | 


### Example Summary

| Title | Description |
| ----- | ----------- |
| [Example 1](#example1) | A single file. |
| [Example 2](#example2) | Using a wildcard expression. |
| [Example 3](#example3) | Specifying a list of files. |
| [Example 4](#example4) | A complex version of building up a file list. |


### Property Detail
#### files <a name="propertyfiles"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ATTRIBUTE</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>No</td></tr>
</table>

The files

#### list <a name="propertylist"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ELEMENT</td></tr>
      <tr><td><i>Access</i></td><td>WRITE_ONLY</td></tr>
      <tr><td><i>Required</i></td><td>No</td></tr>
</table>

More files


### Examples
#### Example 1 <a name="example1"></a>

A single file.

```xml
<files files="onefile.txt"/>
```


#### Example 2 <a name="example2"></a>

Using a wildcard expression.

```xml
<files files="reports/*.txt"/>
```


#### Example 3 <a name="example3"></a>

Specifying a list of files.

```xml
<files>
    <list>
        <files files="onefile.txt"/>
        <files files="reports/*.txt"/>
    </list>
</files>
```


#### Example 4 <a name="example4"></a>

A complex version of building up a file list. It includes taking
advantage of Oddjob's built in path conversion and adds in files
specified as arguments passed in to Oddjob.

```xml
<?xml version="1.0" encoding="UTF-8" standalone="no"?>
<oddjob id="this">
    <job>
        <sequential>
            <jobs>
                <variables id="vars">
                    <aList>
                        <files>
                            <list>
                                <file file="a.jar"/>
                                <value value="b.jar${path.separator}c.jar"/>
                                <value value="${this.args}"/>
                            </list>
                        </files>
                    </aList>
                </variables>
                <foreach>
                    <values>
                        <value value="${vars.aList}"/>
                    </values>
                    <configuration>
                        <xml>
                            <foreach id="loop">
                                <job>
                                    <echo>${loop.current}</echo>
                                </job>
                            </foreach>
                        </xml>
                    </configuration>
                </foreach>
            </jobs>
        </sequential>
    </job>
</oddjob>
```



-----------------------

<div style='font-size: smaller; text-align: center;'>(c) R Gordon Ltd 2005 - Present</div>
