[HOME](../../../../README.md)
# bus:collect

A component that collects beans in a list. Additionally, this component may
be used in the middle of a [bus:bus](../../../../org/oddjob/beanbus/bus/BasicBusService.md) so can act as a Wire Tap.

### Property Summary

| Property | Description |
| -------- | ----------- |
| [beans](#propertybeans) | The same as list. | 
| [count](#propertycount) | Count of items collected. | 
| [keyMapper](#propertykeyMapper) | A function that will extract a key from an item. | 
| [list](#propertylist) | The collected items as list container. | 
| [map](#propertymap) | The collected items as a map container. | 
| [name](#propertyname) | The name of this component. | 
| [output](#propertyoutput) | An output stream that items will be written to as strings. | 
| [to](#propertyto) | The next component in a bus. | 
| [valueMapper](#propertyvalueMapper) | A function that will extract a value from an item to put in the map. | 


### Example Summary

| Title | Description |
| ----- | ----------- |
| [Example 1](#example1) | There are many examples elsewhere. |


### Property Detail
#### beans <a name="propertybeans"></a>

<table style='font-size:smaller'>
      <tr><td><i>Access</i></td><td>READ_ONLY</td></tr>
      <tr><td><i>Required</i></td><td>Read only.</td></tr>
</table>

The same as list. Prefer list, will be deprecated in future versions.

#### count <a name="propertycount"></a>

<table style='font-size:smaller'>
      <tr><td><i>Access</i></td><td>READ_ONLY</td></tr>
      <tr><td><i>Required</i></td><td>R/O.</td></tr>
</table>

Count of items collected.

#### keyMapper <a name="propertykeyMapper"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ELEMENT</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>No.</td></tr>
</table>

A function that will extract a key from an item. If this property is set, items will
be available as a map, not a list.

#### list <a name="propertylist"></a>

<table style='font-size:smaller'>
      <tr><td><i>Access</i></td><td>READ_ONLY</td></tr>
      <tr><td><i>Required</i></td><td>Read only.</td></tr>
</table>

The collected items as list container. Conversions exist so that this list
property can be used as a list or the values can be accessed using an indexed accessor on the value property.

#### map <a name="propertymap"></a>

<table style='font-size:smaller'>
      <tr><td><i>Access</i></td><td>READ_ONLY</td></tr>
      <tr><td><i>Required</i></td><td>Read only.</td></tr>
</table>

The collected items as a map container. Conversions exist so that this map
property can be used as a map or the values can be accessed using a mapped accessor on the value property.

#### name <a name="propertyname"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ATTRIBUTE</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>No.</td></tr>
</table>

The name of this component.

#### output <a name="propertyoutput"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ELEMENT</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>No.</td></tr>
</table>

An output stream that items will be written to as strings.

#### to <a name="propertyto"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ELEMENT</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>No.</td></tr>
</table>

The next component in a bus. Set automatically in a
[bus:bus](../../../../org/oddjob/beanbus/bus/BasicBusService.md).

#### valueMapper <a name="propertyvalueMapper"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ELEMENT</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>No.</td></tr>
</table>

A function that will extract a value from an item to put in the map. If this property
is set, but [bus:collect](../../../../org/oddjob/beanbus/destinations/BusCollect.md) is not, then it will be silently ignored.


### Examples
#### Example 1 <a name="example1"></a>

There are many examples elsewhere.

- [org.oddjob.beanbus.destinations.Batcher](http://rgordon.co.uk/oddjob/1.6.0/api/org/oddjob/beanbus/destinations/Batcher.html)
- [org.oddjob.beanbus.destinations.BeanCopy](http://rgordon.co.uk/oddjob/1.6.0/api/org/oddjob/beanbus/destinations/BeanCopy.html)
- [bus:queue](../../../../org/oddjob/beanbus/destinations/BusQueue.md)
- [bus:limit](../../../../org/oddjob/beanbus/destinations/BusLimit.md)



-----------------------

<div style='font-size: smaller; text-align: center;'>(c) R Gordon Ltd 2005 - Present</div>
