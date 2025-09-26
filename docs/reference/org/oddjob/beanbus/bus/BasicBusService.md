[HOME](../../../../README.md)
# bus:bus

Links components in a data pipeline. Components
provide data by accepting an [java.util.function.Consumer](https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/util/function/Consumer.html) either by
being an [org.oddjob.beanbus.Outbound](http://rgordon.co.uk/oddjob/1.6.0/api/org/oddjob/beanbus/Outbound.html) or by marking a setter with the [org.oddjob.beanbus.Destination](http://rgordon.co.uk/oddjob/1.6.0/api/org/oddjob/beanbus/Destination.html)
annotation. Components accept data by being a [java.util.function.Consumer](https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/util/function/Consumer.html). Components
can be both.


This component parent provides the following features over other component parents
such as [sequential](../../../../org/oddjob/jobs/structural/SequentialJob.md):

- Components will be automatically linked to the next component unless this is disabled.
- Any plain [java.util.function.Consumer](https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/util/function/Consumer.html) will appear in the bus as a service with appropriate icons and state.
- Components will be run (or started) in reverse order so destinations are ready to receive data before it is sent by the previous components.
- Components will be stopped in order so components that send data are stopped before the destinations that receive the data.
- If a component has a property setter of type [java.lang.AutoCloseable](https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/lang/AutoCloseable.html) then one will be set automatically allowing the component to stop the bus.
- If a component has a property setter of type [java.io.Flushable](https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/io/Flushable.html) then one will be set automatically allowing the component to flush the bus.
- Any component that is [java.io.Flushable](https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/io/Flushable.html) will be flushed when a component flushes the bus. Flush will be called in component order. Flush will always be called when the bus stops, unless it crashes.
- If a component wishes to both stop and flush the bus, and doesn't mind a dependency on this framework it can provide a property setter of type [org.oddjob.beanbus.BusConductor](http://rgordon.co.uk/oddjob/1.6.0/api/org/oddjob/beanbus/BusConductor.html) and one will be set automatically
- If a component enters an Exception state the bus will crash. Other components will be stopped in order.




### Property Summary

| Property | Description |
| -------- | ----------- |
| [busConductor](#propertybusconductor) | Provides coordination facilities to the components of a bus. | 
| [count](#propertycount) |  | 
| [name](#propertyname) | A name, can be any text. | 
| [noAutoLink](#propertynoautolink) | Bus components will automatically be linked unless this is true. | 
| [of](#propertyof) | The components of a Bus. | 
| [services](#propertyservices) | Provides services to other components of a bus. | 
| [stop](#propertystop) | Read only view of the internal stop flag. | 
| [to](#propertyto) | An onward consumer so that bus services may be nested. | 


### Example Summary

| Title | Description |
| ----- | ----------- |
| [Example 1](#example1) | A simple bus of 3 components. |
| [Example 2](#example2) | Shows how a bus can be nested to create side branches. |


### Property Detail
#### busConductor <a name="propertybusconductor"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ELEMENT</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>No.</td></tr>
</table>

Provides coordination facilities to the components of a bus. Set automatically
and exposed for advance use only.

#### count <a name="propertycount"></a>

<table style='font-size:smaller'>
      <tr><td><i>Access</i></td><td>READ_ONLY</td></tr>
</table>



#### name <a name="propertyname"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ATTRIBUTE</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>No.</td></tr>
</table>

A name, can be any text.

#### noAutoLink <a name="propertynoautolink"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ATTRIBUTE</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>No, defaults to false.</td></tr>
</table>

Bus components will automatically be linked unless this is true.

#### of <a name="propertyof"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ELEMENT</td></tr>
      <tr><td><i>Access</i></td><td>WRITE_ONLY</td></tr>
      <tr><td><i>Required</i></td><td>No, but pointless if missing.</td></tr>
</table>

The components of a Bus.

#### services <a name="propertyservices"></a>

<table style='font-size:smaller'>
      <tr><td><i>Access</i></td><td>READ_ONLY</td></tr>
      <tr><td><i>Required</i></td><td>Read Only.</td></tr>
</table>

Provides services to other components of a bus. Exposed for advance use only.

#### stop <a name="propertystop"></a>

<table style='font-size:smaller'>
      <tr><td><i>Access</i></td><td>READ_ONLY</td></tr>
</table>

Read only view of the internal stop flag.
This flag is cleared with a reset.

#### to <a name="propertyto"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ELEMENT</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>No.</td></tr>
</table>

An onward consumer so that bus services may be nested.


### Examples
#### Example 1 <a name="example1"></a>

A simple bus of 3 components. The first component is the bus driver that sends 3 beans down the pipe.
The second component is a function that doubles the price and the last component collects the
results.


```xml
<?xml version="1.0" encoding="UTF-8" standalone="no"?>
<oddjob>
    <job>
        <bus:bus id="bean-bus" xmlns:bus="oddjob:beanbus">
            <of>
                <bus:driver xmlns:bus="oddjob:beanbus">
                    <values>
                        <list>
                            <values>
                                <bean class="org.oddjob.beanbus.example.Fruit" quantity="42" type="Apple" price="25.5"/>
                                <bean class="org.oddjob.beanbus.example.Fruit" quantity="24" type="Banana" price="36.2"/>
                                <bean class="org.oddjob.beanbus.example.Fruit" quantity="15" type="Pear" price="40.4"/>
                            </values>
                        </list>
                    </values>
                </bus:driver>
                <bus:map xmlns:bus="oddjob:beanbus">
                    <function>
                        <bean class="org.oddjob.beanbus.example.DoublePrice"/>
                    </function>
                </bus:map>
                <bus:collect id="results" xmlns:bus="oddjob:beanbus"/>
            </of>
        </bus:bus>
    </job>
</oddjob>
```


#### Example 2 <a name="example2"></a>

Shows how a bus can be nested to create side branches. The data is passed to each branch in turn.


```xml
<?xml version="1.0" encoding="UTF-8" standalone="no"?>
<oddjob>
    <job>
        <cascade>
            <jobs>
                <bus:bus id="bus" xmlns:bus="oddjob:beanbus">
                    <of>
                        <bus:driver>
                            <values>
                                <list>
                                    <values>
                                        <value value="red"/>
                                        <value value="red"/>
                                        <value value="blue"/>
                                        <value value="green"/>
                                    </values>
                                </list>
                            </values>
                        </bus:driver>
                        <bus:bus>
                            <of>
                                <bus:filter id="filterRed">
                                    <predicate>
                                        <value value="#{ function(x) { return 'red' == x }}"/>
                                    </predicate>
                                </bus:filter>
                            </of>
                        </bus:bus>
                        <bus:bus>
                            <of>
                                <bus:filter id="filterBlue">
                                    <predicate>
                                        <value value="#{ function(x) { return 'blue' == x }}"/>
                                    </predicate>
                                </bus:filter>
                            </of>
                        </bus:bus>
                    </of>
                </bus:bus>
                <check value="${filterRed.passed}" eq="2"/>
                <check value="${filterBlue.passed}" eq="1"/>
            </jobs>
        </cascade>
    </job>
</oddjob>
```



-----------------------

<div style='font-size: smaller; text-align: center;'>(c) R Gordon Ltd 2005 - Present</div>
