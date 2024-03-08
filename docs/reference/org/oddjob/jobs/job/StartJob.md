[HOME](../../../../README.md)
# start

This job will run another job. It is intended
for starting services or jobs on remote servers which is why it is
named start. If it used on a local job it will block until the local
job has run.



Unlike the [run](../../../../org/oddjob/jobs/job/RunJob.md), this job will not monitor or reflect the
state of the started job. To monitor the state of the started job the
job could be followed by a [state:mirror](../../../../org/oddjob/state/MirrorState.md).


The start job won't reset the job to be started. If the job to start
isn't started because it's in the wrong state this job will still
COMPLETE. This job can be preceded by a [reset](../../../../org/oddjob/jobs/job/ResetJob.md) if resetting
is required.

### Property Summary

| Property | Description |
| -------- | ----------- |
| [job](#propertyjob) | The job to start | 
| [name](#propertyname) | A name, can be any text. | 
| [stop](#propertystop) | This flag is set by the stop method and should be examined by any Stoppable jobs in their processing loops. | 


### Example Summary

| Title | Description |
| ----- | ----------- |
| [Example 1](#example1) | Starting a service. |


### Property Detail
#### job <a name="propertyjob"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ATTRIBUTE</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>Yes.</td></tr>
</table>

The job to start

#### name <a name="propertyname"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ATTRIBUTE</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>No.</td></tr>
</table>

A name, can be any text.

#### stop <a name="propertystop"></a>

<table style='font-size:smaller'>
      <tr><td><i>Access</i></td><td>READ_ONLY</td></tr>
      <tr><td><i>Required</i></td><td>Read Only.</td></tr>
</table>

This flag is set by the stop method and should
be examined by any Stoppable jobs in their processing loops.


### Examples
#### Example 1 <a name="example1"></a>

Starting a service. A folder contains a choice of services. The service
id to use is provided at runtime with a property such as
-DpriceService=nonCachingPriceService. The selected service is started
and used by the Pricing Job.

```xml
<oddjob>
  <job>
    <sequential>
      <jobs>
        <folder name="services">
          <jobs>
            <bean id="cachingPriceService" 
                class="org.oddjob.examples.CachingPriceService" />
            <bean id="nonCachingPriceService" 
                class="org.oddjob.examples.NonCachingPriceService" />
          </jobs>
        </folder>
        <start job="${${priceService}}"/>
        <bean id="pricingJob" class="org.oddjob.examples.PricingJob"
          priceService="${${priceService}}"/>
      </jobs>
    </sequential>
  </job>
</oddjob>
```



-----------------------

<div style='font-size: smaller; text-align: center;'>(c) R Gordon Ltd 2005 - Present</div>
