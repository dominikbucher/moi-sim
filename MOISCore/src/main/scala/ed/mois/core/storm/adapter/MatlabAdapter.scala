/*
 * Authors: 
 * - Dominik Bucher, ETH Zurich, Github: dominikbucher
 */

package ed.mois.core.storm.adapter

import ed.mois.core.storm._
import matlabcontrol._

trait MatlabAdapter[T <: StormState[_]] {
	self : StormProcess[T] => 

	/**
	 * List of ids to be copied to Matlab
	 */
	val copyPropIds: List[StormField[_]]
	/**
	 * The name of the function to be called in Matlab
	 */
	val functionName: String

	// Create a proxy, which we will use to control Matlab
	// The options set here will allow to use an already running Matlab instance
	val options: MatlabProxyFactoryOptions = new MatlabProxyFactoryOptions.Builder()
                                         //.setHidden(true)
                                         .setUsePreviouslyControlledSession(true)
                                         //.setProxyTimeout(30000L)
                                         .build()
    val factory: MatlabProxyFactory = new MatlabProxyFactory(options)
    val proxy: MatlabProxy = factory.getProxy()

    // Calls the Matlab function "functionName"
    def test {
		proxy.feval(functionName)
	}

	override def _evolve(states: T, t: Double, dt: Double): List[StormChange] = {
	  	states.resetChanges
	  	evolve(states, t, dt)
		copyPropIds.foreach(field => proxy.setVariable(states.fieldNames(field.id), states.fields(field.id)))
		proxy.eval(functionName)
		// Accessing the variables from Matlab looks complicated, because Matlab handles everything as an array of doubles
		// Also note that only doubles are supported at the moment
		copyPropIds.foreach(field => states.fields(field.id) = proxy.getVariable(states.fieldNames(field.id)).asInstanceOf[Array[Double]](0))

		// TODO What remains is to collect and return the changes, now that all copied variables have their updated values

		List.empty[StormChange]
	}

	// Leaving evolve empty, as everything is done in _evolve
    def evolve(state: T, t: Double, dt: Double) {
      
    }

    /**
     * Method to disconnect from Matlab.
     */
    def disconnect() {
    	proxy.disconnect()
    }
}