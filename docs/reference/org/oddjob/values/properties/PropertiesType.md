[HOME](../../../../README.md)
# properties

A type that evaluates to a java Properties object.


For more information on configuring this please see [properties](../../../../org/oddjob/values/properties/PropertiesJob.md)
as they share the same underlying mechanisms.

### Property Summary

| Property | Description |
| -------- | ----------- |
| [extract](#propertyextract) | Extract this prefix form property names. | 
| [fromXML](#propertyfromXML) | If the input for the properties is in XML format. | 
| [input](#propertyinput) | An input source for Properties. | 
| [prefix](#propertyprefix) | Append this prefix to property names. | 
| [sets](#propertysets) | Extra properties to be merged into the overall property set. | 
| [source](#propertysource) |  | 
| [substitute](#propertysubstitute) | Use substitution for the values of ${} type properties. | 
| [values](#propertyvalues) | Properties defined as key value pairs. | 


### Example Summary

| Title | Description |
| ----- | ----------- |
| [Example 1](#example1) | Defining a single property. |


### Property Detail
#### extract <a name="propertyextract"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ATTRIBUTE</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>No.</td></tr>
</table>

Extract this prefix form property names. Filters
out properties that do not begin with this prefix.

#### fromXML <a name="propertyfromXML"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ATTRIBUTE</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>No, defaults to false.</td></tr>
</table>

If the input for the properties is in XML format.

#### input <a name="propertyinput"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ELEMENT</td></tr>
      <tr><td><i>Access</i></td><td>WRITE_ONLY</td></tr>
      <tr><td><i>Required</i></td><td>No.</td></tr>
</table>

An input source for Properties.

#### prefix <a name="propertyprefix"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ATTRIBUTE</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>No.</td></tr>
</table>

Append this prefix to property names.

#### sets <a name="propertysets"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ELEMENT</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>No.</td></tr>
</table>

Extra properties to be merged into the overall
property set.

#### source <a name="propertysource"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ELEMENT</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
</table>



#### substitute <a name="propertysubstitute"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ATTRIBUTE</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>No.</td></tr>
</table>

Use substitution for the values of ${} type
properties.

#### values <a name="propertyvalues"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ELEMENT</td></tr>
      <tr><td><i>Access</i></td><td>WRITE_ONLY</td></tr>
      <tr><td><i>Required</i></td><td>No.</td></tr>
</table>

Properties defined as key value pairs.


### Examples
#### Example 1 <a name="example1"></a>

Defining a single property.

<pre>
&lt;variables id="vars"&gt;
&lt;props&gt;
&lt;properties&gt;
&lt;values&gt;
&lt;value key="snack.fruit" value="apple"/&gt;
&lt;/values&gt;
&lt;/properties&gt;
&lt;props/&gt;
&lt;/variables&gt;
</pre>


-----------------------

<div style='font-size: smaller; text-align: center;'>(c) R Gordon Ltd 2005 - Present</div>
