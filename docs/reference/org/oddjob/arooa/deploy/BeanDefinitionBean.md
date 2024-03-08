[HOME](../../../../README.md)
# arooa:bean-def

Provide an element to class name mapping for a
java bean. Additionally, allows an [org.oddjob.arooa.ArooaBeanDescriptor](http://rgordon.co.uk/oddjob/1.6.0/api/org/oddjob/arooa/ArooaBeanDescriptor.html) to be
provided for the class by specifying additional [arooa:property](../../../../org/oddjob/arooa/deploy/PropertyDefinitionBean.md)s.

### Property Summary

| Property | Description |
| -------- | ----------- |
| [annotations](#propertyannotations) | A list of [arooa:annotation](../../../../org/oddjob/arooa/deploy/AnnotationDefinitionBean.md). | 
| [className](#propertyclassName) | The class name for the bean. | 
| [design](#propertydesign) | A class name that provides a DesignFactory for the bean. | 
| [designFactory](#propertydesignFactory) | A class name that provides a DesignFactory for the bean. | 
| [element](#propertyelement) | The unqualified element name for the mapping. | 
| [interceptor](#propertyinterceptor) | A ParsingInterceptor. | 
| [properties](#propertyproperties) | A list of [arooa:property](../../../../org/oddjob/arooa/deploy/PropertyDefinitionBean.md)s | 


### Property Detail
#### annotations <a name="propertyannotations"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ELEMENT</td></tr>
      <tr><td><i>Access</i></td><td>WRITE_ONLY</td></tr>
      <tr><td><i>Required</i></td><td>No.</td></tr>
</table>

A list of [arooa:annotation](../../../../org/oddjob/arooa/deploy/AnnotationDefinitionBean.md).

#### className <a name="propertyclassName"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ATTRIBUTE</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>Yes.</td></tr>
</table>

The class name for the bean.

#### design <a name="propertydesign"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ELEMENT</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>No.</td></tr>
</table>

A class name that provides a
DesignFactory for the bean.

#### designFactory <a name="propertydesignFactory"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ATTRIBUTE</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>No.</td></tr>
</table>

A class name that provides a
DesignFactory for the bean.

#### element <a name="propertyelement"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ATTRIBUTE</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>No, if this definition is only providing
 an ArooaBeanDescriptor.</td></tr>
</table>

The unqualified element name for the
mapping.

#### interceptor <a name="propertyinterceptor"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ELEMENT</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>No.</td></tr>
</table>

A ParsingInterceptor. This
will change to a class name in future releases.

#### properties <a name="propertyproperties"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ELEMENT</td></tr>
      <tr><td><i>Access</i></td><td>WRITE_ONLY</td></tr>
      <tr><td><i>Required</i></td><td>No.</td></tr>
</table>

A list of [arooa:property](../../../../org/oddjob/arooa/deploy/PropertyDefinitionBean.md)s


-----------------------

<div style='font-size: smaller; text-align: center;'>(c) R Gordon Ltd 2005 - Present</div>
