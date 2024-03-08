[HOME](../../../README.md)
# set

A job which sets properties in other
jobs when it executes.

### Property Summary

| Property | Description |
| -------- | ----------- |
| [name](#propertyname) | A name, can be any text. | 
| [stop](#propertystop) | This flag is set by the stop method and should be examined by any Stoppable jobs in their processing loops. | 
| [values](#propertyvalues) | The thing to set on the property that is given by the key of this mapped property. | 


### Example Summary

| Title | Description |
| ----- | ----------- |
| [Example 1](#example1) | Setting lots of properties. |


### Property Detail
#### name <a name="propertyname"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ATTRIBUTE</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>No.</td></tr>
</table>

A name, can be any text.

#### stop <a name="propertystop"></a>

<table style='font-size:smaller'>
      <tr><td><i>Access</i></td><td>READ_ONLY</td></tr>
      <tr><td><i>Required</i></td><td>Read Only.</td></tr>
</table>

This flag is set by the stop method and should
be examined by any Stoppable jobs in their processing loops.

#### values <a name="propertyvalues"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ELEMENT</td></tr>
      <tr><td><i>Access</i></td><td>WRITE_ONLY</td></tr>
      <tr><td><i>Required</i></td><td>No, but pointless if not provided.</td></tr>
</table>

The thing to set on the property that is given by
the key of this mapped property.


### Examples
#### Example 1 <a name="example1"></a>

Setting lots of properties.


```xml
<oddjob>
    <job>
        <sequential>
            <jobs>
                <set>
                    <values>
                        <value key="check.checkBoolean" value="true"/>
                        <value key="check.checkByte" value="127"/>
                        <value key="check.checkChar" value="a"/>
                        <date key="check.checkDate" date="2005-12-25" timeZone="GMT"/>
                        <value key="check.checkDouble" value="9E99"/>
                        <value key="check.checkFloat" value="1.23"/>
                        <value key="check.checkInt" value="1234567"/>
                        <value key="check.checkLong" value="2345678"/>
                        <value key="check.checkShort" value="123"/>
                        <value key="check.checkString" value="hello"/>
                    </values>
                </set>
                <bean id="check" class="org.oddjob.values.CheckBasicSetters"/>
            </jobs>
        </sequential>
    </job>
</oddjob>
```



This is the configuration for one
of the tests.


-----------------------

<div style='font-size: smaller; text-align: center;'>(c) R Gordon Ltd 2005 - Present</div>
