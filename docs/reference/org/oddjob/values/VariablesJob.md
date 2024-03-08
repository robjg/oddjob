[HOME](../../../README.md)
# variables

This job provides a 'variables'
like declaration within Oddjob.


The variables job is like a bean where any property can be set
with any value.


Because names are properties, they can only be valid simple property
names. 'java.version' is not valid simple property because it is
interpreted as a value 'java' that has a property 'version'. To allow
these type of properties to be referenced in Oddjob use
[properties](../../../org/oddjob/values/properties/PropertiesJob.md).

### Example Summary

| Title | Description |
| ----- | ----------- |
| [Example 1](#example1) | A simple variable. |


### Examples
#### Example 1 <a name="example1"></a>

A simple variable.

```xml
<oddjob>
    <job>
        <sequential>
            <jobs>
                <variables id="vars">
                    <greeting>
                        <value value="Hello World"/>
                    </greeting>
                </variables>
                <echo name="Echo a Greeting">${vars.greeting}</echo>
            </jobs>
        </sequential>
    </job>
</oddjob>

```



-----------------------

<div style='font-size: smaller; text-align: center;'>(c) R Gordon Ltd 2005 - Present</div>
