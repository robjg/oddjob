[HOME](../../../../README.md)
# xml

A type that converts it's XML contents into
a String.

### Property Summary

| Property | Description |
| -------- | ----------- |
| [arooaContext](#propertyarooacontext) |  | 
| [xml](#propertyxml) | This is only used internally. | 


### Example Summary

| Title | Description |
| ----- | ----------- |
| [Example 1](#example1) | Capture XML in a variable. |


### Property Detail
#### arooaContext <a name="propertyarooacontext"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ELEMENT</td></tr>
      <tr><td><i>Access</i></td><td>WRITE_ONLY</td></tr>
</table>



#### xml <a name="propertyxml"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ATTRIBUTE</td></tr>
      <tr><td><i>Access</i></td><td>WRITE_ONLY</td></tr>
      <tr><td><i>Required</i></td><td>Irrelevant.</td></tr>
</table>

This is only used internally. It can't
be set via configuration because all contents are converted
into text XML.


### Examples
#### Example 1 <a name="example1"></a>

Capture XML in a variable.

```xml
<oddjob>
    <job>
        <sequential>
            <jobs>
                <variables id="vars">
                    <myXML>
                        <xml>
                            <some-xml>
                                <![CDATA[Some Text]]>
                            </some-xml>
                        </xml>
                    </myXML>
                </variables>
            </jobs>
        </sequential>
    </job>
</oddjob>
```



-----------------------

<div style='font-size: smaller; text-align: center;'>(c) R Gordon Ltd 2005 - Present</div>
