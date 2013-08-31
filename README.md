Module Integration Simulator
============================

A simulator targeted at modular models. This is of special interest with huge models, as the modular description allows to specify and test different parts independently. An exemplary area of use are so-called whole cell models in biology, where one tries to describe every part of a biological cell. As various aspects of the cell are known, they can be integrated into a bigger model by using the MOI Simulator. Also, plugging of components gets easy.

Another goal of the MOI Simulator is to define systems in easy ways and to allow the different modules to be described using different languages and modelling techniques. 

You can find the thesis (which is also an in-depth documentation) under Thesis Documents. The project is split in four sub-projects.

### Core
Contains all the core parts of the simulator, i.e. different strategies to simulate models, the DSL to specify models and so on. 

Important notice: The core contains three simulators: sim, tiny and storm. ONLY use storm, as the others are deprecated and will eventually be removed.

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
```
This will compile the code and run some tests. To run exemplary models, change to the MOISModels project and run it:
```
project MOISModels
run
```
You will be prompted a model to select, which will then run with observables defined in the corresponding .scala file. 


## Developing Your own Model
Easiest to get your own model started is to copy and modify one of the example models, e.g. `ed.mois.models.storm.bollenbach._` in the MOISModels project. I HEAVILY recommend using the storm simulator, as this is the one currently being developed (the others (sim, tiny) will vanish within the next months). Copy all the files (in the Bollenbach example there is only one, but you're not required to keep everything in one file) and modify some things. Run as above with (don't forget to switch to the right project):
```
project MOISModels
run
```
The best documented model is the ResrouceProcessing model. 

For a real model (that is independent of this repository), create a new project depending on the simulator. Easiest is probably the compilation and packaging of the MOI Simulator via (in sbt):
```
package
```
This yields a moiscore_2.10-1.0.0.jar file in MOISCore\target\scala-2.10 which can be used as a library in any project. Haven't tested this yet, but sould work :).

## Use the web interface
To start the web interface, switch to the corresponding project in sbt and run it there:
```
project MOISWebInterface
container:start
```
You can then browse to 127.0.0.1:8080 and see the interface. Choose a simulation in the top right corner. Once it's loaded, select observables by clicking on corresponding names in the simulation graph and run the simulation by clicking the button in the top right. You will get the curves as output once the simulation is finished. Due to some debugging in development mode of Scalatra, it can take a while as the whole state vector trajectory is printed to the console as well. To avoid this, the application has to be deployed in production mode. 

## Warnings
Tested on Windows 8 64 Bit. No warranties for other platforms. Web interface a little clumsy sometimes, can easily be reloaded (F5 in browsers) though.