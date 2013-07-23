Module Integration Simulator
============================

A simulator targeted at modular models. This is of special interest with huge models, as the modular description allows to specify and test different parts independently. An exemplary area of use are so-called whole cell models in biology, where one tries to describe every part of a biological cell. As various aspects of the cell are known, they can be integrated into a bigger model by using the MOI Simulator. Also, plugging of components gets easy.

Another goal of the MOI Simulator is to define systems in easy ways and to allow the different modules to be described using different languages and modelling techniques. 

The project is split in four sub-projects.

### Core
Contains all the core parts of the simulator, i.e. different strategies to simulate models, the DSL to specify models and so on.

### Macros
An own project to allow scala macros (it is it's own project as this needs to be compiled before the core project). This allows for some compile-time optimizations.

### Web Interface
A long-term goal of the simulator is to provide a platform where modelers can plug and play with different models and run and test them. The web interface is the first step towards this platform. As for now it gives an overview of available simulations, lets a user select different parts to be simulated and variables to be displayed and to run simulations.

### Knowledge Base
The knowledge base gives models a possibility to hook into databases to retreive constants and other information. At the moment this is available for the WholeCellKb developed by the Covert group at Stanford. The database needs to be installed locally. See http://wholecellkb.stanford.edu/ for more instructions and information. 

## Requirements
The following things are needed to run the simulator:
* A Java installation (think 1.7+ should be fine, probably also 1.6+)
* Sbt 0.11.2+ (http://www.scala-sbt.org/)
* Plotting requires gnuplot installed and on your path

## Installation and First Steps
Clone the repository into a folder of your choice, open a command line terminal there and type:
```
sbt
```
Sbt will download all the dependencies. Check if everything runs by running some tests:
```
test
test:run
```
This will compile the code and (1) run unit tests (2) allow you to select a model to run. Recommended simulation engine is the storm simulator (as development focuses on that for now). Select an example model from `ed.mois.test.storm`.

## Developing Your own Model
Easiest to get your own model started is to copy and modify one of the example models, e.g. `ed.mois.test.storm.bollenbach._`. I HEAVILY recommend using the storm simulator, as this is the one currently being developed (the others (sim, tiny) will vanish within the next weeks). Copy all the files (in the Bollenbach example there is only one, but you're not required to keep everything in one file) and modify some things. Run as above with:
```
test:run
```

For a real model (that is independent of this repository), create a new project depending on the simulator. Easiest is probably the compilation and packaging of the MOI Simulator via (in sbt):
```
package
```
This yields a moiscore_2.10-1.0.0.jar file in MOISCore\target\scala-2.10 which can be used as a library in any project. Haven't tested this yet, but sould work :).


## Warnings
As of this commit, the web interface isn't working (still relying on some old stuff).

Tested on Windows 8 64 Bit. No warranties for other platforms. 