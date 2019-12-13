# JaCoLine https://jacoline.dev
Java Command Line Inspector

* Primary Goal

Provide a useful tool for developers and devops to understand and validate their Java command line options.

* Secondary Goal

Gather statistics on usage and errors made with VM switches and feed back to VM developers.

* Current Features
  
  Checks switches are available in your JDK version
  Warns when switches are marked for deprecation
  Warns about duplicate switches
  Checks values are in permitted range
  Checks if you are using debug VM switches with a non-debug JVM
  Checks you have "unlocked" experimental and diagnostic switches
  Suggests misspelled switches

* Future Work
  
  Add support for GraalVM, OpenJ9, Zing VMs
  Rules engine for switch values / combinations
  Suggested switches based on a requested application profile (e.g. latency/throughput, startup/peak performance)
