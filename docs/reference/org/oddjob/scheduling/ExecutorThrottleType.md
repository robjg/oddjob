[HOME](../../../README.md)
# throttle

Throttle parallel execution. This will limit the
number of jobs running in parallel.



### Property Summary

| Property | Description |
| -------- | ----------- |
| [limit](#propertylimit) | The maximum number of simultaneous jobs this throttle will allow. | 
| [original](#propertyoriginal) | The `ExecuutorService` to throttle. | 


### Example Summary

| Title | Description |
| ----- | ----------- |
| [Example 1](#example1) | Throttling parallel execution. |
| [Example 2](#example2) | Sharing a throttle. |


### Property Detail
#### limit <a name="propertylimit"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ATTRIBUTE</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>Yes.</td></tr>
</table>

The maximum number of simultaneous jobs this
throttle will allow.

#### original <a name="propertyoriginal"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ELEMENT</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>No.</td></tr>
</table>

The `ExecuutorService` to throttle. This
will be automatically set by Oddjob.


### Examples
#### Example 1 <a name="example1"></a>

Throttling parallel execution.

```xml
<?xml version="1.0" encoding="UTF-8" standalone="no"?>
<oddjob>
    <job>
        <parallel id="parallel">
            <executorService>
                <throttle limit="2"/>
            </executorService>
            <jobs>
                <wait name="Wait 1"/>
                <wait name="Wait 2"/>
                <wait name="Wait 3"/>
                <wait name="Wait 4"/>
            </jobs>
        </parallel>
    </job>
</oddjob>
```


#### Example 2 <a name="example2"></a>

Sharing a throttle. The same throttle is shared between
to [parallel](../../../org/oddjob/jobs/structural/ParallelJob.md) jobs. The total number of jobs executing between
both parallels is 2.

```xml
<?xml version="1.0" encoding="UTF-8" standalone="no"?>
<oddjob>
    <job>
        <sequential>
            <jobs>
                <variables id="vars">
                    <throttle>
                        <convert>
                          <value>
                            <throttle limit="2"/>
                          </value>
                        </convert>
                    </throttle>
                </variables>
                <parallel id="parallel-1">
                    <executorService>
                        <value value="${vars.throttle.is}"/>
                    </executorService>
                    <jobs>
                        <wait name="Wait 1"/>
                        <wait name="Wait 2"/>
                    </jobs>
                </parallel>
                <parallel id="parallel-2">
                    <executorService>
                        <value value="${vars.throttle.is}"/>
                    </executorService>
                    <jobs>
                        <wait name="Wait 3"/>
                        <wait name="Wait 4"/>
                    </jobs>
                </parallel>
            </jobs>
        </sequential>
    </job>
</oddjob>
```


The throttle type is a factory type and so would provide a new instance
each time it's used. To overcome this the throttle is
wrapped in a convert [convert](../../../org/oddjob/arooa/types/ConvertType.md) that creates a single instance.


-----------------------

<div style='font-size: smaller; text-align: center;'>(c) R Gordon Ltd 2005 - Present</div>
