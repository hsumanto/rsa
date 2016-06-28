This project contains a query system for NetCDF datasets (and other CDM datasets
that can be read by the NetCDF-Java library).

NOTE: Despite the name, this project has no dependency on the Raster Storage
Archive. However, the RSA (and other systems) may use rsaquery as a library for
image processing, by implementing org.vpac.ndg.query.io.DatasetProvider.

To create a new filter and query:

 1. Create a filter class that extends org.vpac.ndg.query.Filter.
 2. Create an XML file that describes the query, by linking the filter to inputs
    and outputs.
 3. Run the query using the QueryTest application.

JUnit tests are provided; they may be run using Gradle.
