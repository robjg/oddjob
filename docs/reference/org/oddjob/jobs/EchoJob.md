[HOME](../../../README.md)
# echo

Echo text to the console.

### Property Summary

| Property | Description |
| -------- | ----------- |
| [lines](#propertylines) | Lines of text to display. | 
| [name](#propertyname) | A name, can be any text. | 
| [output](#propertyoutput) | Where to send the output. | 
| [text](#propertytext) | The text to display. | 


### Example Summary

| Title | Description |
| ----- | ----------- |
| [Example 1](#example1) | Hello World. |
| [Example 2](#example2) | Hello World Twice. |
| [Example 3](#example3) | Echo a file list. |


### Property Detail
#### lines <a name="propertylines"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ELEMENT</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>No, if there is no text and no lines
 only a blank line will be printed.
 printed.</td></tr>
</table>

Lines of text to display.

#### name <a name="propertyname"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ATTRIBUTE</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>No.</td></tr>
</table>

A name, can be any text.

#### output <a name="propertyoutput"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ELEMENT</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>No, defaults to the console.</td></tr>
</table>

Where to send the output.

#### text <a name="propertytext"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>TEXT</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>No, if there is no text and no lines
 only a blank line will be printed.</td></tr>
</table>

The text to display.


### Examples
#### Example 1 <a name="example1"></a>

Hello World.


```xml
<oddjob>
    <job>
        <echo name="Greeting Job">Hello World</echo>
    </job>
</oddjob>
```


#### Example 2 <a name="example2"></a>

Hello World Twice.


```xml
<oddjob>
    <job>
        <sequential>
            <jobs>
                <echo id="first">Hello World Twice!</echo>
                <echo>${first.text}</echo>
            </jobs>
        </sequential>
    </job>
</oddjob>
```


#### Example 3 <a name="example3"></a>

Echo a file list.


```xml
<oddjob id="this">
    <job>
        <echo>
            <lines>
                <files files="${this.args[0]}/test/io/reference/*.txt"/>
            </lines>
        </echo>
    </job>
</oddjob>
```



-----------------------

<div style='font-size: smaller; text-align: center;'>(c) R Gordon Ltd 2005 - Present</div>
