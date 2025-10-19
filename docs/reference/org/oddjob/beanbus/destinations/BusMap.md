[HOME](../../../../README.md)
# bus:map

Apply a [java.util.function.Function](https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/util/function/Function.html) to beans in a Bean Bus. If the result of the function. If the
function returns null then nothing is passed to the next component so this has the same effect as
an [bus:filter](../../../../org/oddjob/beanbus/destinations/BeanFilter.md)

### Property Summary

| Property | Description |
| -------- | ----------- |
| [count](#propertycount) | The number of items the function has been applied to. | 
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
      <tr><td><i>Required</i></td><td>Read Only.</td></tr>
</table>

The number of items the function has been applied to.

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
                <bus:driver>
                    <values>
                        <list>
                            <values>
                                <value value="#{25.5}"/>
                                <value value="#{36.2}"/>
                                <value value="#{40.4}"/>
                            </values>
                        </list>
                    </values>
                </bus:driver>
                <bus:map>
                    <function>
                        <value value="#{ function(x) { return x * 2 } }"/>
                    </function>
                </bus:map>
                <bus:collect id="results"/>
            </of>
        </bus:bus>
    </job>
</oddjob>
```



-----------------------

<div style='font-size: smaller; text-align: center;'>(c) R Gordon Ltd 2005 - Present</div>
