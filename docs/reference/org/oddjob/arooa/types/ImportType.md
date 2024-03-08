[HOME](../../../../README.md)
# import

Import XML which is processed as if it's
in-line.

### Property Summary

| Property | Description |
| -------- | ----------- |
| [file](#propertyfile) | A file. | 
| [input](#propertyinput) | An input stream. | 
| [resource](#propertyresource) | A resource file on the classpath. | 
| [xml](#propertyxml) | XML as text. | 


### Example Summary

| Title | Description |
| ----- | ----------- |
| [Example 1](#example1) | Using import for a file list. |


### Property Detail
#### file <a name="propertyfile"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ATTRIBUTE</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>No.</td></tr>
</table>

A file.

#### input <a name="propertyinput"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ELEMENT</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>No.</td></tr>
</table>

An input stream.

#### resource <a name="propertyresource"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ATTRIBUTE</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>No.</td></tr>
</table>

A resource file on the classpath.

#### xml <a name="propertyxml"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ATTRIBUTE</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>No.</td></tr>
</table>

XML as text.


### Examples
#### Example 1 <a name="example1"></a>

Using import for a file list. the variables pathA and pathB are identical.

```xml
<oddjob>
    <job>
        <variables id="vars">
            <pathA>
                <import resource="org/oddjob/arooa/types/ImportExampleImport.xml"/>
            </pathA>
            <pathB>
                <files>
                    <list>
                        <file file="a.jar"/>
                        <file file="b.jar"/>
                        <file file="c.jar"/>
                    </list>
                </files>
            </pathB>
        </variables>
    </job>
</oddjob>
```


The imported file is:

```xml
<files>
    <list>
        <file file="a.jar"/>
        <file file="b.jar"/>
        <file file="c.jar"/>
    </list>
</files>
```



-----------------------

<div style='font-size: smaller; text-align: center;'>(c) R Gordon Ltd 2005 - Present</div>
