[HOME](../../../../README.md)
# state:watch

Evaluate a state expression that becomes an event source for triggering other jobs.
used with [events:trigger](../../../../org/oddjob/events/Trigger.md) and [events:when](../../../../org/oddjob/events/When.md).


Expressions are of the form:


<pre>
[NOT] job-id IS job-condition
</pre>


And can be chained with AND and OR and nested with parenthesis. Examples are:


<pre>
job1 is success
not job1 is success
job1 is success or ( job2 is success and job3 is success)
</pre>

### Property Summary

| Property | Description |
| -------- | ----------- |
| [expression](#propertyexpression) | The state expression to evaluate. | 


### Example Summary

| Title | Description |
| ----- | ----------- |
| [Example 1](#example1) | Runs a job when two other jobs complete, but only if one of the jobs hasn't been run. |


### Property Detail
#### expression <a name="propertyexpression"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>TEXT</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>Yes.</td></tr>
</table>

The state expression to evaluate.


### Examples
#### Example 1 <a name="example1"></a>

Runs a job when two other jobs complete, but only if one of the jobs hasn't been run.

```xml
<?xml version="1.0" encoding="UTF-8" standalone="no"?>
<oddjob>
    <job>
        <sequential>
            <jobs>
                <events:when id="when" xmlns:events="oddjob:events">
                    <jobs>
                        <events:watch name="Watch Jobs 1 and 2">
                            <eventSource>
                                <state:watch xmlns:state="http://rgordon.co.uk/oddjob/state">
                                    <![CDATA[job1 is COMPLETE 
AND 
job2 is COMPLETE
]]>
                                </state:watch>
                            </eventSource>
                        </events:watch>
                        <state:if xmlns:state="http://rgordon.co.uk/oddjob/state">
                            <jobs>
                                <sequential>
                                    <jobs>
                                        <state:evaluate id="check-job3" name="Is Job 3 Complete">
                                            <![CDATA[job3 is complete]]>
                                        </state:evaluate>
                                        <check gt="${check-job3.evaluation.time}" value="${when.trigger.time}"/>
                                    </jobs>
                                </sequential>
                                <state:flag/>
                                <run job="${job3}" name="Run Job 3"/>
                            </jobs>
                        </state:if>
                    </jobs>
                </events:when>
                <folder>
                    <jobs>
                        <echo id="job1" name="Job 1">
                            <![CDATA[Hello]]>
                        </echo>
                        <echo id="job2" name="Job 2">
                            <![CDATA[World]]>
                        </echo>
                        <echo id="job3" name="Job 3">
                            <![CDATA[It's Done!]]>
                        </echo>
                    </jobs>
                </folder>
            </jobs>
        </sequential>
    </job>
</oddjob>

```



-----------------------

<div style='font-size: smaller; text-align: center;'>(c) R Gordon Ltd 2005 - Present</div>
