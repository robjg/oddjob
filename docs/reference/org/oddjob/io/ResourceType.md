[HOME](../../../README.md)
# resource

Specify a resource on the class path.


This uses
Oddjob's internal class path to find the resource which includes all
Oddballs. Oddballs will be searched in the order they were loaded.

### Property Summary

| Property | Description |
| -------- | ----------- |
| [resource](#propertyresource) | The resource | 


### Example Summary

| Title | Description |
| ----- | ----------- |
| [Example 1](#example1) | Specifiy properties as a resource on the class path. |


### Property Detail
#### resource <a name="propertyresource"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ATTRIBUTE</td></tr>
      <tr><td><i>Access</i></td><td>WRITE_ONLY</td></tr>
      <tr><td><i>Required</i></td><td>Yes.</td></tr>
</table>

The resource


### Examples
#### Example 1 <a name="example1"></a>

Specifiy properties as a resource on the class path.

<pre>
&lt;variables id='props'&gt;
&lt;properties&gt;
&lt;resource resource="org/oddjob/AResource.props"/&gt;
&lt;/properties&gt;
&lt;/variables&gt;
</pre>


-----------------------

<div style='font-size: smaller; text-align: center;'>(c) R Gordon Ltd 2005 - Present</div>
