[HOME](../../../../README.md)
# bus:driver

Drives data from  an iterable (such as a [list](../../../../org/oddjob/arooa/types/ListType.md))
through a Bean Bus. It can also be used outside Bean Bus to push data to any `java.util.function.Consumer`.

### Property Summary

| Property | Description |
| -------- | ----------- |
| [count](#propertycount) |  | 
| [name](#propertyname) |  | 
| [to](#propertyto) |  | 
| [values](#propertyvalues) |  | 


### Example Summary

| Title | Description |
| ----- | ----------- |
| [Example 1](#example1) | Drive 3 Beans through a Bean Bus. |


### Property Detail
#### count <a name="propertycount"></a>

<table style='font-size:smaller'>
      <tr><td><i>Access</i></td><td>READ_ONLY</td></tr>
</table>



#### name <a name="propertyname"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ATTRIBUTE</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
</table>



#### to <a name="propertyto"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ELEMENT</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
</table>



#### values <a name="propertyvalues"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ELEMENT</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
</table>




### Examples
#### Example 1 <a name="example1"></a>

Drive 3 Beans through a Bean Bus.

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
