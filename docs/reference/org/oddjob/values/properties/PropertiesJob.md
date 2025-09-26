[HOME](../../../../README.md)
# properties

Creates properties that can used to configure
other jobs.


There are four ways to set properties:
<ol>
<li>As Property name/value Pairs in the values property of this job.</li>
<li>By defining the environment attribute to be the prefix to which all
environment variables will be appended as properties.</li>
<li>By using the sets property to provide a number of addition property
sets which are likely to be a reference to properties defined elsewhere.</li>
<li>By defining the Input property to be a File/Resource or some other
type of input.</li>
</ol>
Combinations are possible and the order of evaluation is
as above. Oddjob will do it's usual property substitution using previously
defined property values if required.


If the substitute property is true, property values will be evaluated
for substitution.


The Properties job and [properties](../../../../org/oddjob/values/properties/PropertiesType.md) type are very similar, the difference
between them is that the job defines properties for Oddjob and the type provides
properties for configuring a single job (which could be the
sets property of the property job).



### Property Summary

| Property | Description |
| -------- | ----------- |
| [environment](#propertyenvironment) | The prefix for environment variables. | 
| [extract](#propertyextract) | Extract this prefix form property names. | 
| [fromXML](#propertyfromxml) | If the input for the properties is in XML format. | 
| [input](#propertyinput) | An input source for Properties. | 
| [name](#propertyname) | A name, can be any text. | 
| [override](#propertyoverride) | Properties of this job will override any previously set. | 
| [prefix](#propertyprefix) | Append this prefix to property names. | 
| [properties](#propertyproperties) | Provide all the merged properties defined by this job. | 
| [sets](#propertysets) | Extra properties to be merged into the overall property set. | 
| [stop](#propertystop) | This flag is set by the stop method and should be examined by any Stoppable jobs in their processing loops. | 
| [substitute](#propertysubstitute) | Use substitution for the values of ${} type properties. | 
| [system](#propertysystem) | Set to true to set System properties rather than Oddjob properties. | 
| [values](#propertyvalues) | Properties defined as key value pairs. | 


### Example Summary

| Title | Description |
| ----- | ----------- |
| [Example 1](#example1) | Defining and using a property. |
| [Example 2](#example2) | Defining a property using substitution. |
| [Example 3](#example3) | Loading properties from a class path resource. |
| [Example 4](#example4) | Overriding Properties. |
| [Example 5](#example5) | Capturing Environment Variables. |


### Property Detail
#### environment <a name="propertyenvironment"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ATTRIBUTE</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>No.</td></tr>
</table>

The prefix for environment variables.

#### extract <a name="propertyextract"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ATTRIBUTE</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>No.</td></tr>
</table>

Extract this prefix form property names. Filters
out properties that do not begin with this prefix.

#### fromXML <a name="propertyfromxml"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ATTRIBUTE</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>No, default to false.</td></tr>
</table>

If the input for the properties is in XML format.

#### input <a name="propertyinput"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ELEMENT</td></tr>
      <tr><td><i>Access</i></td><td>WRITE_ONLY</td></tr>
      <tr><td><i>Required</i></td><td>No.</td></tr>
</table>

An input source for Properties.

#### name <a name="propertyname"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ATTRIBUTE</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>No.</td></tr>
</table>

A name, can be any text.

#### override <a name="propertyoverride"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ATTRIBUTE</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>No. Default is false.</td></tr>
</table>

Properties of this job will override any previously
set.

#### prefix <a name="propertyprefix"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ATTRIBUTE</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>No.</td></tr>
</table>

Append this prefix to property names.

#### properties <a name="propertyproperties"></a>

<table style='font-size:smaller'>
      <tr><td><i>Access</i></td><td>READ_ONLY</td></tr>
      <tr><td><i>Required</i></td><td>Read Only.</td></tr>
</table>

Provide all the merged properties defined by this
job.

#### sets <a name="propertysets"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ELEMENT</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>No.</td></tr>
</table>

Extra properties to be merged into the overall
property set.

#### stop <a name="propertystop"></a>

<table style='font-size:smaller'>
      <tr><td><i>Access</i></td><td>READ_ONLY</td></tr>
      <tr><td><i>Required</i></td><td>Read Only.</td></tr>
</table>

This flag is set by the stop method and should
be examined by any Stoppable jobs in their processing loops.

#### substitute <a name="propertysubstitute"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ATTRIBUTE</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>No.</td></tr>
</table>

Use substitution for the values of ${} type
properties.

#### system <a name="propertysystem"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ATTRIBUTE</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>No. Defaults to false</td></tr>
</table>

Set to true to set System properties rather than
Oddjob properties.

#### values <a name="propertyvalues"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ELEMENT</td></tr>
      <tr><td><i>Access</i></td><td>WRITE_ONLY</td></tr>
      <tr><td><i>Required</i></td><td>No.</td></tr>
</table>

Properties defined as key value pairs.


### Examples
#### Example 1 <a name="example1"></a>

Defining and using a property. Note the escape syntax for property
expansion.

```xml
<oddjob>
  <job>
   <sequential>
    <jobs>
     <properties>
      <values>
       <value key="fruit.favourite" value="apple"/>
       <value key="snack.favourite" value="${fruit.favourite}"/>
      </values>
     </properties>
     <echo id="echo">$${snack.favourite} is ${snack.favourite}</echo>
    </jobs>
   </sequential>
  </job>
 </oddjob>
```


#### Example 2 <a name="example2"></a>

Defining a property using substitution. This is the same example as
previously but it is the properties job doing the substitution not
the Oddjob framework. The value of snack.favourite is escaped because we
want ${fruit.favourite} passed into the properties job. If the property
was defined in a file it would not need to be escaped like this.

```xml
<?xml version="1.0" encoding="UTF-8" standalone="no"?>
<oddjob>
    <job>
        <sequential>
            <jobs>
                <properties substitute="true">
                    <values>
                        <value key="fruit.favourite" value="apple"/>
                        <value key="snack.favourite" value="$${fruit.favourite}"/>
                    </values>
                </properties>
                <echo id="echo"><![CDATA[$${snack.favourite} is ${snack.favourite}]]></echo>
            </jobs>
        </sequential>
    </job>
</oddjob>
```


#### Example 3 <a name="example3"></a>

Loading properties from a class path resource.

```xml
<oddjob>
    <job>
        <sequential>
            <jobs>
                <properties>
                    <input>
                        <resource
                            resource='org/oddjob/values/properties/PropertiesJobTest1.properties'/>
                    </input>
                </properties>
                <echo id='echo'>${someones.name}</echo>
            </jobs>
        </sequential>
    </job>
</oddjob>
```


The properties file contains:

```
# properties for test

someones.name = John Smith
someones.name.title = Mr.
someones.address = London
```


This will display
<pre>
John Smith
</pre>

#### Example 4 <a name="example4"></a>

Overriding Properties. Normally setting a property is first come first
set. Using the override property on the properties job makes the properties
defined in that job take priority.

```xml
<oddjob>
  <job>
   <sequential>
    <jobs>
     <properties>
      <values>
       <value key="fruit.favourite" value="apple"/>
      </values>
     </properties>
     <echo id="echo1">$${fruit.favourite} is ${fruit.favourite}</echo>
     <properties>
      <values>
       <value key="fruit.favourite" value="pear"/>
      </values>
     </properties>
     <echo id="echo2">$${fruit.favourite} is ${fruit.favourite}</echo>
     <properties override="true">
      <values>
       <value key="fruit.favourite" value="banana"/>
      </values>
     </properties>
     <echo id="echo3">$${fruit.favourite} is ${fruit.favourite}</echo>
    </jobs>
   </sequential>
  </job>
 </oddjob>
```


This will display
<pre>
${fuit.favourite} is apple
${fuit.favourite} is apple
${fuit.favourite} is banana
</pre>

#### Example 5 <a name="example5"></a>

Capturing Environment Variables. Note that the case sensitivity of
environment variables is Operating System dependent. On Windows
<code>${env.Path}</code> and <code>${env.path}</code> would also yield the
same result. On Unix (generally) only <code>${env.PATH}</code> will work.

```xml
<oddjob>
  <job>
    <sequential>
      <jobs>
        <properties id='props' environment='env' />
        <echo id='echo-path'>Path is ${env.PATH}</echo>
      </jobs>
    </sequential>
  </job>
</oddjob>
```



-----------------------

<div style='font-size: smaller; text-align: center;'>(c) R Gordon Ltd 2005 - Present</div>
