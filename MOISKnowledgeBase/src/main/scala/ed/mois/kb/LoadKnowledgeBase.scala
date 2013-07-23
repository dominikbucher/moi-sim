/*
 * Authors: 
 * - Dominik Bucher, ETH Zurich, Github: dominikbucher
 */

package ed.mois.kb

import ed.mois.kb.model._
import org.squeryl._
import org.squeryl.adapters.MySQLAdapter
import org.squeryl.PrimitiveTypeMode._

object LoadKnowledgeBase {
  import KnowledgeBase._

  Class.forName("com.mysql.jdbc.Driver")
  SessionFactory.concreteFactory = Some(() =>
    Session.create(
      java.sql.DriverManager.getConnection("jdbc:mysql://localhost/wholecellkb", Constants.USERNAME, Constants.PASSWORD),
      new MySQLAdapter))

  def countObjectsByModelType(modelType: String): Int = {
    transaction {
      val entries = from(KnowledgeBase.entry)(a => where(a.model_type === modelType) select a)
      entries.size
    }
  }

  def fromKbByWid[A](wid: String)(implicit m: Manifest[A]): A = {
    transaction {
      val entry = from(KnowledgeBase.entry)(a => where(a.wid === wid) select (a))
      entry.map(e => getKbEntry[A](e)).head
    }
  }

  def fromKbByName[A](name: String)(implicit m: Manifest[A]): A = {
    transaction {
      val entry = from(KnowledgeBase.entry)(a => where(a.name === name) select (a))
      entry.map(e => getKbEntry[A](e)).head
    }
  }

  def getKbEntry[A](entry: Public_Entry)(implicit m: Manifest[A]): A = {
    entry.model_type match {
      case Some("Parameter") => {
        val ret = for {
          p <- from(KnowledgeBase.parameter)(a => where(a.parent_ptr_species_component_id === entry.id) select (a))
          v <- from(KnowledgeBase.entrychardata)(a => where(a.id === p.value_id) select (a))
        } yield {
          m.erasure match {
            case c if c == classOf[Double] => v.value.map(_.toDouble).get.asInstanceOf[A]
            case _ => v.value.map(_.asInstanceOf[A]).get
          }
        }
        ret.head
      }
      case Some("State") => {
        throw new Exception(s"${entry.name.get}: Model type isn't implemented yet.")
      }
      case Some("ProteinComplex") => {
        throw new Exception(s"${entry.name.get}: Model type isn't implemented yet.")
      }
      case Some("Type") => {
        throw new Exception(s"${entry.name.get}: Model type isn't implemented yet.")
      }
      case Some("Process") => {
        val ret = for {
          p <- from(KnowledgeBase.process)(a => where(a.parent_ptr_species_component_id === entry.id) select (a))
        } yield {
          p.asInstanceOf[A]
        }
        ret.head
      }
      case Some("ChromosomeFeature") => {
        throw new Exception(s"${entry.name.get}: Model type isn't implemented yet.")
      }
      case Some("Note") => {
        throw new Exception(s"${entry.name.get}: Model type isn't implemented yet.")
      }
      case Some("Stimulus") => {
        throw new Exception(s"${entry.name.get}: Model type isn't implemented yet.")
      }
      case Some("Reference") => {
        throw new Exception(s"${entry.name.get}: Model type isn't implemented yet.")
      }
      case Some("Compartment") => {
        throw new Exception(s"${entry.name.get}: Model type isn't implemented yet.")
      }
      case Some("Chromosome") => {
        throw new Exception(s"${entry.name.get}: Model type isn't implemented yet.")
      }
      case Some("Pathway") => {
        throw new Exception(s"${entry.name.get}: Model type isn't implemented yet.")
      }
      case Some("TranscriptionUnit") => {
        throw new Exception(s"${entry.name.get}: Model type isn't implemented yet.")
      }
      case Some("Species") => {
        throw new Exception(s"${entry.name.get}: Model type isn't implemented yet.")
      }
      case Some("TranscriptionalRegulation") => {
        throw new Exception(s"${entry.name.get}: Model type isn't implemented yet.")
      }
      case Some("Metabolite") => {
        throw new Exception(s"${entry.name.get}: Model type isn't implemented yet.")
      }
      case Some("Gene") => {
        throw new Exception(s"${entry.name.get}: Model type isn't implemented yet.")
      }
      case Some("Reaction") => {
        throw new Exception(s"${entry.name.get}: Model type isn't implemented yet.")
      }
      case Some("ProteinMonomer") => {
        throw new Exception(s"${entry.name.get}: Model type isn't implemented yet.")
      }
      case _ => throw new Exception(s"${entry.name.get}: Model type doesn't match any known structure.")
    }
  }

  def getAllDifferentModelTypes() = {
    var modelTypes = collection.mutable.Set[String]()
    transaction {
      val entries = from(KnowledgeBase.entry)(a => select(a))
      entries.foreach(e => modelTypes.add(e.model_type.get))
    }
    modelTypes.toList
  }

  def getReactionsOfProcess(wid: String) = {
    transaction {
      val process = fromKbByWid[Public_Process](wid)
      val reactions = from(reaction, speciescomponent, entry)((r, sc, e) => {
        where(r.processes_id === process.parent_ptr_species_component_id and
          sc.parent_ptr_entry_id === r.parent_ptr_species_component_id and
          e.id === sc.parent_ptr_entry_id) select (r, sc, e)
      })
      reactions.toList
    }
  }

  def getMetabolites() = {
    transaction {
      val mets = from(metabolite, molecule, speciescomponent, entry)((met, m, sc, e) => {
        where(m.parent_ptr_species_component_id === met.parent_ptr_molecule_id and
          sc.parent_ptr_entry_id === m.parent_ptr_species_component_id and
          e.id === sc.parent_ptr_entry_id) select (met, m, sc, e)
      })
      mets.toList
    }
  }

  def getMetaboliteBiomassCompositions() = {
    transaction {
      val mets = from(metabolite)(met => select(met)).toList
      mets.map(m => {
        val bcs = (for (bc <- m.biomass_composition) yield bc).toList
        (m, bcs)
      })
    }
  }

  def main(args: Array[String]) {
    println("by count: " + countObjectsByModelType("Metabolite"))
    println("by name: " + fromKbByName[Double]("density"))
    //println("by name: " + fromKbByName[Double]("initialBiomassConcentration"))
    //println("by wid: " + fromKbByWid[Double]("Parameter_0017"))

    println(getReactionsOfProcess("Process_Metabolism").map(r => r._3.wid).mkString(", "))

    //    var mets: Query[Public_Metabolite] = null
    //    transaction {
    //      mets = from(metabolite)(met => select(met))
    //      val mols = for {
    //        m <- mets
    //        mol <- m.molecule
    //      } yield mol
    //      println(mols.mkString(", "))
    //    }

    //println(getAllDifferentModelTypes.mkString("\") => {\n\n}\n case Some(\""))
    //    transaction {
    //      val states = from(KnowledgeBase.state)(a => select(a))
    //      for {
    //        a <- states
    //        b <- a.parent_ptr_species_component_id
    //      } {
    //        val species = from(KnowledgeBase.speciescomponent)(x => where(x.parent_ptr_entry_id === b) select (x))
    //        for {
    //          c <- species
    //          d <- c.parent_ptr_entry_id
    //        } {
    //          val entries = from(KnowledgeBase.entry)(y => where(y.id === d) select (y))
    //          for {
    //            e <- entries
    //          } {
    //            println(e.name.get + "..." + a.parent_ptr_species_component_id)
    //          }
    //        }
    //      }
    //    }
  }
}