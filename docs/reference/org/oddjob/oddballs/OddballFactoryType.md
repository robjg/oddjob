[HOME](../../../README.md)
# oddball

Create an Oddball from various sources. Primarily intended to be used
with the h href="https://github.com/robjg/oj-resolve>oj-resolve</h> project to load a
maven dependency as an Oddball. See [oddballs](../../../org/oddjob/oddballs/OddballsDescriptorFactory.md).

### Property Summary

| Property | Description |
| -------- | ----------- |
| [paths](#propertypaths) | Paths to create an Oddball from. | 
| [urls](#propertyurls) | URLs to create an Oddball from. | 


### Example Summary

| Title | Description |
| ----- | ----------- |
| [Example 1](#example1) | Loading two Oddballs. |


### Property Detail
#### paths <a name="propertypaths"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ELEMENT</td></tr>
      <tr><td><i>Access</i></td><td>WRITE_ONLY</td></tr>
      <tr><td><i>Required</i></td><td>Either this or URLs is required.</td></tr>
</table>

Paths to create an Oddball from.

#### urls <a name="propertyurls"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ELEMENT</td></tr>
      <tr><td><i>Access</i></td><td>WRITE_ONLY</td></tr>
      <tr><td><i>Required</i></td><td>Either this or Paths is required.</td></tr>
</table>

URLs to create an Oddball from.


### Examples
#### Example 1 <a name="example1"></a>

Loading two Oddballs.
```xml
<oddjob id="this">
    <job>
        <oddjob file="${this.args[0]}/test/launch/oddballs-launch.xml">
            <descriptorFactory>
                <oddballs>
                    <oddballs>
                        <oddball>
                            <paths>
                                <file file="${this.args[0]}/test/oddballs/apple/classes"/>
                            </paths>
                        </oddball>
                        <oddball>
                            <paths>
                                <file file="${this.args[0]}/test/oddballs/orange/classes"/>
                            </paths>
                        </oddball>
                    </oddballs>
                </oddballs>
            </descriptorFactory>
        </oddjob>
    </job>
</oddjob>
```



-----------------------

<div style='font-size: smaller; text-align: center;'>(c) R Gordon Ltd 2005 - Present</div>
