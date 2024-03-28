[HOME](../../../../README.md)
# arooa:descriptors

An Arooa Descriptor Factory that is a container
for a collection of other descriptors. The other descriptors will
most probably be [arooa:descriptor](../../../../org/oddjob/arooa/deploy/ArooaDescriptorBean.md)s.


This type can be used wherever an [arooa:descriptor](../../../../org/oddjob/arooa/deploy/ArooaDescriptorBean.md) can
be used.

### Property Summary

| Property | Description |
| -------- | ----------- |
| [descriptors](#propertydescriptors) | A list of Arooa Descriptor Factories. | 


### Example Summary

| Title | Description |
| ----- | ----------- |
| [Example 1](#example1) | Oddjob's descriptor. |


### Property Detail
#### descriptors <a name="propertydescriptors"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ELEMENT</td></tr>
      <tr><td><i>Access</i></td><td>WRITE_ONLY</td></tr>
</table>

A list of Arooa Descriptor Factories.


### Examples
#### Example 1 <a name="example1"></a>

Oddjob's descriptor. Note that it started life before the descriptor
elements were created, and so [is](../../../../org/oddjob/arooa/types/IsType.md) is used instead of
[arooa:bean-def](../../../../org/oddjob/arooa/deploy/BeanDefinitionBean.md) elements.


It can be found <a href="https://github.com/robjg/oddjob/blob/master/src/main/resources/META-INF/arooa.xml">Here on Github</a>




-----------------------

<div style='font-size: smaller; text-align: center;'>(c) R Gordon Ltd 2005 - Present</div>
