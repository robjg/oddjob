[HOME](../../../README.md)
# buffer

A buffer can be used to accumulate output from
one or more jobs which can then be used as input to another job.


A buffer can be used wherever input or output can be specified. A job


A buffer can be initialised with text, or lines of text and will can
also provide it's contents as text.



### Property Summary

| Property | Description |
| -------- | ----------- |
| [lines](#propertylines) | The buffer contents as an array of lines. | 
| [text](#propertytext) | The buffer as a text property. | 


### Example Summary

| Title | Description |
| ----- | ----------- |
| [Example 1](#example1) | Capturing the contents of a file in a buffer. |
| [Example 2](#example2) | Accumulate output in a buffer. |
| [Example 3](#example3) | Write the contents of a buffer to file. |
| [Example 4](#example4) | Using the contents of a buffer as lines. |


### Property Detail
#### lines <a name="propertylines"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ELEMENT</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>No.</td></tr>
</table>

The buffer contents as an array of lines. Either set the contents to be the array
or read the contents of the buffer as an array.

#### text <a name="propertytext"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>TEXT</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>No.</td></tr>
</table>

The buffer as a text property. Either set the
buffer contents from text or get the buffer contents as text.


### Examples
#### Example 1 <a name="example1"></a>

Capturing the contents of a file in a buffer.

```xml
<oddjob id='this'>
    <job>
        <sequential>
            <jobs>
                <variables id='v'>
                    <buff>
                        <buffer/>
                    </buff>
                </variables>
                <copy id='foo'>
                    <from>
                        <file file='${this.args[0]}/work/io/buffer_example.txt'/>
                    </from>
                    <output>
                        <value value='${v.buff}'/>
                    </output>
                </copy>
                <echo id='e'>${v.buff}</echo>
            </jobs>
        </sequential>
    </job>
</oddjob>

```


#### Example 2 <a name="example2"></a>

Accumulate output in a buffer.

```xml
<?xml version="1.0" encoding="UTF-8" standalone="no"?>
<oddjob id="this">
    <job>
        <sequential id="jobs">
            <jobs>
                <variables id="v">
                    <buff>
                        <buffer/>
                    </buff>
                </variables>
                <echo><![CDATA[apples]]>
                    <output>
                        <value value="${v.buff}"/>
                    </output>
                </echo>
                <echo><![CDATA[oranges]]>
                    <output>
                        <value value="${v.buff}"/>
                    </output>
                </echo>
                <echo>${v.buff}</echo>
            </jobs>
        </sequential>
    </job>
</oddjob>

```


#### Example 3 <a name="example3"></a>

Write the contents of a buffer to file. This example also shows
initialising the buffer with a list.

```xml
<oddjob id='this'>
    <job>
        <sequential>
            <jobs>
                <mkdir dir="${this.args[0]}/work/io"/>
                <variables id='v'>
                    <buff>
                        <buffer>
                            <lines>
                                <list>
                                    <values>
                                        <value value="apples"/>
                                        <value value="oranges"/>
                                    </values>
                                </list>
                            </lines>
                        </buffer>
                    </buff>
                </variables>
                <copy>
                    <input>
                        <value value='${v.buff}'/>
                    </input>
                    <output>
                        <file file="${this.args[0]}/work/io/buffer_example.txt"/>
                    </output>
                </copy>
            </jobs>
        </sequential>
    </job>
</oddjob>


```


#### Example 4 <a name="example4"></a>

Using the contents of a buffer as lines. This also shows how a buffer
can be initialised with text.

```xml
<?xml version="1.0" encoding="UTF-8" standalone="no"?>
<oddjob id="this">
    <job>
        <sequential>
            <jobs>
                <variables id="v">
                    <buff>
                        <buffer><![CDATA[apples
oranges]]></buffer>
                    </buff>
                </variables>
                <foreach>
                    <values>
                        <value value="${v.buff.lines}"/>
                    </values>
                    <configuration>
                        <xml>
                            <foreach id="f">
                                <job>
                                    <echo>Line ${f.index} is ${f.current}.</echo>
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
