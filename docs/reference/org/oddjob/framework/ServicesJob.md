[HOME](../../../README.md)
# services

Allows objects to be registered that will
automatically be injected into subsequent components that
are configured for automatic dependency injection.



### Property Summary

| Property | Description |
| -------- | ----------- |
| [name](#propertyname) | A name, can be any text. | 
| [registeredServices](#propertyregisteredServices) | Service definitions. | 
| [services](#propertyservices) | Provide access to the registered services. | 
| [stop](#propertystop) | This flag is set by the stop method and should be examined by any Stoppable jobs in their processing loops. | 


### Example Summary

| Title | Description |
| ----- | ----------- |
| [Example 1](#example1) | The Development guide has numerous examples using this job. |


### Property Detail
#### name <a name="propertyname"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ATTRIBUTE</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>No.</td></tr>
</table>

A name, can be any text.

#### registeredServices <a name="propertyregisteredServices"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ELEMENT</td></tr>
      <tr><td><i>Access</i></td><td>WRITE_ONLY</td></tr>
      <tr><td><i>Required</i></td><td>No, but pointless if missing.</td></tr>
</table>

Service definitions. These are simple beans
that define the services being registered. Because of their
simplicity they do not have their own type and can be specified
using the [is](../../../org/oddjob/arooa/types/IsType.md).


The properties of the service definition beans are:
<dl>
<dt>service</dt>
<dd>The service object being registered.</dd>

<dt>qualifier</dt>
<dd>A qualified that provides extra information for the
type of service.</dd>

<dt>intransigent</dt>
<dd>Whether or not to supply a service if the qualifier does
not match that which is required.</dd>
</dl>

#### services <a name="propertyservices"></a>

<table style='font-size:smaller'>
      <tr><td><i>Access</i></td><td>READ_ONLY</td></tr>
</table>

Provide access to the registered services.


Services
are registered by name using object `toString` and then if qualified
';' and the qualifier. If this job has an id `my-services` and
the service has a toString of `MyCafe` and it is qualified with qualifier
`Vegetarian` then it could be referenced as:
<pre>
${my-services.services.service(MyCafe;Vegetarian)}
</pre>

#### stop <a name="propertystop"></a>

<table style='font-size:smaller'>
      <tr><td><i>Access</i></td><td>READ_ONLY</td></tr>
      <tr><td><i>Required</i></td><td>Read Only.</td></tr>
</table>

This flag is set by the stop method and should
be examined by any Stoppable jobs in their processing loops.


### Examples
#### Example 1 <a name="example1"></a>

The Development guide has numerous examples using this job.


-----------------------

<div style='font-size: smaller; text-align: center;'>(c) R Gordon Ltd 2005 - Present</div>
