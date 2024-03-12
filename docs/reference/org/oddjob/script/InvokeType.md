[HOME](../../../README.md)
# invoke

Invoke a java method or script snippet,
or JMX operation.


For a script, the source must be a <code>javax.script.Invocable</code>
object.



### Property Summary

| Property | Description |
| -------- | ----------- |
| [args](#propertyargs) | An alternative configuration for the values to use as arguments. | 
| [function](#propertyfunction) | The function/method/operation name to call. | 
| [parameters](#propertyparameters) | The values to use as arguments. | 
| [source](#propertysource) | The java object or script Invocable on which to invoke the method/function. | 


### Example Summary

| Title | Description |
| ----- | ----------- |
| [Example 1](#example1) | Invoke a method on a bean. |
| [Example 2](#example2) | Invoke a static method of a class. |
| [Example 3](#example3) | Invoking a function of a script. |


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
the word static (see examples).

#### parameters <a name="propertyparameters"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ELEMENT</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>Must match the expected arguments.</td></tr>
</table>

The values to use as arguments. Note that the
<code>args</code> property may be more convenient for simple arguments.

#### source <a name="propertysource"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ELEMENT</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>Yes.</td></tr>
</table>

The java object or script Invocable on
which to invoke the method/function. If the method is a Java static
method then this is the class on which to invoke the method.


### Examples
#### Example 1 <a name="example1"></a>

Invoke a method on a bean. The method takes a single date parameter which
is uses to generate a time of day dependent greeting.

```xml
<oddjob>
    <job>
        <sequential>
            <jobs>
                <variables id='vars'>
                    <message>
                        <invoke function="greeting">
                            <source>
                                <bean
                                    class='org.oddjob.script.GreetingService'/>
                            </source>
                            <parameters>
                                <schedule>
                                    <date>
                                        <value value='${date}'/>
                                    </date>
                                    <schedule>
                                        <schedules:now
                                            xmlns:schedules="http://rgordon.co.uk/oddjob/schedules"/>
                                    </schedule>
                                </schedule>
                            </parameters>
                        </invoke>
                    </message>
                </variables>
                <echo id="echo-greeting">${vars.message}</echo>
            </jobs>
        </sequential>
    </job>
</oddjob>
```


The ${date} reference is there so that it can be injected
during a test, to get a guaranteed result. When this is example
is run as is, this is null so the system clock to be used
there by giving a real time based greeting.


One subtle point to note about Oddjob configuration that this example
highlights is to do with when types are resolved.
The `invoke` type will be resolved when the
`echo` job is run. The `schedule` type will be resolved when the
`variables` job is
run. If the `echo` job were scheduled to run several hours after
the `variables` job had run it would not give the correct greeting!

#### Example 2 <a name="example2"></a>

Invoke a static method of a class.

```xml
<oddjob>
    <job>
        <sequential>
            <jobs>
                <variables id='vars'>
                    <message>
                        <invoke function="static greetPerson">
                            <source>
                                <class name='org.oddjob.script.GreetingService'/>
                            </source>
                            <parameters>
                                <value value="John"/>
                            </parameters>
                        </invoke>
                    </message>
                </variables>
                <echo id="echo-greeting">${vars.message}</echo>
            </jobs>
        </sequential>
    </job>
</oddjob>
```


#### Example 3 <a name="example3"></a>

Invoking a function of a script.

See the [script](../../../org/oddjob/script/ScriptJob.md) examples.


-----------------------

<div style='font-size: smaller; text-align: center;'>(c) R Gordon Ltd 2005 - Present</div>
