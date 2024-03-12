[HOME](../../../../README.md)
# arooa:descriptor

A definition of an Arooa descriptor.

### Property Summary

| Property | Description |
| -------- | ----------- |
| [components](#propertycomponents) | A list of [arooa:bean-def](../../../../org/oddjob/arooa/deploy/BeanDefinitionBean.md)s for components. | 
| [conversions](#propertyconversions) | List of class names that must implement the [org.oddjob.arooa.convert.ConversionProvider](http://rgordon.co.uk/oddjob/1.6.0/api/org/oddjob/arooa/convert/ConversionProvider.html) interface. | 
| [namespace](#propertynamespace) | The name space that applies to all elements defined in definitions. | 
| [prefix](#propertyprefix) | The default prefix for the name space. | 
| [values](#propertyvalues) | A list of [arooa:bean-def](../../../../org/oddjob/arooa/deploy/BeanDefinitionBean.md)s for values. | 


### Example Summary

| Title | Description |
| ----- | ----------- |
| [Example 1](#example1) | See the Dev Guide. |
| [Example 2](#example2) | The descriptor for the JMX client and server. |


### Property Detail
#### components <a name="propertycomponents"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ELEMENT</td></tr>
      <tr><td><i>Access</i></td><td>WRITE_ONLY</td></tr>
      <tr><td><i>Required</i></td><td>No.</td></tr>
</table>

A list of [arooa:bean-def](../../../../org/oddjob/arooa/deploy/BeanDefinitionBean.md)s for components.

#### conversions <a name="propertyconversions"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ELEMENT</td></tr>
      <tr><td><i>Access</i></td><td>WRITE_ONLY</td></tr>
      <tr><td><i>Required</i></td><td>No.</td></tr>
</table>

List of class names that must implement
the [org.oddjob.arooa.convert.ConversionProvider](http://rgordon.co.uk/oddjob/1.6.0/api/org/oddjob/arooa/convert/ConversionProvider.html) interface.

#### namespace <a name="propertynamespace"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ATTRIBUTE</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>No.</td></tr>
</table>

The name space that applies to
all elements defined in definitions.

#### prefix <a name="propertyprefix"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ATTRIBUTE</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>Yes if name space is provided.</td></tr>
</table>

The default prefix for the name space.

#### values <a name="propertyvalues"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ELEMENT</td></tr>
      <tr><td><i>Access</i></td><td>WRITE_ONLY</td></tr>
      <tr><td><i>Required</i></td><td>No.</td></tr>
</table>

A list of [arooa:bean-def](../../../../org/oddjob/arooa/deploy/BeanDefinitionBean.md)s for values.


### Examples
#### Example 1 <a name="example1"></a>

See the Dev Guide. There is an example of a custom descriptor
<a href="http://rgordon.co.uk/projects/oddjob/devguide/oddballs.html">here</a>.

#### Example 2 <a name="example2"></a>

The descriptor for the JMX client and server. This is the internal descriptor
used by Oddjob.


_java.io.IOException: No Resource Found: 'org/oddjob/jmx/jmx.xml', classloader=java.net.URLClassLoader@2eafb4e7_


-----------------------

<div style='font-size: smaller; text-align: center;'>(c) R Gordon Ltd 2005 - Present</div>
