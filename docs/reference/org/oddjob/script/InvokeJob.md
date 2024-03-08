[HOME](../../../README.md)
# invoke

Invoke a java method or script snippet.


This is a wrapper for [invoke](../../../org/oddjob/script/InvokeType.md). The result of the
invocation is placed in the <code>result</code> property.


Note that stopping this job will simply attempt to interrupt the
thread invoking the method. The outcome of this will obviously vary.


Oddjob will do it's best to convert arguments to the signature of
the method or operation. An exception will result if it can't achieve
this.

### Property Summary

| Property | Description |
| -------- | ----------- |
| [args](#propertyargs) | An alternative configuration for the values to use as arguments. | 
| [function](#propertyfunction) | The function/method/operation name to call. | 
| [name](#propertyname) | A name, can be any text. | 
| [parameters](#propertyparameters) | The values to use as arguments. | 
| [result](#propertyresult) |  | 
| [source](#propertysource) | The java object or script Invocable on which to invoke the method/function. | 
| [stop](#propertystop) | This flag is set by the stop method and should be examined by any Stoppable jobs in their processing loops. | 


### Example Summary

| Title | Description |
| ----- | ----------- |
| [Example 1](#example1) | Invoking a method on a bean. |
| [Example 2](#example2) | Invoking a static method. |
| [Example 3](#example3) | Examples elsewhere. |


### Property Detail
#### args <a name="propertyargs"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ELEMENT</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>Must match the expected arguments.</td></tr>
</table>

An alternative configuration for the values to use
as arguments. This was added for convenience as setting up a lot
of simple arguments can be tedious. If this property is provided then
parameters is ignored.

#### function <a name="propertyfunction"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ATTRIBUTE</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>Yes.</td></tr>
</table>

The function/method/operation name to call. Note
that for a Java static method the method name must be prefixed with
the word static (see InvokeType examples).

#### name <a name="propertyname"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ATTRIBUTE</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>No.</td></tr>
</table>

A name, can be any text.

#### parameters <a name="propertyparameters"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ELEMENT</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>Must match the expected arguments.</td></tr>
</table>

The values to use as arguments. Note that the
<code>args</code> property may be more convenient for simple arguments.

#### result <a name="propertyresult"></a>

<table style='font-size:smaller'>
      <tr><td><i>Access</i></td><td>READ_ONLY</td></tr>
</table>



#### source <a name="propertysource"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ELEMENT</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>Yes.</td></tr>
</table>

The java object or script Invocable on
which to invoke the method/function. If the method is a Java static
method then this is the class on which to invoke the method.

#### stop <a name="propertystop"></a>

<table style='font-size:smaller'>
      <tr><td><i>Access</i></td><td>READ_ONLY</td></tr>
      <tr><td><i>Required</i></td><td>Read Only.</td></tr>
</table>

This flag is set by the stop method and should
be examined by any Stoppable jobs in their processing loops.


### Examples
#### Example 1 <a name="example1"></a>

Invoking a method on a bean.

```xml
<oddjob>
    <job>
        <sequential>
            <jobs>
                <invoke id="invoke-job" function="echo">
                    <source>
                        <bean
                            class='org.oddjob.script.EchoService'/>
                    </source>
                    <parameters>
                        <value value="Hello"/>
                    </parameters>
                </invoke>
                <echo id="echo">${invoke-job.result}</echo>
            </jobs>
        </sequential>
    </job>
</oddjob>

```


Where <code>EchoService</code> is:

{@oddjob.java.resource org/oddjob/script/EchoService.java}

#### Example 2 <a name="example2"></a>

Invoking a static method. Note that this uses args instead of parameters
for convenience.

```xml
<?xml version="1.0" encoding="UTF-8" standalone="no"?>
<oddjob>
    <job>
        <invoke function="static totalPrice" id="invoke-totalPrice">
            <args>
                <tokenizer text="Red Apples, 24, 47.3"/>
            </args>
            <source>
                <class name="org.oddjob.script.InvokeJobTest"/>
            </source>
        </invoke>
    </job>
</oddjob>

```


#### Example 3 <a name="example3"></a>

Examples elsewhere.


See [invoke](../../../org/oddjob/script/InvokeType.md) for several more examples. Property configuration
is the same for the type and the job.


The [jmx:service](../../../org/oddjob/jmx/JMXServiceJob.md) job has an example of
invoking a JMX operation.


-----------------------

<div style='font-size: smaller; text-align: center;'>(c) R Gordon Ltd 2005 - Present</div>
