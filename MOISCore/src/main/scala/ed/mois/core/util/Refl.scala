/*
 * Contains some reflection utilities.
 *
 * Authors: 
 * - Dominik Bucher, ETH Zurich, Github: dominikbucher
 */

package ed.mois.core.util

import java.lang.reflect.Field

object Refl {
  def getCCParams(cc: AnyRef) =
    (Map[String, Any]() /: cc.getClass.getDeclaredFields) { (a, f) =>
      f.setAccessible(true)
      a + (f.getName -> f.get(cc))
    }

  def getTinyCCFields(cc: AnyRef) =
    (Map[String, Any]() /: cc.getClass.getDeclaredFields) { (a, f) =>
      f.setAccessible(true)
      if (f.get(cc).isInstanceOf[ed.mois.core.tiny.Field[_]])
        a + (f.getName -> f.get(cc))
      else a
    }
  
  def getTinyCCFieldNames(cc: AnyRef) =
    (Map[Int, String]() /: cc.getClass.getDeclaredFields) { (a, f) =>
      f.setAccessible(true)
      if (f.get(cc).isInstanceOf[ed.mois.core.tiny.Field[_]])
        a + (f.get(cc).asInstanceOf[ed.mois.core.tiny.Field[_]].id -> f.getName)
      else a
    }

  def getStormCCFields(cc: AnyRef) =
    (Map[String, Any]() /: cc.getClass.getDeclaredFields) { (a, f) =>
      f.setAccessible(true)
      if (f.get(cc).isInstanceOf[ed.mois.core.storm.StormField[_]])
        a + (f.getName -> f.get(cc))
      else a
    }
  
  def getStormCCFieldNames(cc: AnyRef) =
    (Map[Int, String]() /: cc.getClass.getDeclaredFields) { (a, f) =>
      f.setAccessible(true)
      if (f.get(cc).isInstanceOf[ed.mois.core.storm.StormField[_]])
        a + (f.get(cc).asInstanceOf[ed.mois.core.storm.StormField[_]].id -> f.getName)
      else a
    }

  def getCCparamList(cc: AnyRef) = cc.getClass.getDeclaredFields.toList

  def setCCField(cc: AnyRef, name: String, value: Any) {
    cc.getClass.getMethods.find(_.getName == name + "_$eq").get.invoke(cc, value.asInstanceOf[AnyRef])
  }

  def getCCParamsLength(cc: AnyRef) = {
    cc.getClass.getDeclaredFields.length
  }
}