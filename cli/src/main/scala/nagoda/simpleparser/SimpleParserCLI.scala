package nagoda.simpleparser

import cats.implicits._
import scala.io.Source
import scala.util.{Failure, Success, Try}

import SimpleParserCore._

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
object SimpleParserCLI {
  val FILENAME_REQUIRED_MSG = "Invalid arguments, filename is required."
  def parseArgs(args: Array[String]): Try[String] = {
    if (args.length > 0) {
      Success(args(0))
    } else {
      Failure(new Exception(FILENAME_REQUIRED_MSG))
    }
  }

  def readFile(filename: String): Try[List[String]] = Try(Source.fromFile(filename).getLines().toList)

  def printRecords(records: List[UserData], header: String) = {
    println(header)
    records.foreach {
      case UserData(lastName, firstName, email, favoriteColor, dateOfBirth) =>
        println(s"$lastName, $firstName, $email, $favoriteColor, ${DEFAULT_DATE_FORMAT.format(dateOfBirth)}")
    }
  }

  def sortByEmailDescLastNameDesc(records: List[UserData]): List[UserData] = {
    records.sortWith((l, r) => {
      if (l.email == r.email) {
        l.lastName < r.lastName
      } else {
        l.email > r.email
      }
    })
  }

  def sortByDateOfBirthAsc(records: List[UserData]): List[UserData] = {
    records.sortBy(_.dateOfBirth)
  }

  def sortByLastNameDesc(records: List[UserData]): List[UserData] = {
    records.sortBy(_.lastName)(Ordering[String].reverse)
  }

  val USAGE = "Usage: simpleparser filename"
  def printUsage(e: Throwable): Unit = {
    println(s"$e\n$USAGE")
  }

  def main(args: Array[String]): Unit = {
    val records = parseArgs(args)
      .flatMap(readFile)
      .flatMap(sourceLines =>
        detectDelimiter(sourceLines.head).flatMap(delimiter => sourceLines.map(parseLine(_, delimiter)).sequence
      )) match {
        case Success(records) => records
        case Failure(e) =>
          printUsage(e)
          sys.exit(1)
      }

    Seq(
      (sortByEmailDescLastNameDesc _, "Sorted by Email (desc), LastName (asc)"),
      (sortByDateOfBirthAsc _,  "Sorted by DateOfBirth (asc)"),
      (sortByLastNameDesc _, "Sorted by LastName (desc)")
    ).foreach {
        case (sorter, message) => printRecords(sorter(records), message)
    }
  }
}
