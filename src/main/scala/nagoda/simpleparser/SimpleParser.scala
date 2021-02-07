package nagoda.simpleparser

import java.text.SimpleDateFormat
import java.util.Date

import scala.io.Source
import scala.util.{Failure, Success, Try}

/*
  The SimpleParser command line tool takes a file in 1 of 3 formats, parses
  the contents and then outputs 3 versions of the data in the following orders:
    * email descending
    * birth date ascending
    * last name descending

  All files have the same field order:

  LastName
  FirstName
  Email
  FavoriteColor
  DateOfBirth

  The file formats different in the field delimiter used. The 3 field delimiters are:

  Pipe, '|'
  Comma, ','
  Space, ' '

  Usage:

  simpleparser <filename>
 */
object SimpleParser {
  val PIPE = "PIPE"
  val COMMA = "COMMA"
  val SPACE = "SPACE"

  val DELIMITERS = Map(
    PIPE -> '|',
    COMMA -> ',',
    SPACE -> ' '
  )

  case class UserData(
    lastName: String,
    firstName: String,
    email: String,
    favoriteColor: String,
    dateOfBirth: Date
  )

  val FILENAME_REQUIRED_MSG = "Invalid arguments, filename is required."
  def parseArgs(args: Array[String]): Try[String] = {
    if (args.length > 0) {
      Success(args(0))
    } else {
      Failure(new Exception(FILENAME_REQUIRED_MSG))
    }
  }

  def readFile(filename: String): Try[List[String]] = Try(Source.fromFile(filename).getLines().toList)

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

  val DEFAULT_DATE_FORMAT = new SimpleDateFormat("M/d/yyyy")
  val PARSING_ERROR_MSG = "Unable to parse line"
  def parse(lines: List[String], delimiter: Char) = {
    Try(
      lines.map(_.split(delimiter) match {
        case Array(lastName, firstName, email, favoriteColor, dob) => UserData(
          lastName, firstName, email, favoriteColor, DEFAULT_DATE_FORMAT.parse(dob)
        )
        case line => throw new Exception(s"$PARSING_ERROR_MSG: $line")
      })
    )
  }

  def print(userData: List[UserData], sortMessage: String) = {
    println(sortMessage)
    userData.foreach {
      case UserData(lastName, firstName, email, favoriteColor, dateOfBirth) =>
        println(s"$lastName, $firstName, $email, $favoriteColor, ${DEFAULT_DATE_FORMAT.format(dateOfBirth)}")
    }
  }

  def sortByEmailDescLastNameDesc(userData: List[UserData]): List[UserData] = {
    userData.sortWith((l, r) => {
      if (l.email == r.email) {
        l.lastName < r.lastName
      } else {
        l.email > r.email
      }
    })
  }

  def sortByDateOfBirthAsc(userData: List[UserData]): List[UserData] = {
    userData.sortBy(_.dateOfBirth)
  }

  def sortByLastNameDesc(userData: List[UserData]): List[UserData] = {
    userData.sortBy(_.lastName)(Ordering[String].reverse)
  }

  val USAGE = "Usage: simpleparser filename"
  def printUsage(e: Throwable): Unit = {
    println(s"$e\n$USAGE")
  }

  def main(args: Array[String]): Unit = {
    val userData = parseArgs(args)
      .flatMap(readFile)
      .flatMap(sourceLines =>
        detectDelimiter(sourceLines.head).flatMap(delimiter => parse(sourceLines, delimiter))
      ) match {
        case Success(userData) => userData
        case Failure(e) =>
          printUsage(e)
          sys.exit(1)
      }

    Seq(
      (sortByEmailDescLastNameDesc _, "Sorted by Email (desc), LastName (asc)"),
      (sortByDateOfBirthAsc _,  "Sorted by DateOfBirth (asc)"),
      (sortByLastNameDesc _, "Sorted by LastName (desc)")
    ).foreach {
        case (sorter, message) => print(sorter(userData), message)
    }
  }
}
