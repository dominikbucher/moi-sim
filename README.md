Module Integration Simulator
============================

A simulator targeted at modular models. This is of special interest with huge models, as the modular description allows to specify and test different parts independently. An exemplary area of use are so-called whole cell models in biology, where one tries to describe every part of a biological cell. As various aspects of the cell are known, they can be integrated into a bigger model by using the MOI Simulator. Also, plugging of components gets easy.

Another goal of the MOI Simulator is to define systems in easy ways and to allow the different modules to be described using different languages and modelling techniques. 

# Requirements
The following things are needed to run the simulator:
* sbt 0.11.2+ (http://www.scala-sbt.org/)
* Plotting requires gnuplot installed and on your path

# Installation and First Steps
Clone the repository into a folder of your choice, open a command line terminal there and type:
    sbt
Sbt will download all the dependencies. Check if everything runs by running some tests:
    test
    test:run
This will compile the code and (1) run unit tests (2) allow you to select a model to run. Recommended simulation engine is the storm simulator (as development focuses on that for now). Select an example model from ed.mois.test.storm.

# Developing Your own Model
Easiest to get your own model started is to copy and modify one of the example models, e.g. ed.mois.test.storm.bollenbach._

Copy all the files (in the example this is only one, but you're not required to keep everything in one file) and modify some things. 

# Warnings
As of this commit, the web interface isn't working (still relying on some old stuff).

Tested on Windows 8 64 Bit. No warranties for other platforms. 