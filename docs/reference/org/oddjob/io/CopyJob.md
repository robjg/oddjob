[HOME](../../../README.md)
# copy

A Copy job. Copy either:

- A file from one name to another.
- A file or files or directories to a different directory.
- An input (from another job) to a file.
- A file to an output.
- An input to an output.
- A file or input by lines to a Consumer such as a Bean Bus Destination


### Property Summary

| Property | Description |
| -------- | ----------- |
| [consumer](#propertyconsumer) | A consumer of strings. | 
| [directoriesCopied](#propertydirectoriesCopied) | The number of directories copied. | 
| [filesCopied](#propertyfilesCopied) | The number of files copied. | 
| [from](#propertyfrom) | The file to read from. | 
| [input](#propertyinput) | An input stream. | 
| [name](#propertyname) | A name, can be any text. | 
| [output](#propertyoutput) | An output stream. | 
| [to](#propertyto) | The file to write to. | 


### Example Summary

| Title | Description |
| ----- | ----------- |
| [Example 1](#example1) | Copy a file. |
| [Example 2](#example2) | Copy a directory. |
| [Example 3](#example3) | Copy from a file to a buffer. |
| [Example 4](#example4) | Copy into a Bean Bus. |


### Property Detail
#### consumer <a name="propertyconsumer"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ELEMENT</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>No. Will be set automatically in a Bean Bus.</td></tr>
</table>

A consumer of strings. Intended for use as the driver in a
[bus:bus](../../../org/oddjob/beanbus/bus/BasicBusService.md).

#### directoriesCopied <a name="propertydirectoriesCopied"></a>

<table style='font-size:smaller'>
      <tr><td><i>Access</i></td><td>READ_ONLY</td></tr>
      <tr><td><i>Required</i></td><td>Read Only.</td></tr>
</table>

The number of directories copied.

#### filesCopied <a name="propertyfilesCopied"></a>

<table style='font-size:smaller'>
      <tr><td><i>Access</i></td><td>READ_ONLY</td></tr>
      <tr><td><i>Required</i></td><td>Read Only.</td></tr>
</table>

The number of files copied.

#### from <a name="propertyfrom"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ELEMENT</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>Yes unless input supplied.</td></tr>
</table>

The file to read from.

#### input <a name="propertyinput"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ELEMENT</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>Yes unless from supplied.</td></tr>
</table>

An input stream.

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
      <tr><td><i>Required</i></td><td>Yes unless to is supplied.</td></tr>
</table>

An output stream.

#### to <a name="propertyto"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ATTRIBUTE</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>Yes unless output supplied.</td></tr>
</table>

The file to write to.


### Examples
#### Example 1 <a name="example1"></a>

Copy a file.

```xml
<oddjob id='this'>
    <job>
        <copy to='${work.dir}'>
            <from>
                <file file='${base.dir}/test/io/reference/test1.txt'/>
            </from>
        </copy>
    </job>
</oddjob>
```


#### Example 2 <a name="example2"></a>

Copy a directory.

```xml
<oddjob id='this'>
    <job>
        <copy to='${work.dir}'>
            <from>
                <file file='${base.dir}/test/io/reference/a'/>
            </from>
        </copy>
    </job>
</oddjob>
```


#### Example 3 <a name="example3"></a>

Copy from a file to a buffer.

```xml
<oddjob id="this">
    <job>
        <sequential>
            <jobs>
                <copy name="Copy from a file to a buffer">
                    <from>
                        <file
                            file='${this.args[0]}/test/io/reference/test1.txt'/>
                    </from>
                    <output>
                        <identify id='buff'>
                            <value>
                                <buffer/>
                            </value>
                        </identify>

                    </output>
                </copy>
                <echo id='e' name="What's in the file?">${buff}</echo>
            </jobs>
        </sequential>
    </job>
</oddjob>
```


#### Example 4 <a name="example4"></a>

Copy into a Bean Bus. The lines from the file are copied into
the bus where they are mapped with a function that appends 'Foo' before writing
them out in another file.

```xml
<?xml version="1.0" encoding="UTF-8" standalone="no"?>
<oddjob id="oddjob">
    <job>
        <sequential>
            <jobs>
                <properties>
                    <values>
                        <value key="work.dir" value="${java.io.tmpdir}"/>
                    </values>
                </properties>
                <bus:bus xmlns:bus="oddjob:beanbus">
                    <of>
                        <copy name="Copy from a file to lines">
                            <from>
                                <file file="${oddjob.dir}/files/Lines.txt"/>
                            </from>
                        </copy>
                        <bus:map>
                            <function>
                                <value value="#{function(x) { return x + 'Foo' }}"/>
                            </function>
                        </bus:map>
                        <bus:collect>
                            <output>
                                <file file="${work.dir}/LinesFoo.txt"/>
                            </output>
                        </bus:collect>
                    </of>
                </bus:bus>
            </jobs>
        </sequential>
    </job>
</oddjob>
```



-----------------------

<div style='font-size: smaller; text-align: center;'>(c) R Gordon Ltd 2005 - Present</div>
