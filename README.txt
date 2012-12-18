Oddjob-1.3.0
============

Changes in 1.3.0
----------------
- Parallel and Sequential have been given a stateOperator property that
  allows how they interpret their child states to vary. The SERVICES 
  state operator does what serviceman used to do and so it has been removed.
- Parallel has been given a join property that restores the pre Version 1.0
  behaviour of waiting for the parallel threads to complete before 
  continuing. Note that it's use is discouraged!
- A new Parent State of STARTED has been introduced to better reflect
  child service states. Timer and Retry now uses STARTED instead of ACTIVE.
- Cascade now works correctly with the STARTED state of services.
- Timer and Retry now allow the nextDue property to be set while the timer 
  is running.
- New Swing Panel is available that provides Oddjob jobs as simple Buttons.
  It is not yet an Oddjob element but can be added with 
  <bean class='org.oddjob.swing.OddjobPanel'/>
- Change foreach so that a parallel foreach can have a run window (i.e. 
  preLoad and purgeAfter properties). Also individual foreach jobs may now 
  be configured with designer.
- Developer Guide documentation has been improved especially around 
  using Oddballs.

Still To Do for 1.3.0
---------------------

- Add Parent Started State to User Guide. Document thread.
- Write tests for setting nextDue while timer running.
- Find bug with destroying JMX Client - see Stack Exceptions in 
  TogetherTest#testClientServerLoopback.
- Use a ServerSide file system for choosing files on server component 
  designer forms.
- Allow pasting and dragging Oddjobs onto the startup panel of Oddjob 
  Explorer.
- Introduce a witheach job that behaves like foreach except that values
  are 'pushed' into it.
- Add serialisation of services.
- Document synthetic annotations in the Developer Guide.
- Document conversions. Possibly include conversions in the Reference.

Deferred to A Later Version
---------------------------
- Look at what happens when there are exceptions in a timer. Should there be
  a haltOnException flag. Should an Exception in a timer be indicated as
  an exception state not an incomplete state?
- Should If job work on asynchronous completion of a job?
- Add Security to the WebApp (with a Read Only role).  
- Include a Jetty Oddball to allow connecting to an Oddjob server from a 
  browser without the need for a separate Servlet Container.
- Improve the AJAX JSF front end to be more AJAXy.
- Introduce the idea of Read only configuration that can't be modified if
  it's been loaded from a non modifiable resource, i.e. from the class path.
- Check a configuration hasn't been modified by someone else before a 
  modification is saved back from designer.
- Add Undo functionality to Oddjob configuration.
- Improve the <rename> job. Follow Ant's lead of changing to a <move> job and
  copy some of it's feature including; overwrite, force, failonerror, verbose,
  preservelastmodified. Add the ability to back up the moved files like Linux does.
- Introduce a FilterType that can filter files by modified date, created date,
  or match against a regular expression.
- Need better protection from exceptions in the DetailModel.setSelectedContext
  method.


Changes in 1.2.0
----------------
- Oddjob will now not stop until it's root job is in a stopped state. This
  is because a server with an RMI registry keep's Oddjob alive but
  the newer servers using the PlatformMBean does not. A wait job is
  no longer needed to stop Oddjob shutting down. The new service manager job 
  will allow services to run in as 'daemon' services if required.
- Introduce an OddjobShutdownThread interface so that jobs know they are 
  being stopped from the shutdown thread.
- JMX client URLs can now connect with just hostname:port in the URL. The
  client 'url' property has been renamed 'connection' to reflect that it
  doesn't just have to be an URL.
- Introduced a new jmx:service job that exposes attributes and operations
  of MBeans for getting and setting and invoking by other Oddjob jobs.
- Introduced a new Invoker interface for more general invocations of 
  InvokeType than the previous hard coded java methods and script snippets.
- Introduced an Invoker for MBean operations 
- Provide a new Invoke job that can do everything InvokeType can do including
  invoking MBean operations.
- Trigger has a new cancelWhen property that will cause the trigger to
  complete without executing it's job.
- Introduced new state conditions. FAILURE, FINISHED and EXECUTING (which
  included EXECUTING and ACTIVE states) has become RUNNING and a new 
  EXECUTING state condition has been introduced for just actual EXECUTING 
  states. There is also a new STARTED state condition for services.
- Added a NoDescribe annotation that allows a property to be hidden from
  the properties panel. Required for things like a DataSource that has
  a connection property the shouldn't be described.
- Allow file wild cards in the launch class path.
- Process -D property arguments in the Launcher so they can be 
  set from batch files.
- Support annotations, synthetic or normal, for Oddjob reset methods. 
- Support annotations, synthetic or normal, for ArooaLifeCycle Methods. 
- Added the idea of synthetic annotations to the descriptor. This allows
  a method or property to tagged with a pseudo annotation with the XML 
  descriptor so beans do not need to have a dependency on an Oddjob jar 
  for things such as life cycle events. 
