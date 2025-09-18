[HOME](../../../README.md)
# oddballs

Create Oddjob job definition descriptors from any
number of directories that follow the Oddball format.


The Oddball directory structure is:
<code><pre>
myball
classes
com
acme
MyStuff.class
lib
someutil.jar
</pre></code>
You can have either a <code>lib</code> or <code>classes</code> or both,
but you must have something.


Additionally there can be as many <code>META-INF/arooa.xml</code> resources
on that confirm to the [arooa:descriptor](../../../org/oddjob/arooa/deploy/ArooaDescriptorBean.md) format for defining
element mappings and conversions.

### Property Summary

| Property | Description |
| -------- | ----------- |
| [files](#propertyfiles) | The Oddball directory or directories. | 
| [oddballs](#propertyoddballs) | Other factories for creating Oddballs. | 


### Example Summary

| Title | Description |
| ----- | ----------- |
| [Example 1](#example1) | Loading two Oddballs. |


### Property Detail
#### files <a name="propertyfiles"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ELEMENT</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>Yes.</td></tr>
</table>

The Oddball directory or directories.

#### oddballs <a name="propertyoddballs"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ELEMENT</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>No. Defaults to a directory loading factory.</td></tr>
</table>

Other factories for creating Oddballs. See [oddball](../../../org/oddjob/oddballs/OddballFactoryType.md).
In the future, it is hoped to support loading Oddballs from archives. Following the
existing java naming convention for archives they will probably be
called .oar files.


### Examples
#### Example 1 <a name="example1"></a>

Loading two Oddballs.
```xml
<oddjob id="this">
<job>
    <oddjob file="${this.args[0]}/test/launch/oddballs-launch.xml">
        <descriptorFactory>
            <oddballs>
                <files>
                    <files>
                        <list>
                            <file file="${this.args[0]}/test/oddballs/apple"/>
                            <file file="${this.args[0]}/test/oddballs/orange"/>
                        </list>
                    </files>
                </files>
            </oddballs>
        </descriptorFactory>
    </oddjob>
</job>
</oddjob>
```


This is equivalent to launching Oddjob with the oddball path option
set as in:
<code><pre>
java -jar run-oddjob.jar \
-op test/oddballs/apple:test/oddballs/orange \
-f test/launch/oddballs-launch.xml
</pre></code>
Or if the <code>test/oddballs</code> directory only contains these two
directories, then using the oddball directory option:
<code><pre>
java -jar run-oddjob.jar \
-ob test/oddballs \
-f test/launch/oddballs-launch.xml
</pre></code>
If the <code>apple</code> and <code>orange</code> directories were
copied to Oddjob's Oddballs directory they would be loaded by default.


-----------------------

<div style='font-size: smaller; text-align: center;'>(c) R Gordon Ltd 2005 - Present</div>
