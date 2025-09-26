[HOME](../../../../README.md)
# arooa:property

Provide a definition for a property within
an [arooa:bean-def](../../../../org/oddjob/arooa/deploy/BeanDefinitionBean.md).


Providing property definitions within a BeanDefinition is an alternative
to using annotations in the Java code or providing an
[org.oddjob.arooa.ArooaBeanDescriptor](http://rgordon.co.uk/oddjob/1.6.0/api/org/oddjob/arooa/ArooaBeanDescriptor.html) Arooa class file.

### Property Summary

| Property | Description |
| -------- | ----------- |
| [annotation](#propertyannotation) | An annotation for the property. | 
| [auto](#propertyauto) | Is the property set automatically by the framework. | 
| [configuredHow](#propertyconfiguredhow) |  | 
| [flavour](#propertyflavour) | Not used at present. | 
| [name](#propertyname) | The name of the property. | 
| [type](#propertytype) | The type of the property. | 


### Property Detail
#### annotation <a name="propertyannotation"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ATTRIBUTE</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>No.</td></tr>
</table>

An annotation for the property.

#### auto <a name="propertyauto"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ATTRIBUTE</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>No. Defaults to false.</td></tr>
</table>

Is the property set automatically by the
framework. True/False.

#### configuredHow <a name="propertyconfiguredhow"></a>

<table style='font-size:smaller'>
      <tr><td><i>Access</i></td><td>READ_ONLY</td></tr>
</table>



#### flavour <a name="propertyflavour"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ATTRIBUTE</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>No.</td></tr>
</table>

Not used at present.

#### name <a name="propertyname"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ATTRIBUTE</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>Yes.</td></tr>
</table>

The name of the property.

#### type <a name="propertytype"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ATTRIBUTE</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>Yes.</td></tr>
</table>

The type of the property. One
of ATTRIBUTE, ELEMENT, TEXT, COMPONENT, HIDDEN.


-----------------------

<div style='font-size: smaller; text-align: center;'>(c) R Gordon Ltd 2005 - Present</div>