- Added an autocommit flag to SQLJob.
- Added a Service Manager (serviceman) job has been add for managing 
  services.
- The INCOMPLETE state has been removed from the ServiceState enum.
  This state wasn't really meaningful in the context of a service. It existed
  only because historically JMXClientJob could become incomplete when a 
  server is shut down. If differentiating between a shutdown server and a
  lost network connection is a requirement then a property could be added to 
  the client job in future.
- The check job now includes a null flag to check that a property isn't set.
- Changed the designer for a bean from just XML to one that dynamically 
  creates a form for the bean's settable properties.
- Improved properties job to show when properties are overridden, to
  show environment properties, and, if no properties are defined, to show 
  all properties available.
- Created a start job that is the old run job. The run job now shows what
  it is running and reflects it's state. It will also wait until Execution
  has finished so behaviour will be the same running a local job or a
  server job. Depends job is now deprecated.
- Re-wrote the multi value table widget used to design mapped and indexed
  properties and the Variables designer. It now has up down buttons for
  re-ordering (alt-up/alt-down) and is much less buggy.
- Property expansion now supports nested properties of the form ${${}}.
- A file chooser has been added to Input Handlers. 
- Ctrl enter and escape shortcut keys now work in the SwingInputHandler
  dialogue.
- The oj-spring oddball is now a a separate project distributed to 
  https://sourceforge.net/projects/ojob/files/oj-spring
- The file type is now always the canonical file.
- Fixed problem where a remote job in an Exception state sends an exception to 
  the client that isn't in the clients class loader.
- Re-wrote cascade job to cope with changes to children while running and to
  terminate if a child fails.
- Cleanup resources in Exec Job
- Improved the Oddjob export property so beans and references are exported 
  to a nested Oddjob as is, and not wrapped as an ArooaValue. 
- Validate changes in Designer before closing the dialog so changes aren't
  lost if there is a problem.
- Changed Iconic interface to just use ImageIcon instead of a bespoke 
  wrapper class.  

Changes in 1.1.0
----------------
- Fixed a bug where a timer with skipMissedRuns re-ran again when the timer
  was started and stopped before the end of the current scheduled interval.
- Added ability to configure Oddjob from a URL.
- Removed the async property from Stop job. A separate trigger can be used
  instead which is much more explicit.
- Added a 'forceComplete' action that will force a job to be COMPLETE.
- Added support Proxying Callables in Oddjob.
- The ContextClassLoader is now always set correctly before running a job.
- The repeat job has been re-written to no longer support schedules just 
  simple repeats.
- Fixed bug with not registering Out Parameters in SQL Job.
- Moved the Struts Webapp to JSF 2.1 with MyFaces 2.1.4 and JSF AJAX is 
  now used to update just the main sections of the web view.
- The CSS for the Webapp has been improved.
- Fixed bug whereby explorer wouldn't close when Oddjob is killed with 
  Ctrl-C.

Changes in 1.0.0
----------------
- A new append type has been introduced that allows a file to be appended to.
- Corrected millisecond date format to be yyyy-MM-dd HH:mm:ss.SSS (decimal 
  point not colon).
- Fixed bug with stopping wrapped jobs via thread interrupt.
- Reworked state so components can have different state. Services and structural jobs
  now have different states to Jobs.
- JMX server now has a read only mode.
- JMX server will now use the Platform MBean Server if no URL
  is provided.
- Fixed bug in foreach where @Inject didn't work.
- foreach now has purgeAfter and preLoad properties that introduce the idea
  of an execution window for values.
- Support for opening several Oddjob Explorers from one Oddjob has been added
  by the introduction of a MultiExplorerLauncher job. This is now the default 
  job for the explorer element.
- A DelimitedType has been introduced to support more flexible String to 
  String array conversion than just CSV.
- foreach now provides a file attribute for configuration and it supports the
  Design Inside action for configuration. It was necessary to introduce a
  root foreach element in the inner configuration.
- fix bug where Oddjob leaves configuration input streams open.
- Echo can now echo any array of String using the lines property.
- XML can now provide an ArooaConfiguration that also supports
  inline editing and saving back into the original configuration that
  the xml property of the arooa:configuration type did not.
- foreach supports execution of it's children in parallel.
- Schedules have been refactored. Element names have changed and merged:
    dayOfYear -> yearly
    monthOfYear -> yearly
    dayOfMonth -> monthly, which now has weekOfMonth and dayOfWeek to allow
      things like the last Friday of the month to be specified more easily.
    dayOfWeek ->  weekly
    time -> daily, also introduced a new time schedule that is a single
      once only time.
    dayAfter -> day-after, also introduced day-before to improve scheduling
     around holidays.
