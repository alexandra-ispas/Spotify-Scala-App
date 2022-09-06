package com

import java.sql.ResultSet
import scala.util.Try

package object spotify {
  type AlteredTable = Try[Boolean]
  type CommandRunner = Try[Any]
  type QueryResult = Try[ResultSet]
}
