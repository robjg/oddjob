[HOME](../../../README.md)
# jmx:service

Expose a JMX Server so that Oddjob jobs
can interact with it.


Features of this service include:

- Attributes of MBeans can be read and changed.
- Operations on MBeans can be invoked.
- MBeans are displayed as part of Oddjob's job hierarchy within Oddjob Explorer.


MBeans are identified as part of Oddjob's property expansion
syntax using their full Object Names. If this service is given
the id 'my-jmx-world' an MBean in the domain 'mydomain' and name
'type=greeting,name=hello' would be identified from another Oddjob
job with the expression:

<pre>
${my-jmx-world/mydomain:type=greeting,name=hello}
</pre>

Note that what is being referenced here is an Oddjob wrapper around
the MBean that allows operations and attributes of the MBean to accessed
elsewhere. What is referenced is not an MBean instance.


The example below shows an MBean (wrapper) being passed as the source
property to an [invoke](../../../org/oddjob/script/InvokeJob.md).


Attributes of the MBean can be accessed as if they were properties of
the MBean. If the MBean above has an attribute 'FullText' its value
can be accessed using the expression:

<pre>
${my-jmx-world/mydomain:type=greeting,name=hello.FullText}
</pre>

If an MBean Object Name contains dots (.) it must be quoted using double
quotes. If the domain in the above example was my.super.domain the
MBean can be identified with the expression:

<pre>
${my-jmx-world/"my.super.domain:type=greeting,name=hello"}
</pre>

and the attribute with:

<pre>
${my-jmx-world/"my.super.domain:type=greeting,name=hello".FullText}
</pre>

Note that this support for quoting does not apply to Oddjob property
expansion expressions in general - only too these MBean identifiers.

### Property Summary

| Property | Description |
| -------- | ----------- |
| [connection](#propertyconnection) | The JMX service URL. | 
| [environment](#propertyenvironment) | The environment. | 
| [heartbeat](#propertyheartbeat) | The heart beat interval, in milliseconds. | 
| [name](#propertyname) | A name, can be any text. | 


### Example Summary

| Title | Description |
| ----- | ----------- |
| [Example 1](#example1) | This example demonstrates reading an attribute, setting an attribute and invoking an operation. |


### Property Detail
#### connection <a name="propertyconnection"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ATTRIBUTE</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>No. If not provided the client connects to the Platform 
 MBean Server for the current VM.</td></tr>
</table>

The JMX service URL. This is can be either
the full blown convoluted JMX Service URL starting
<code>service.jmx....</code> or it can just be the last part of the
form <code>hostname[:port][/instance-name]</code>.

#### environment <a name="propertyenvironment"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ELEMENT</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>No.</td></tr>
</table>

The environment. Typically username/password
credentials.

#### heartbeat <a name="propertyheartbeat"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ATTRIBUTE</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>Not, defaults to 5 seconds.</td></tr>
</table>

The heart beat interval, in milliseconds.

#### name <a name="propertyname"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ATTRIBUTE</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>No.</td></tr>
</table>

A name, can be any text.


### Examples
#### Example 1 <a name="example1"></a>

This example demonstrates reading an attribute, setting an attribute
and invoking an operation.

```xml
<?xml version="1.0" encoding="UTF-8" standalone="no"?>
<oddjob>
    <job>
        <sequential id="sequential">
            <jobs>
                <jmx:service id="jmx-service" 
                  connection="localhost:13013" xmlns:jmx="http://rgordon.co.uk/oddjob/jmx"/>
                <echo id="echo-farm"><![CDATA[${jmx-service/"fruit:service=vendor,name=Pickles".Farm}]]></echo>
                <set>
                    <values>
                        <value key="jmx-service/fruit:service=vendor,name=Pickles.Rating" value="4.2"/>
                    </values>
                </set>
                <invoke id="invoke-quote" function="quote">
                    <parameters>
                        <value value="apples"/>
                        <value value="2012-08-06"/>
                        <value value="42"/>
                    </parameters>
                    <source>
                        <value value="${jmx-service/fruit:service=vendor,name=Pickles}"/>
                    </source>
                </invoke>
            </jobs>
        </sequential>
    </job>
</oddjob>

```



-----------------------

<div style='font-size: smaller; text-align: center;'>(c) R Gordon Ltd 2005 - Present</div>