- Removed Ids from Values. This is because they over complicated 
  configuration via the GUI. Introduced a new IdentifiableValueType, 
  with tag <identify>, that can used to register a value if required.
- Scheduling jobs (Timer and Retry) that are still ACTIVE go to READY
  if stopped. This ensures they can be restarted without a reset, which had caused
  their schedules to be reset, instead of picking up where they left off.
- Parallel is now completely asynchronous so that it doesn't wait for it's children.
  Waiting for children can still be achieved by nesting in a <state:join> job.
  State transitions are now predictable (seen release notes for version 0.30.0). 
- Fixed problems in <state:cascade> that cause an exception when attempting
  to drag and drop children. 
- Problems with scheduling around Daylight Saving Times have been fixed.
- Changed Echo text property to be XML text rather than an attribute to allow
  multiline echos in one echo job.
- Explorer and Designer have been tidied up. Shortcut keys now work.
  Escape and Ctrl-Enter accelerator keys have been added to dialogs. Node
  selection is more intelligent after a selected node is removed or modified.
  The root node is automatically in focus when the job tree is loaded.
- Logging messages have been tidied up. The job name has been removed from log
  messages and added to the Log4j Mapped Diagnostic Context instead with the
  key 'ojname'.
- Upgraded Apache commons-io to be 2.1
- Upgraded HSQL to 2.2.5
 

Changes in 0.30.0
-----------------
- Properties Panel of Explorer now updates on Job completion.
- Explorer dialog positioning has been improved.
- Job sleep and stop now use State synchronisation as race conditions were
  causing tests to fail.
- Upgraded Ant libs to 1.8.2.
- Oddjob now unpacks into an oddjob directory without version numbers. This 
  is so oddballs can unpack from a zip file easily.
- The Arooa Descriptor format has been simplified.
- Throttling can now be applied to parallel execution to reduce the number
  of simultaneously running jobs.
- Found the bug with dragging configuration with id's. Ids are no longer 'lost'.
- Log4j version has been updated to 1.2.16 and support the TRACE level added.
- Child and Parent Oddjobs can now share properties and values.
- Properties can now override existing properties with the override attribute.
  The environment suffix can only now be used with PropertiesJob now, not
  the PropertiesType.
- Added an Unload Action. This allows a configuration to be unloaded from
  oddjob without using Hard Reset.
- Added an AddJob action. This allows a job to be added to Oddjob without
  the need to use the designer on the parent job, which destroys the 
  state of any jobs already run for that parent.
- Allowed Parallel not to wait for it's children to complete. Not sure if
  this is a good idea - maybe should keep thread driven and event driven
  jobs separate. Also this introduces an uncertainty with the state
  sequence as parallel can now go READY - EXECUTING - READY - EXECUTING
  depending on how quickly the child executes. This will need thinking about
  for the next version.
- Converted Oddjob's date format to ISO 8601 - i.e. yyyy-mm-dd. Unlike the old
  format that used the names of the month, this ensures that an Oddjob 
  configuration created on a JVM in one locale can be used on a JVM with any 
  other locale.
- Converted dayofweek/monthofyear/dayofyear to number formats for the same
  reason.
- A new ConvertType has been introduced to force conversions.
- The <class> element has become <bean>, and <class-for> has become <class>. 
  This seemed more appropriate, especially for Spring users, but I was 
  beginning to regret it after changing a 100 or so tests.
- BufferType now resets it's contents when configured.
- Removed depricated client/server tags. Now only namespaced 
  jmx:client/jmx:server tags are allowed.
- An Input job has been added that allows Oddjob to ask for user input, 
  either from the console or from OddjobExplorer.
- A command line parser for Exec has been implemented which allows for quotes 
  and arguments over multiple lines.
- More Examples have been added to the Reference.
- A mechanism for including XML Files in the reference has been implemented. 
  This allows examples to be wrapped in a unit test.
- Changed the reference to be Descriptor driven. All properties are 
  included whether they are documented or not.
- Changed the reference format to have summary sections for properties and 
  examples.
- Echo job no longer throws an exception when there no text. It prints a 
  blank line instead.
- Fixed bugs with the merging of list types.
- Added conversion from String to String[] that treats string as a CSV.

