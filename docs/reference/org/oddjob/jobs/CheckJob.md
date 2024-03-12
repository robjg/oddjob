[HOME](../../../README.md)
# check

Checks a value for certain criteria. This
job is analogous to the Unix 'test' command.


This Job will COMPLETE if all checks pass. It will be INCOMPLETE
if any fail.


The conditional values are converted into the type of the
value before the checks are made. Thus in the example below
if the row count property is an integer, the 1000 is converted
into an integer for the comparison.


If the value property is not provided the job will be INCOMPLETE unless
the null property is set to true.

### Property Summary

| Property | Description |
| -------- | ----------- |
| [eq](#propertyeq) | The value must be equal to this. | 
| [ge](#propertyge) | The value must be greater than or equal to this. | 
| [gt](#propertygt) | The value must be greater than this. | 
| [le](#propertyle) | The value must be less than or equals to this. | 
| [lt](#propertylt) | The value must be less than this. | 
| [name](#propertyname) | The name of this job. | 
| [ne](#propertyne) | The value must be not equal to this. | 
| [null](#propertynull) | Must the value be null for the check to pass. | 
| [result](#propertyresult) | The result of the check. | 
| [value](#propertyvalue) | The value to check. | 
| [z](#propertyz) | The value to check. | 


### Example Summary

| Title | Description |
| ----- | ----------- |
| [Example 1](#example1) | Example text comparisons. |
| [Example 2](#example2) | Numeric checks. |
| [Example 3](#example3) | Check a Property Exists. |


### Property Detail
#### eq <a name="propertyeq"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ATTRIBUTE</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>No.</td></tr>
</table>

The value must be equal to this.

#### ge <a name="propertyge"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ATTRIBUTE</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>No.</td></tr>
</table>

The value must be greater than or equal to this.

#### gt <a name="propertygt"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ATTRIBUTE</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>No.</td></tr>
</table>

The value must be greater than this.

#### le <a name="propertyle"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ATTRIBUTE</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>No.</td></tr>
</table>

The value must be less than or equals to this.

#### lt <a name="propertylt"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ATTRIBUTE</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>No.</td></tr>
</table>

The value must be less than this.

#### name <a name="propertyname"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ATTRIBUTE</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>No.</td></tr>
</table>

The name of this job. Can be any text.

#### ne <a name="propertyne"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ATTRIBUTE</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>No.</td></tr>
</table>

The value must be not equal to this.

#### null <a name="propertynull"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ATTRIBUTE</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>No, if this does exist the check value null will fail.</td></tr>
</table>

Must the value be null for the check to pass.
True the value must be null. False it must not be null. If this
property is true other checks will cause an exception because they
require the value property has a value.

#### result <a name="propertyresult"></a>

<table style='font-size:smaller'>
      <tr><td><i>Access</i></td><td>READ_ONLY</td></tr>
</table>

The result of the check.

#### value <a name="propertyvalue"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ATTRIBUTE</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>No, but the check value is not null will fail.</td></tr>
</table>

The value to check.

#### z <a name="propertyz"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ATTRIBUTE</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>No, but the check value is not null will fail.</td></tr>
</table>

The value to check.


### Examples
#### Example 1 <a name="example1"></a>

Example text comparisons. All these checks COMPLETE.

```xml
<oddjob>
  <job>
    <sequential>
      <jobs>
        <check value='apple' eq='apple' />
        <check value='apple' ne='orange' />
        <check value='apple' lt='orange' />
        <check value='apple' le='apple' />
        <check value='orange' gt='apple' />
        <check value='orange' ge='orange' />
        <check value='anything' null='false' />
        <check value='${missing}' null='true' />
        <check value='pear' z='false' />
        <check value='' z='true' />
      </jobs>
    </sequential>
  </job>
</oddjob>
```


Checks that are INCOMPLETE.

```xml
<oddjob>
  <job>
    <!-- needed to force predictable state transitions for the unit test, 
      so Oddjob goes to INCOMPLETE not ACTIVE first. -->
    <state:join xmlns:state="http://rgordon.co.uk/oddjob/state">
      <job>
        <parallel id="all-checks">
          <jobs>
            <check value='apple' eq='orange' />
            <check value='apple' ne='apple' />
            <check value='orange' lt='apple' />
            <check value='orange' le='apple' />
            <check value='apple' gt='orange' />
            <check value='apple' ge='orange' />
            <check value='anything' null='true' />
            <check value='${missing}' null='false' />
          </jobs>
        </parallel>
      </job>
    </state:join>
  </job>
</oddjob>
```


#### Example 2 <a name="example2"></a>

Numeric checks. Note that the value must be the numeric value. The
operand attributes values are converted to the type of the value before
the comparison. If the value was "999" and the lt was "${sequence.current}"
this check would not be COMPLETE because the text "999" is greater than
"1000".

```xml
<oddjob>
  <job>
    <sequential>
      <jobs>
        <sequence id='sequence' from='1000' />
        <check value='${sequence.current}' eq='1000' />
        <check value='${sequence.current}' ne='999' />
        <check value='${sequence.current}' lt='1001' />
        <check value='${sequence.current}' le='1000' />
        <check value='${sequence.current}' gt='999' />
        <check value='${sequence.current}' ge='1000' />
      </jobs>
    </sequential>
  </job>
</oddjob>
```


#### Example 3 <a name="example3"></a>

Check a Property Exists. The second check will be INCOMPLETE because the
property doesn't exist.

```xml
<oddjob>
  <job>
    <sequential>
      <jobs>
        <properties>
          <values>
            <value key="property.that.exists" value="some-value" />
          </values>
        </properties>
        <check name="Check Something that Exists" value='${property.that.exists}'
          id="should-complete" />
        <check name="Check Something that doesn't Exist" value='${property.doesnt.exist}'
          id="should-incomplete" />
      </jobs>
    </sequential>
  </job>
</oddjob>
```



-----------------------

<div style='font-size: smaller; text-align: center;'>(c) R Gordon Ltd 2005 - Present</div>
