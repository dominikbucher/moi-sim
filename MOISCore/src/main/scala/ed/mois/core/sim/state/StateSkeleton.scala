/*
 * Authors: 
 * - Dominik Bucher, ETH Zurich, Github: dominikbucher
 */

package ed.mois.core.sim.state

import ed.mois.core.util._
import ed.mois.core.kb.KBAccess
import ed.mois.core.sim.chg.TrackChange
import ed.mois.core.sim.prop.TrackedProperty
import ed.mois.core.sim.prop.PropertyId
import ed.mois.core.sim.prop.Property

/**
 * Defines a trait that outlines states, namely by letting them define
 * trackable properties.
 */
trait StateSkeleton extends TrackChange with KBAccess with Helpers {
  /**
   * The name of this state.
   */
  val name: String

  /**
   * Collection of properties used in this state. Add new ones by calling [[prop()]].
   */
  var props = collection.mutable.Map.empty[String, TrackedProperty[Any]]

  /**
   * Adds a property to the state.
   *
   * @param init The initialization value of the property.
   * @param name The name of the property. The name must be unique.
   * @return The newly created property.
   */
  def prop[T](init: T, propName: String) = {
    val property = trackable(init, PropertyId(name, propName, props.size))
    props += (propName -> property.asInstanceOf[TrackedProperty[Any]])
    property
  }

  /**
   * Overrides all properties in this state with the properties supplied.
   * The property names MUST be equivalent.
   *
   * @param newProps The new properties.
   */
  def overrideProps(newProps: List[Property[Any]]) {
    newProps.foreach(np => {
      props(np.id.name).update(np())
    })
  }
  
  /**
   * Overrides a single property.
   *
   * @param newProp The value to be written into the property.
   */
  def overrideProp(newProp: Property[Any]) {
    props(newProp.id.name).update(newProp())
  }
}