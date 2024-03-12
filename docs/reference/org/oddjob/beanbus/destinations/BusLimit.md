[HOME](../../../../README.md)
# bus:limit

Only allow a certain number of beans passed. When the limit is reached the Bus will
be Stopped. Any beans arriving while the bus is stopping will be ignored.

### Property Summary

| Property | Description |
| -------- | ----------- |
| [count](#propertycount) | The number so far. | 
| [limit](#propertylimit) | The limit. | 
| [name](#propertyname) | The name of this component. | 
| [to](#propertyto) | The next component in a bus. | 


### Example Summary

| Title | Description |
| ----- | ----------- |
| [Example 1](#example1) | Limit the Bus to 2 beans. |


### Property Detail
#### count <a name="propertycount"></a>

<table style='font-size:smaller'>
      <tr><td><i>Access</i></td><td>READ_ONLY</td></tr>
      <tr><td><i>Required</i></td><td>Read only.</td></tr>
</table>

The number so far.

#### limit <a name="propertylimit"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ATTRIBUTE</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>No, defaults to 0.</td></tr>
</table>

The limit.

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
      <tr><td><i>Access</i></td><td>WRITE_ONLY</td></tr>
      <tr><td><i>Required</i></td><td>No.</td></tr>
</table>

The next component in a bus. Set automatically in a
[bus:bus](../../../../org/oddjob/beanbus/bus/BasicBusService.md).


### Examples
#### Example 1 <a name="example1"></a>

Limit the Bus to 2 beans.

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
                                <value value="Apple"/>
                                <value value="Orange"/>
                                <value value="Banana"/>
                            </values>
                        </list>
                    </values>
                </bus:driver>
                <bus:collect id="before" name="Beans Before"  xmlns:bus="oddjob:beanbus"/>
                <bus:limit id="only-filter" limit="2" xmlns:bus="oddjob:beanbus"/>
                <bus:collect id="results" name="Beans After" xmlns:bus="oddjob:beanbus"/>
            </of>
        </bus:bus>
    </job>
</oddjob>
```



-----------------------

<div style='font-size: smaller; text-align: center;'>(c) R Gordon Ltd 2005 - Present</div>
