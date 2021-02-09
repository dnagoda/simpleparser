package nagoda.simpleparser

import java.text.SimpleDateFormat

import SimpleParserCore._
import cats.implicits._

class SimpleParserCLISpec extends UnitSpec {
  import SimpleParserCLI._

  val YYYYMMDD_DATE = new SimpleDateFormat("yyyyMMdd")

  "parseArgs" should "return filename when available" in {
    val filename = "filename"
    val argsWithFilename = Array(filename)
    parseArgs(argsWithFilename).success.value should equal (filename)
  }

  "parseArgs" should "throw exception when filename is unavailable" in {
    val argsWithoutFilename = Array[String]()
    parseArgs(argsWithoutFilename).failure.exception should have message FILENAME_REQUIRED_MSG
  }

  "readFile" should "return a List of lines" in {
    withFile(TEST_DATA, DELIMITERS(PIPE)) { filename =>
      val lines = readFile(filename).success.value
      lines.length should equal (TEST_DATA.length)
    }
  }

  "readFile" should "throw Exception when file is unavailable" in {
    readFile(" invalid_filename ").failure.exception shouldBe a [Throwable]
  }

  "sortByEmailDescAndLastNameAsc" should "order records by email ascending and then lastname descending" in {
    val rawData = createRawData(TEST_DATA, DELIMITERS(PIPE))
    val parsedData = rawData.map(parseLine(_, DELIMITERS(PIPE))).sequence.success.value
    val sortedData = sortByEmailDescLastNameDesc(parsedData)

    val firstRecord = sortedData.head
    firstRecord.email should equal ("zemail")
    firstRecord.lastName should equal ("zlastname")

    val lastRecord = sortedData.last
    lastRecord.email should equal ("1email")
    lastRecord.lastName should equal ("1lastname")

    val firstAAEMAILRecord = sortedData(3)
    firstAAEMAILRecord.email should equal ("aaemail")
    firstAAEMAILRecord.lastName should equal ("aalastname")

    val lastAAEMAILRecord = sortedData(5)
    lastAAEMAILRecord.email should equal ("aaemail")
    lastAAEMAILRecord.lastName should equal ("aclastname")
  }

  "sortByDateOfBirthAsc" should "order records by date of birth ascending" in {
    val rawData = createRawData(TEST_DATA, DELIMITERS(PIPE))
    val parsedData = rawData.map(parseLine(_, DELIMITERS(PIPE))).sequence.success.value
    val sortedData = sortByDateOfBirthAsc(parsedData)

    sortedData.head.dateOfBirth should equal (YYYYMMDD_DATE.parse("19750301"))
    sortedData.last.dateOfBirth should equal (YYYYMMDD_DATE.parse("20100615"))
  }

  "sortByLastNameDesc" should "order records by lastname descending" in {
    val rawData = createRawData(TEST_DATA, DELIMITERS(PIPE))
    val parsedData = rawData.map(parseLine(_, DELIMITERS(PIPE))).sequence.success.value
    val sortedData = sortByLastNameDesc(parsedData)

    sortedData.head.lastName should equal ("zlastname")
    sortedData.last.lastName should equal ("1lastname")
  }
}
