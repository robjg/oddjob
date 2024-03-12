[HOME](../../../../README.md)
# bus:map

Apply a [java.util.function.Function](http://rgordon.co.uk/oddjob/1.6.0/api/java/util/function/Function.html) to beans in a Bean Bus.

### Property Summary

| Property | Description |
| -------- | ----------- |
| [count](#propertycount) |  | 
| [function](#propertyfunction) | The function to apply to beans on the bus. | 
| [name](#propertyname) | The name of this component. | 
| [to](#propertyto) | The next component in a bus. | 


### Example Summary

| Title | Description |
| ----- | ----------- |
| [Example 1](#example1) | Apply a function to double the price on a `Fruit` bean. |


### Property Detail
#### count <a name="propertycount"></a>

<table style='font-size:smaller'>
      <tr><td><i>Access</i></td><td>READ_ONLY</td></tr>
</table>



#### function <a name="propertyfunction"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ELEMENT</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>Yes.</td></tr>
</table>

The function to apply to beans on the bus.

#### name <a name="propertyname"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ATTRIBUTE</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>No.</td></tr>
</table>

The name of this component.

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

Apply a function to double the price on a `Fruit` bean.

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



-----------------------

<div style='font-size: smaller; text-align: center;'>(c) R Gordon Ltd 2005 - Present</div>
