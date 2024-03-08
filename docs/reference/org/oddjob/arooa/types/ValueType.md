[HOME](../../../../README.md)
# value

A simple value. This is the most commonly used
type.


A value can be:

- Any simple type, either text or a number or boolean.
- It can also be a reference to any other type somewhere else. i.e. value can contain a ${someid.anyvalue} reference.



The ValueType value is expected to be an [org.oddjob.arooa.ArooaValue](http://rgordon.co.uk/oddjob/1.6.0/api/org/oddjob/arooa/ArooaValue.html). This ensures
that references to other types aren't converted until the ValueType
itself is converted. Because of this, simple values will be wrapped
as an [org.oddjob.arooa.types.ArooaObject](http://rgordon.co.uk/oddjob/1.6.0/api/org/oddjob/arooa/types/ArooaObject.html) by the automatic internal conversion. For
normal use this is entirely transparent. An example below demonstrates
this.

### Property Summary

| Property | Description |
| -------- | ----------- |
| [value](#propertyvalue) | The value. | 


### Example Summary

| Title | Description |
| ----- | ----------- |
| [Example 1](#example1) | A value that is a constant string value. |
| [Example 2](#example2) | A value that is a reference to a property. |
| [Example 3](#example3) | Examining the internals of a value in Oddjob. |


### Property Detail
#### value <a name="propertyvalue"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ATTRIBUTE</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>No.</td></tr>
</table>

The value.


### Examples
#### Example 1 <a name="example1"></a>

A value that is a constant string value.

_java.io.IOException: No Resource Found: 'org/oddjob/arooa/types/ValueTypeExample1.xml', classloader=java.net.URLClassLoader@2eafb4e7_

#### Example 2 <a name="example2"></a>

A value that is a reference to a property.

_java.io.IOException: No Resource Found: 'org/oddjob/arooa/types/ValueTypeExample2.xml', classloader=java.net.URLClassLoader@2eafb4e7_

#### Example 3 <a name="example3"></a>

Examining the internals of a value in Oddjob.

```xml
<?xml version="1.0" encoding="UTF-8" standalone="no"?>
<oddjob>
    <job>
        <sequential>
            <jobs>
                <variables id="vars">
                    <fruit>
                        <value value="apple"/>
                    </fruit>
                </variables>
                <echo><![CDATA[${vars.fruit}
${vars.fruit.class}
${vars.fruit.value}
${vars.fruit.value.class}
${vars.fruit.value.value}
${vars.fruit.value.value.class}]]></echo>
            </jobs>
        </sequential>
    </job>
</oddjob>

```


The output is:

<pre>
apple
class org.oddjob.arooa.types.ValueType
apple
class org.oddjob.arooa.types.ArooaObject
apple
class java.lang.String
</pre>



-----------------------

<div style='font-size: smaller; text-align: center;'>(c) R Gordon Ltd 2005 - Present</div>