Changes in 0.29.0
-------------------
- Introduced a new XMLConfigurationType to allow ArooaConfiguration
objects to be created in configurations.
- Changed the foreach job to use an ArooaConfiguration instead of
just text xml.
- Removed xml and input properties from Oddjob as these can now
be set with the XMLConfigurationType.
- Changed the persisting of Oddjobs again. Now Oddjob is persisted
but the last reset is remembered in applied to the child job
before running.
- Allow Values to have ids as well as components.
- Fixed bug in services where they were only applied for a components 
properties.
- Changed class loading to use services instead of the context class loader.
- BufferType can now take a String or List of Strings. It will now also return an 
array of Strings if required.
- Added a BeanReport which produces a simple tabular report on the properties of
beans.
- Changed SQL persistence to use a BLOB data type, and to use the correct
ClassLoader when restoring jobs.
- Added support for Prepared and Callable statements to SQL job.
- Added an FTP Oddball.
- Added loading of user properties via oddball.properites.
- Fixed problems where accelerator keys aren't associated with actions 
in Explorer
- A new mail Oddball has been added.
- Examples now use new Net and Mail Oddballs.
- Arrays now convert to a list.
- SQL Date, Time and Timstamp conversions have been added.
- Date type has a format property.
- BigDecimal conversions added.
- SQLJob changed to cope with scripts.
- SQLJob now has different possible result processor including 
a result sheet and bean capture.
- A simple pipeline processing paradime, called BeanBus, has been added to
support changeable result processors for SQLJob.
- Changed BSF to Java 1.6 javax.scripting. The bsf Oddball has been removed.
- stdout, stderr, logout and tee types added to capture output.
- Provide a BeanView that provides Titles for properties (work in progress).
- Competing for work functionality has been added with GrabJob and Keeper.
- Basic archiving support has been added using the idea of Silhouettes.
- Dynamic definition of beans is now possible using MagicBeanDescriptorFactory.
- Changed to preferring long element names to be hyphenated rather than 
camel case. i.e. filePersister is now file-persister.
- Removed setting of ContextClassLoader for Oddjob except for launch classpath.
- Reworked properties yet again. Now normal java properties can be referenced in
attribute expressions, and will be used first before component properties.
- JMXClient is now LogEnabled.
- The remote structural handler now uses a single notification with all child 
state, which means the JMX client can cope with missed structural events.
- Stopping jobs now waits for a job to stop before returning. Destroying jobs
now calls stop, so that jobs are stopped before they are deleted/dragged etc.
- State locking has been reworked to be stricter. this means the order of events is now 
completely predictable, but the risk of deadlock has been increased. So far however no
deadlocking has been observed.
- Stafeful now include as lastJobStateEvent method.
- state:cascade and state:join jobs have been added.
- Upgraded the Spring Oddball to Spring-3.0.3.
- The Default Executors now uses an unlimited thread pool for immediate 
execution - i.e. Parallel jobs.

Changes in 0.28.0
-------------------
- Allowed Explorer Actions to be pluggable.
- Introduced a Loadable method allows Oddjob, and the foreach job, to be 
loaded without running.
- Changed Resetable interface to include return status.
- Fixed a bug in Timer where the ScheduleContext wasn't reset.
- Changed cut and paste of Oddjob nodes to act like other simple 
  jobs.
- Added a Design Inside action for Oddjob nodes so they can be designed like 
  other jobs.
- Added a check job which is analogous to the unix test command for text and 
numbers.
- Added a modified flag to ConfigurationSession. 
- Oddjob Explorer now indicates the name of the current configuration (Oddjob) in
the title bar with a * when it's modified.
- Oddjob Explorer now warns when closing a session that contains modifications.
- AntJob can now be stopped but this is a real bodge. The log throws an Exception
causing the build to fail!
- Fixed bug in Designer where Value type fields were not updated.
- Cut and Paste Errors in OddjobExplorer now display in a dialog, not the
error log.
- JMX handlers are now pluggable.
- Changed JMX client handling to allow for missing or different 
version ClientHandlers to allow for a miss match between client and server.
- Improved automatic type conversion to allow for the conversion of any super type. 
- Improved conversion of arrays.
- Added general enum conversions.
- Added the ValueFactory interface and conversion for simple value wrappers.
- State now includes a DESTROYED state. Trigger and Mirror now throw an exception
if their watched job moves to this state.
- A JMX Server can now take an environment map which allows security properties
to be set. A simple security ValueFactory has been created that provides
a JMX simple security environment.
- A JMX Client can now take an environment map which allows security credentials
to be set. A username password ValueFactory has been created that provides
the client credentials for JMX security.
- The JMX Client now detects network failure via a heart beat, and causes the client
to enter an EXCEPTION state.
- Job Synchronisation has now been merged with state change which is much
simpler and elegant.
- Split out the code to create more Oddballs, oj-ant, oj-bsf, 
oj-hsql. Created a new oj-assembly project to build them all.
- Added basic autowiring and use it to improve the setting of Executors 
in the timer jobs.

Known Issues
------------
- Setting a remote property does not work on a remote bean which is a
DynaBean, such as VariablesJob. This is probably because the DynaClass 
used for the MBeans doesn't fake properties like the BeanUtils LazyDynaClass 
does.
- Setting remote nested properties doesn't work. Setting a top level property is
fine but with a property such as x.y BeanUtils will bring x across the network
and set y on the local copy.
- An XML namespace can not be removed from designer once it has been introduced.

