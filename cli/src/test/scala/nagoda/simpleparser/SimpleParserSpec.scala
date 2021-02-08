package nagoda.simpleparser

import java.io.{File, FileWriter}
import java.text.SimpleDateFormat

class SimpleParserSpec extends UnitSpec {
  import SimpleParser._

  val YYYYMMDD_DATE = new SimpleDateFormat("yyyyMMdd")

  val TEST_DATA = List(
    List("aclastname","acfirstname","aaemail","white","1/1/2000"),
    List("ablastname","abfirstname","aaemail","white","1/1/2000"),
    List("blastname","bfirstname","bemail","blue","12/31/1999"),
    List("alastname","bfirstname","bemail","blue","12/31/1999"),
    List("zlastname","zfirstname","zemail","red","3/1/1975"),
    List("1lastname","1firstname","1email","pink","6/15/2010"),
    List("aalastname","aafirstname","aaemail","white","1/1/2000")
  )

  val BAD_DATE_TEST_DATA = List(
    List("lastname", "firstname", "email", "white", "20000101"),
  )

  val TEST_FILENAME = "tmp_test_file"

  def createRawData(sourceData: List[List[String]], delimiter: Char): List[String] = {
    sourceData.map(_.mkString(delimiter.toString))
  }

  def write(filename: String, content: String): Unit = {
    val writer = new FileWriter(filename)
    try {
      writer.write(content)
    } finally {
      writer.close
    }
  }

  def delete(filename: String): Unit = {
    val file = new File(filename)
    file.delete()
  }

  def withFile(sourceData: List[List[String]], delimiter: Char)(test: String => Any) = {
    val content = createRawData(sourceData, delimiter).mkString("\n")
    write(TEST_FILENAME, content)
    test(TEST_FILENAME)
    delete(TEST_FILENAME)
  }

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

  "detectDelimiter" should "return PIPE when file is PIPE delimited" in {
    withFile(TEST_DATA, DELIMITERS(PIPE)) { filename =>
      val lines = readFile(filename).success.value
      detectDelimiter(lines.head).success.value should equal (DELIMITERS(PIPE))
    }
  }

  "detectDelimiter" should "return COMMA when file is COMMA delimited" in {
    withFile(TEST_DATA, DELIMITERS(COMMA)) { filename =>
      val lines = readFile(filename).success.value
      detectDelimiter(lines.head).success.value should equal (DELIMITERS(COMMA))
    }
  }

  "detectDelimiter" should "return SPACE when file is SPACE delimited" in {
    withFile(TEST_DATA, DELIMITERS(SPACE)) { filename =>
      val lines = readFile(filename).success.value
      detectDelimiter(lines.head).success.value should equal (DELIMITERS(SPACE))
    }
  }

  "detectDelimiter" should "throw Exception when delimiter is unknown" in {
    val UNKNOWN_DELIMITER = ':'
    withFile(TEST_DATA, UNKNOWN_DELIMITER) { filename =>
      val lines = readFile(filename).success.value
      detectDelimiter(lines.head).failure.exception should have message UNKNOWN_DELIMITER_MSG
    }
  }

  "parse" should "return a List of UserData objects" in {
    val rawData = createRawData(TEST_DATA, DELIMITERS(PIPE))
    val userData = parse(createRawData(TEST_DATA, DELIMITERS(PIPE)), DELIMITERS(PIPE)).success.value
    userData.length should equal (TEST_DATA.length)
    userData.zip(TEST_DATA).foreach {
      case (userData, expectedData) => {
        userData shouldBe a [UserData]
        userData.lastName should equal(expectedData(0))
        userData.firstName should equal(expectedData(1))
        userData.email should equal(expectedData(2))
        userData.favoriteColor should equal(expectedData(3))
        userData.dateOfBirth should equal(DEFAULT_DATE_FORMAT.parse(expectedData(4)))
      }
    }
  }

  "parse" should "throw Exception when date is in unexpected format" in {
    val rawData = createRawData(BAD_DATE_TEST_DATA, DELIMITERS(PIPE))
    parse(rawData, DELIMITERS(PIPE)).failure.exception shouldBe a [Throwable]
  }

  "sortByEmailDescAndLastNameAsc" should "order records by email ascending and then lastname descending" in {
    val rawData = createRawData(TEST_DATA, DELIMITERS(PIPE))
    val parsedData = parse(createRawData(TEST_DATA, DELIMITERS(PIPE)), DELIMITERS(PIPE)).success.value
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
    val parsedData = parse(createRawData(TEST_DATA, DELIMITERS(PIPE)), DELIMITERS(PIPE)).success.value
    val sortedData = sortByDateOfBirthAsc(parsedData)

    sortedData.head.dateOfBirth should equal (YYYYMMDD_DATE.parse("19750301"))
    sortedData.last.dateOfBirth should equal (YYYYMMDD_DATE.parse("20100615"))
  }

  "sortByLastNameDesc" should "order records by lastname descending" in {
    val rawData = createRawData(TEST_DATA, DELIMITERS(PIPE))
    val parsedData = parse(createRawData(TEST_DATA, DELIMITERS(PIPE)), DELIMITERS(PIPE)).success.value
    val sortedData = sortByLastNameDesc(parsedData)

    sortedData.head.lastName should equal ("zlastname")
    sortedData.last.lastName should equal ("1lastname")
  }
}
