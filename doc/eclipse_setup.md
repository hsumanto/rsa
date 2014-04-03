# Developing RSA in Eclipse

These instructions are for [Eclipse][ecl] 4.3 "Kepler" on Ubuntu 13.10.

Note that these instructions cover setting up Eclipse to debug all of the
existing RSA projects. You can skip steps in certain situations. E.g. if you
aren't interested in the web services, don't import the SpatialCubeService
project and skip the instructions for Tomcat.

## Pre-requisites

Follow the instructions for setting up a [manual installation][mi]. RSA
should be checked out to its own directory; don't put it in your Eclipse
workspace.

Create a **new workspace** in Eclipse, and then install the Java EE libraries:

1. Choose *Help > Install New Software*
1. In the *Work with* box, choose the sources that match your version of
   Eclipse, e.g. `Kepler - http://download.eclipse.org/releases/kepler`.
1. Tick the box for *Web, XML, Java EE and OSGi Enterprise Development*, and
   proceed with the installation (press *Next >* etc.).
1. Restart Eclipse if it asks you to.

Install the Python development tools (optional: this is useful but not required
for rsaquery code generation):

1. Choose *Help > Eclipse Marketplace*
1. Search for 'pydev'.
1. Click *Install* in the PyDev row, and continue with the installation.
1. Restart Eclipse if it asks you to.
1. Choose *Window > Preferences > PyDev > Interpreters > Python Interpreter*.
1. Click *New*.
1. Set the name to "Python2", and the executable location to `/usr/bin/python2`.
1. Continue with the installation of the interpreter.

Add a Tomcat server to your workspace.

1. Configure your local Tomcat instance.
    1. Open a shell.
    1. Install `tomcat6-user`: `sudo apt-get install tomcat6-user`
    1. Change directory to your Eclipse workspace.
    1. Run `tomcat6-instance-create tomcat_instance`
1. In Eclipse, choose *Window > Preferences > Server > Runtime Environment*.
1. Click *Add*.
1. Select *Apache > Apache Tomcat v6.0* and click *Next*.
1. Set the *Tomcat installation directory* to the instance you created
   above (`tomcat_instance`).


## Library Dependencies

Most the RSA's dependencies are already checked in to the repository, and the
classpaths are set up to use them. However GDAL needs to be provided by the
system, so you need to configure it manually.

1. Choose *Window > Preferences > Java > Build Path > User Libraries*.
1. Click *New*.
1. Call the new library "GDAL". Do not tick the *system library* button.
1. Click *OK* to accept the creation of thew new library.
1. With the new GDAL library selected, click *Add External JARs*.
1. Browse to the location of your GDAL `.jar` file - e.g.
   `/usr/local/lib/gdal.jar`, and accept it (click *OK*).
1. Double-click on *Native library location* under *gdal.jar*.
1. Click on *External Folder* and choose */usr/local/lib*.


## Importing the Projects

Import all RSA projects into your workspace.

1. Right-click in the Projects or Package Explorer and choose *Import >
   General > Existing Projects into Workspace*.
1. Make sure all projects (*CmdClient*, *Common*, etc.) are selected.
1. Click *Finish*.

Note that a number of classes required by SpatialCubeService are provided by
Tomcat and GDAL. If you skipped ahead and imported the projects first, there
will be compilation errors until Tomcat and GDAL are set up (see above).


## Database Connection Settings

Ensure RSA has been configured to use the right database and storage pool. Refer
to the database configuration section of the [manual installation][mi] guide.

1. Copy `SpatialCubeService/config/*.SAMPLE`: remove the `.SAMPLE` prefix,
   and customise the contents (e.g. set passwords).
1. Do the same for `StorageManager/config/*.SAMPLE`.
1. Do the same for `CmdClient/config/*.SAMPLE`.

At this point, you should be able to run the tests and command line client (see
`org.vpac.ndg.cli.Client`). Read on for instructions for setting up the web
services.


## Configure the Server

Configure SpatialCubeService to start in Tomcat:

1. Choose *Window > Show View > Servers*.
1. Right-click in the blank area and choose *New > Server*.
1. Select *Apache > Tomcat v6.0 Server*.
1. Set the *Server runtime environment* to the server installed above (the
   default name is *Apache Tomcat v6.0*), and click *Next*.
1. Select *SpatialCubeService* from the list on the left, and click *Add >*.

Unfortunately, Eclipse [doesn't allow][eso] a project to automatically use a
native library in Tomcat. So you need to manually configure it to use GDAL.

1. Double-click on the new server in the Servers view. This will open a new
   editor for the server properties.
1. Choose *General Information > Open launch configuration*.
1. Choose *Environment > New*, and set `LD_LIBRARY_PATH` to `/usr/local/lib`
1. Choose the *Classpath* tab.
1. Select *User Entries* and click *Add External JARs*.
1. Browse to your `gdal.jar` file, e.g. `/usr/local/gdal.jar`.

[eso]: http://stackoverflow.com/a/7005867/320036


## Start the Server

Make sure PostgreSQL is running, and then start the Tomcat server. You should
now be able to get a list of datasets by going to:

    http://localhost:8080/SpatialCubeService/Dataset

[ecl]: http://eclipse.org/
[mi]: manual_install.md
