[HOME](../../../README.md)
# url-class-loader

A simple wrapper for URLClassloader.


The class loader is created when this type is configured, and the same
class loader is then shared with all jobs that reference this type.
Because creating numerous class loader can use up the permgen heap space
it is best to avoid creating the type in a loop. Instead, add it to
[variables](../../../org/oddjob/values/VariablesJob.md) outside the loop and only reference it inside the
loop.


The parent class loader will be set to Oddjob's classloader by default. To avoid this use the `noInherit`
property.



### Property Summary

| Property | Description |
| -------- | ----------- |
| [files](#propertyfiles) | Files to add to the classpath. | 
| [noInherit](#propertynoInherit) | Don't inherit the parent class loader. | 
| [parent](#propertyparent) | The parent class loader to inherit. | 
| [urls](#propertyurls) | URLs to add to the classpath. | 


### Example Summary

| Title | Description |
| ----- | ----------- |
| [Example 1](#example1) | A simple example. |
| [Example 2](#example2) | Forcing the platform class loader as a parent. |


### Property Detail
#### files <a name="propertyfiles"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ELEMENT</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>No.</td></tr>
</table>

Files to add to the classpath.

#### noInherit <a name="propertynoInherit"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ATTRIBUTE</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>No.</td></tr>
</table>

Don't inherit the parent class loader.

#### parent <a name="propertyparent"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ELEMENT</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>No, defaults to any existing Oddjob 
 class loader.</td></tr>
</table>

The parent class loader to inherit.

#### urls <a name="propertyurls"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ELEMENT</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>No.</td></tr>
</table>

URLs to add to the classpath.


### Examples
#### Example 1 <a name="example1"></a>

A simple example. A single directory is added to the class path.


```xml
<oddjob id="oddjob">
    <job>
        <oddjob id='nested' file="${oddjob.dir}/URLClassLoaderInner.xml">
            <classLoader>
                <url-class-loader>
                    <files>
                        <file file='${oddjob.args[0]}/test/classloader'/>
                    </files>
                </url-class-loader>
            </classLoader>
        </oddjob>
    </job>
</oddjob>
```




#### Example 2 <a name="example2"></a>

Forcing the platform class loader as a parent. When migrating to Java 9 classes such as `java.sql.date`
will no longer load with the no parent class loader. See
<a href="https://docs.oracle.com/javase/9/migrate/toc.htm#JSMIG-GUID-D867DCCC-CEB5-4AFA-9D11-9C62B7A3FAB1">
Migrating to JDK 9</a> for more information.


-----------------------

<div style='font-size: smaller; text-align: center;'>(c) R Gordon Ltd 2005 - Present</div>
