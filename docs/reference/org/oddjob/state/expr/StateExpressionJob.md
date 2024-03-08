[HOME](../../../../README.md)
# state:evaluate

Evaluate a state expression and become COMPLETE if it is true or INCOMPLETE otherwise.


Can be useful with an [state:if](../../../../org/oddjob/state/IfJob.md). The expression is the same as that described
in [state:watch](../../../../org/oddjob/state/expr/StateExpressionType.md).



### Property Summary

| Property | Description |
| -------- | ----------- |
| [evaluation](#propertyevaluation) | The event that is the result of the evaluation. | 
| [expression](#propertyexpression) | The expression. | 
| [name](#propertyname) | A name, can be any text. | 
| [stop](#propertystop) | This flag is set by the stop method and should be examined by any Stoppable jobs in their processing loops. | 


### Property Detail
#### evaluation <a name="propertyevaluation"></a>

<table style='font-size:smaller'>
      <tr><td><i>Access</i></td><td>READ_ONLY</td></tr>
      <tr><td><i>Required</i></td><td>Read only.</td></tr>
</table>

The event that is the result of the evaluation.

#### expression <a name="propertyexpression"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>TEXT</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>Yes.</td></tr>
</table>

The expression.

#### name <a name="propertyname"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ATTRIBUTE</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>No.</td></tr>
</table>

A name, can be any text.

#### stop <a name="propertystop"></a>

<table style='font-size:smaller'>
      <tr><td><i>Access</i></td><td>READ_ONLY</td></tr>
      <tr><td><i>Required</i></td><td>Read Only.</td></tr>
</table>

This flag is set by the stop method and should
be examined by any Stoppable jobs in their processing loops.


-----------------------

<div style='font-size: smaller; text-align: center;'>(c) R Gordon Ltd 2005 - Present</div>
