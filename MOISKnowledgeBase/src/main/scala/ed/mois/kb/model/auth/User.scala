/*
 * Authors: 
 * - Dominik Bucher, ETH Zurich, Github: dominikbucher
 */

package ed.mois.kb.model.auth

import ed.mois.kb.model.KnowledgeBaseObject
import java.util.Date
import ed.mois.kb.KnowledgeBase

class Auth_User (
  val username: String,
  val first_name: String,
  val last_name: String,
  val email: String,
  val password: String,
  val is_staff: Int,
  val is_active: Int,
  val is_superuser: Int,
  val last_login: Date,
  val date_joined: Date)
  extends KnowledgeBaseObject {
  
  lazy val user_profile = KnowledgeBase.userProfileUser.left(this)
}