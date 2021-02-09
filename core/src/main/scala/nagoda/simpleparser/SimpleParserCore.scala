package nagoda.simpleparser

import java.text.SimpleDateFormat
import java.util.Date

import scala.util.Try

object SimpleParserCore {
  val PIPE = "PIPE"
  val COMMA = "COMMA"
  val SPACE = "SPACE"

  val DELIMITERS = Map(
    PIPE -> '|',
    COMMA -> ',',
    SPACE -> ' '
  )

  val DEFAULT_DATE_FORMAT = new SimpleDateFormat("M/d/yyyy")

  case class UserData(
    lastName: String,
    firstName: String,
    email: String,
    favoriteColor: String,
    dateOfBirth: Date
  )

  /*
  There are scenarios where the first line would indicate one
  delimiter and subsequent lines would indicate a different
  delimiter. If that's a common scenario, then we'd want to
  consider adding a --delimiter argument so that the user
  could indicate the expected delimiter.
   */
  val UNKNOWN_DELIMITER_MSG = "Unable to detect delimiter!"
  def detectDelimiter(line: String): Try[Char] = {
    Try(
      if (line.split(DELIMITERS(PIPE)).length == 5) {
        DELIMITERS(PIPE)
      } else if (line.split(DELIMITERS(COMMA)).length == 5) {
        DELIMITERS(COMMA)
      } else if (line.split(DELIMITERS(SPACE)).length == 5) {
        DELIMITERS(SPACE)
      } else {
        throw new Exception(UNKNOWN_DELIMITER_MSG)
      }
    )
  }

  val PARSING_ERROR_MSG = "Unable to parse line"
  def parseLine(line: String, delimiter: Char) = {
    Try(
      line.split(delimiter) match {
        case Array(lastName, firstName, email, favoriteColor, dob) => UserData(
          lastName, firstName, email, favoriteColor, DEFAULT_DATE_FORMAT.parse(dob)
        )
        case line => throw new Exception(s"$PARSING_ERROR_MSG: $line")
      }
    )
  }
}