[HOME](../../../../README.md)
# bus:filter

Filter out beans passing through the bus according to an `java.util.function.Predicate`.

### Property Summary

| Property | Description |
| -------- | ----------- |
| [blocked](#propertyblocked) | Number of items blocked by the predicate. | 
| [name](#propertyname) | The name of this component. | 
| [passed](#propertypassed) | Number of items passing the predicate. | 
| [predicate](#propertypredicate) | The predicate. | 
| [to](#propertyto) | The next component in a bus. | 


### Example Summary

| Title | Description |
| ----- | ----------- |
| [Example 1](#example1) | Filter out Bananas. |


### Property Detail
#### blocked <a name="propertyblocked"></a>

<table style='font-size:smaller'>
      <tr><td><i>Access</i></td><td>READ_ONLY</td></tr>
      <tr><td><i>Required</i></td><td>Read only.</td></tr>
</table>

Number of items blocked by the predicate.

#### name <a name="propertyname"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ATTRIBUTE</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>No.</td></tr>
</table>

The name of this component.

#### passed <a name="propertypassed"></a>

<table style='font-size:smaller'>
      <tr><td><i>Access</i></td><td>READ_ONLY</td></tr>
      <tr><td><i>Required</i></td><td>Read only.</td></tr>
</table>

Number of items passing the predicate.

#### predicate <a name="propertypredicate"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ELEMENT</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>Yes.</td></tr>
</table>

The predicate.

#### to <a name="propertyto"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ELEMENT</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>No.</td></tr>
</table>

The next component in a bus. Set automatically in a
[bus:bus](../../../../org/oddjob/beanbus/bus/BasicBusService.md).


### Examples
#### Example 1 <a name="example1"></a>

Filter out Bananas.

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
                <bus:filter xmlns:bus="oddjob:beanbus">
                    <predicate>
                        <bean class="org.oddjob.beanbus.example.RemoveFruit" remove="Banana"/>
                    </predicate>
                </bus:filter>
                <bus:collect id="results" xmlns:bus="oddjob:beanbus"/>
            </of>
        </bus:bus>
    </job>
</oddjob>
```



-----------------------

<div style='font-size: smaller; text-align: center;'>(c) R Gordon Ltd 2005 - Present</div>
