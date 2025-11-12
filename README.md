# Oddjob

Oddjob is a task automation and scheduling solution.

- [Running](#running)
- [Configuration](#configuration)
- [Modules](#modules)
- [Building](#building)
- [More Info](#more-info)

### Running

Full Oddjob distributions can be [Downloaded](http://rgordon.co.uk/oddjob/download.html)
and [Run from the Console](http://rgordon.co.uk/oddjob/current/userguide/started.html#running)

Or Oddjob can be started from your favorite IDE with this Maven dependency

```xml
<dependency>
    <groupId>uk.co.rgordon</groupId>
    <artifactId>oddjob</artifactId>
    <version>${oddjob.version}</version>
</dependency>
```

And this main `org.oddjob.Main` with 

```xml
<oddjob>
    <job>
        <echo>Hello World</echo>        
    </job>
</oddjob>
```

saved as `oddjob.xml` in your projects Working Directory.

Oddjob can also be [Embedded](http://rgordon.co.uk/oddjob/current/devguide/embedding.html)
in your own applications.

### Configuration

The [Reference Pages](docs/reference/README.md) provide lots of 
details for configuring Oddjob's various jobs.

### Modules

This is the main module for Oddjob, however extra functionality is
provided via Oddjob plugins called Oddballs.

- [oj-ant](https://github.com/robjg/oj-ant)
: Run Ant from within Oddjob

- [oj-net](https://github.com/robjg/oj-net) 
: FTP jobs

- [oj-mail](https://github.com/robjg/oj-mail)
: Mail jobs

- [oj-ssh](https://github.com/robjg/oj-ssh)
: SSH jobs

- [oj-web](https://github.com/robjg/oj-web)
: HTTP client and server jobs. Includes the server side for the 
web UI.

- [oj-resolve](https://github.com/robjg/oj-resolve)
: Resolve artifacts from Maven

### Building

To build this module as a Snapshot you will first need to clone and 
`mvn install` [oj-parent](https://github.com/robjg/oj-parent) and [arooa](https://github.com/robjg/arooa). You can then run
`mvn install` in this module.

[oj-assembly](https://github.com/robjg/oj-assembly) provides a module POM capable of building
all the Oddjob modules included in the distribution.

Other modules that contribute to the final Oddjob distribution are
[run-oddjob](https://github.com/robjg/run-oddjob) - the launch framework for Oddjob,
[oj-reactjs](https://github.com/robjg/oj-reactjs) - the Web UI front end, [oj-doc](https://github.com/robjg/oj-doc)
which builds the Reference pages amongst other things, and 
[oj-examples](https://github.com/robjg/oj-examples) which are examples and also the
source of the Oddjob documentation.

To see how to build a full Oddjob application see 
[The Developer Guide](http://rgordon.co.uk/oddjob/current/devguide/building.html)

### More Info

More information can be found on [Oddjob's Home Page](http://rgordon.co.uk/oddjob).

