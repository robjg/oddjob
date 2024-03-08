[HOME](../../../../README.md)
# identify

Register a value with an Id.


Unlike components, values can't have an Id. This type allows
values to be registered so they can
be referenced via the given Id elsewhere in the configuration.


Components are registered when the configuration is parsed
but the given value will only be registered during the configuration
phase, such as when a job runs in Oddjob.



### Property Summary

| Property | Description |
| -------- | ----------- |
| [id](#propertyid) | The id to register the value with. | 
| [value](#propertyvalue) | The value to register. | 


### Example Summary

| Title | Description |
| ----- | ----------- |
| [Example 1](#example1) | Register a value. |


### Property Detail
#### id <a name="propertyid"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ATTRIBUTE</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>Yes.</td></tr>
</table>

The id to register the value with.

#### value <a name="propertyvalue"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ELEMENT</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>No but pointless if missing.</td></tr>
</table>

The value to register.


### Examples
#### Example 1 <a name="example1"></a>

Register a value.

```xml
<oddjob>
    <job>
        <sequential>
            <jobs>
                <variables id="vars">
                    <test>
                        <identify id="our-value">
                            <value>
                                <value value="Apples"/>
                            </value>
                        </identify>
                    </test>
                </variables>
                <echo>Checking ${vars.test} is ${our-value}</echo>
                <check value="${vars.test}" eq="${our-value}"/>
            </jobs>
        </sequential>
    </job>
</oddjob>
```



-----------------------

<div style='font-size: smaller; text-align: center;'>(c) R Gordon Ltd 2005 - Present</div>
