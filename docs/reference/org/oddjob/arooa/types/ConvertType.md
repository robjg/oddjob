[HOME](../../../../README.md)
# convert

Convert a value to the given Java Class. Most of
the time Oddjob's own automatic conversions are fine for setting
job properties but occasionally it can be useful to force a conversion
to a different type.


This type uses Oddjob's internal converters itself to perform the
conversion.


The <code>is</code> property can provide direct access to the converted
value. This can be useful for gaining access to a Java type from Oddjob's
wrapper types.

### Property Summary

| Property | Description |
| -------- | ----------- |
| [is](#propertyis) | The result of the conversion. | 
| [to](#propertyto) | The name of the java class to convert to. | 
| [value](#propertyvalue) | The value to convert. | 


### Example Summary

| Title | Description |
| ----- | ----------- |
| [Example 1](#example1) | Convert a delimited list to an array of Strings. |
| [Example 2](#example2) | Demonstrate the use of the is property. |


### Property Detail
#### is <a name="propertyis"></a>

<table style='font-size:smaller'>
      <tr><td><i>Access</i></td><td>READ_ONLY</td></tr>
      <tr><td><i>Required</i></td><td>Read Only.</td></tr>
</table>

The result of the conversion.

#### to <a name="propertyto"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ELEMENT</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>Yes.</td></tr>
</table>

The name of the java class to convert to.

#### value <a name="propertyvalue"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ELEMENT</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>No. If missing the result of the conversion will be
 null.</td></tr>
</table>

The value to convert.


### Examples
#### Example 1 <a name="example1"></a>

Convert a delimited list to an array of Strings.

```xml
<?xml version="1.0" encoding="UTF-8" standalone="no"?>
<oddjob id="this">
    <job>
        <foreach>
            <values>
                <convert>
                    <to>
                        <class name="[Ljava.lang.String;"/>
                    </to>
                    <value>
                        <value
                            value='"grapes, red", "grapes, white", gratefruit'/>
                    </value>
                </convert>
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
    </job>
</oddjob>
```


The output is:

```
grapes, red
grapes, white
gratefruit
```


#### Example 2 <a name="example2"></a>

Demonstrate the use of the is property.

```xml
<?xml version="1.0" encoding="UTF-8" standalone="no"?>
<oddjob id="this">
    <job>
        <sequential>
            <jobs>
                <variables id="vars">
                    <aNumber>
                        <convert>
                            <to>
                                <class name="java.lang.Integer"/>
                            </to>
                            <value>
                                <value value="42"/>
                            </value>
                        </convert>
                    </aNumber>
                </variables>
                <echo><![CDATA[${vars.aNumber}
${vars.aNumber.class.name}
${vars.aNumber.value}
${vars.aNumber.value.class.name}
${vars.aNumber.is}
${vars.aNumber.is.class.name}]]></echo>
            </jobs>
        </sequential>
    </job>
</oddjob>
```


The output is:

```
42
org.oddjob.arooa.types.ConvertType
42
org.oddjob.arooa.types.ValueType
42
java.lang.Integer
```



-----------------------

<div style='font-size: smaller; text-align: center;'>(c) R Gordon Ltd 2005 - Present</div>
