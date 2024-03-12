[HOME](../../../../README.md)
# map

A map allows a map of strings to values to be created.


This map will be converted to a map of string to objects during configuration
of a job.


As yet there is no merging of maps supported by this type.

### Property Summary

| Property | Description |
| -------- | ----------- |
| [element](#propertyelement) |  | 
| [elementType](#propertyelementType) | The required element type. | 
| [values](#propertyvalues) | Any values. | 


### Example Summary

| Title | Description |
| ----- | ----------- |
| [Example 1](#example1) | A simple map with element access. |
| [Example 2](#example2) | Adding additional elements to a map. |


### Property Detail
#### element <a name="propertyelement"></a>

<table style='font-size:smaller'>
      <tr><td><i>Access</i></td><td>READ_ONLY</td></tr>
</table>



#### elementType <a name="propertyelementType"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ELEMENT</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>No. Elements will be left being what they want to
 be.</td></tr>
</table>

The required element type. If this is specified
all elements of the array will attempt to be converted to this type.

#### values <a name="propertyvalues"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ELEMENT</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>No.</td></tr>
</table>

Any values.


### Examples
#### Example 1 <a name="example1"></a>

A simple map with element access.

```xml
<?xml version="1.0" encoding="UTF-8" standalone="no"?>
<oddjob>
    <job>
        <sequential>
            <jobs>
                <properties>
                    <values>
                        <value key="night.type" value="school night"/>
                    </values>
                </properties>
                <variables id="vars">
                    <beersAllowed>
                        <map>
                            <elementType>
                                <class name="int"/>
                            </elementType>
                            <values>
                                <value key="weekend" value="4"/>
                                <value key="school night" value="1"/>
                            </values>
                        </map>
                    </beersAllowed>
                </variables>
                <echo><![CDATA[On a ${night.type} I am allowed ${vars.beersAllowed.element(${night.type})} beer(s).]]></echo>
            </jobs>
        </sequential>
    </job>
</oddjob>
```


The output is:

```
On a school night I am allowed 1 beer(s).
```


#### Example 2 <a name="example2"></a>

Adding additional elements to a map. Also demonstrates iterable access
to the map.

```xml
<?xml version="1.0" encoding="UTF-8" standalone="no"?>
<oddjob id="this">
    <job>
        <sequential>
            <jobs>
                <variables id="vars">
                    <aMap>
                        <map/>
                    </aMap>
                </variables>
                <set>
                    <values>
                        <value key="vars.aMap.add(morning snack)" value="apples"/>
                    </values>
                </set>
                <set>
                    <values>
                        <value key="vars.aMap.add(afternoon snack)" value="bananas"/>
                    </values>
                </set>
                <repeat id="each">
                    <values>
                        <value value="${vars.aMap}"/>
                    </values>
                    <job>
                        <echo><![CDATA[${each.current.key} is ${each.current.value}]]></echo>
                    </job>
                </repeat>
            </jobs>
        </sequential>
    </job>
</oddjob>
```


The output is:

```
morning snack is apples
afternoon snack is bananas
```



-----------------------

<div style='font-size: smaller; text-align: center;'>(c) R Gordon Ltd 2005 - Present</div>
