[HOME](../../../../README.md)
# arooa:conversion

Provide a Bean for use in an [arooa:descriptor](../../../../org/oddjob/arooa/deploy/ArooaDescriptorBean.md) that provides conversions.

### Property Summary

| Property | Description |
| -------- | ----------- |
| [className](#propertyclassname) | The class name of the conversion. | 
| [methodName](#propertymethodname) | The name of the method that provides the conversion if the conversion is not an [org.oddjob.arooa.convert.ConversionProvider](http://rgordon.co.uk/oddjob/1.6.0/api/org/oddjob/arooa/convert/ConversionProvider.html). | 


### Example Summary

| Title | Description |
| ----- | ----------- |
| [Example 1](#example1) | Creating Gremlins |


### Property Detail
#### className <a name="propertyclassname"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ATTRIBUTE</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>yes.</td></tr>
</table>

The class name of the conversion. If a method is provided then
this is the name of the class that has that method and the conversion will be from
objects of that class to the return type of the method. If method is provided then
this class name must refer to an [org.oddjob.arooa.convert.ConversionProvider](http://rgordon.co.uk/oddjob/1.6.0/api/org/oddjob/arooa/convert/ConversionProvider.html).

#### methodName <a name="propertymethodname"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ATTRIBUTE</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>No.</td></tr>
</table>

The name of the method that provides the conversion if the conversion
is not an [org.oddjob.arooa.convert.ConversionProvider](http://rgordon.co.uk/oddjob/1.6.0/api/org/oddjob/arooa/convert/ConversionProvider.html). The conversion will be provided via reflection
at runtime using this method. The return type of this method is used to register the
conversion 'to' class.


### Examples
#### Example 1 <a name="example1"></a>

Creating Gremlins


```xml
<?xml version="1.0" encoding="UTF-8" standalone="no"?>
<oddjob id="oddjob">
    <job>
        <oddjob file="${oddjob.dir}/ReflectionConversionExample.xml" id="example">
            <descriptorFactory>
                <arooa:descriptor xmlns:arooa="http://rgordon.co.uk/oddjob/arooa">
                    <conversions>
                        <arooa:conversion className="org.oddjob.arooa.convert.gremlin.GremlinSupplier" methodName="get"/>
                    </conversions>
                </arooa:descriptor>
            </descriptorFactory>
        </oddjob>
    </job>
</oddjob>
```

```xml
<?xml version="1.0" encoding="UTF-8" standalone="no"?>
<oddjob>
    <job>
        <sequential>
            <jobs>
                <variables id="vars">
                    <gremlin>
                        <bean class="org.oddjob.arooa.convert.gremlin.GremlinSupplier" name="Gizmo"/>
                    </gremlin>
                </variables>
                <bean class="org.oddjob.arooa.convert.gremlin.ThingWithGremlin" id="thing">
                    <myGremlin>
                        <value value="${vars.gremlin}"/>
                    </myGremlin>
                </bean>
            </jobs>
        </sequential>
    </job>
</oddjob>
```



-----------------------

<div style='font-size: smaller; text-align: center;'>(c) R Gordon Ltd 2005 - Present</div>
