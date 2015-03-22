==============================================================
The GLIMPSES toolkit
Version 1.0
README

Developed by Jaswanth Sreeram, Ashwini Bhagwat, Sarang Ozarde
==============================================================

------
Index
------

1. Introduction
2. Directory Structure
3. Pre-requisites 
4. Installation
5. Using GLIMPSES

---------------
1. Introduction
---------------
The GLIMPSES toolkit provides profiling tools for understanding program
behavior and evaluating the suitability of program regions to be executed
on the SPEs of a CELL processor.

The profiling tool used is PIN - A Dynamic Instrumentation Tool from Intel. PIN framework binary is free, and the tools written on top of it are open source, 
it can be found at http://www.pintool.org. The GLIMPSES toolkit has modified the tool calltrace to profile the workload and extract the parameters. 

This toolkit also uses the prefuse toolkit for providing the programmer with 
visualizations of the profiles that are generated as part of using GLIMPSES.
For the sake of convenience, a distribution of both PIN and prefuse is
provided along with the GLIMPSES toolkit. Both PIN and prefuse use their own licenses, please see the licenses.txt.

For more information on the toolkit, please see 
http://sourceforge.net/projects/glimpses.

----------------------
2. Directory Structure
----------------------
The top level GLIMPSES directory contains the following sub-directories:

pin/      : This contains the PIN distribution.

prefuse-beta/ : This contains the prefuse distribution.

WorkBench/ : This contains programs that help one to use the profiling 
	    pass in the compiler. It also contains some sample applications to run 
	    the profiling on. See Section 5.
	
In addition to these, there is also a Makefile in the top level 
directory that can be used to build all the components in this GLIMPSES
distribution.

-----------------
3. Pre-requisites
-----------------
The pre-requisites for installing and using GLIMPSES are listed below. Note that 
somewhat recent versions of these tools will do. Also note that most of
these packages are installed by default on any recent Linux distribution.

 GNU Make (3.79)
 GCC (>=4.0)
 Flex
 Bison
 libtool
 autoconf
 Java 1.6
 dot (this can be downloaded at   http://www.graphviz.org/Download_linux.php)
 Ghostview 
 Emacs

In addition, other Linux/Unix commandline utilities like grep, cp, ln, echo
etc. are assumed to be available.

 * Be sure to add dot and gv to your path variable

-----------------------
4. Building/Installation
------------------------
To build the toolkit, run make in the top-level directory.
One may have to set the JAVA_HOME & PATH environment variable for prefuse to build.

When make is run, it 
  - Builds Prefuse
  - Builds the PIN Tool  

-----------------
5. Using GLIMPSES
-----------------
To start using GLIMPSES, first build it using the information in Step 4.
Now, we need to do 2 things - Profiling & Visualization

Simply run the glimpses executable in the glimpses folder as
./glimpses

Follow the screen instructions from there to Profile/Visualize a workload.
Note that you need to first profile a workload, to be able to visualize the results. 

Alternatively, you can profile a workload as : 

(1) Profiling

Change directory to glimpses/WorkBench. 
Scripts named profile-* are the scripts used to generate profiles. 
When run,they do all of the following, automatically:
	
i.   Run the PIN tool on binary image of the application provided
ii.  Process the profiles generated for building the CFGs.
iii. Process the source code, if available, for displaying the source code related to selected function.

 For example, to profile the mpeg2dec applicaitons, type

 ./profile-mpeg2dec

At the end of the profiling process, a folder is created, which contains
the results of the profiling and files required for the visualization of 
the same. For example, once mpeg2dec is run, a folder called
mpeg2dec_O is created under glimpses/WorkBench/Output.


More information and help is provided in the Help section in the Visualization UI 

	* * * * *
