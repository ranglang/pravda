package pravda.cli.languages

package impl

import java.nio.file.{Files, Paths}
import java.nio.ByteBuffer
import java.nio.channels.Channels

import cats.Id
import com.google.protobuf.ByteString
import scala.sys.process.stdin

class IoLanguageImpl extends IoLanguage[Id] {

  def createTmpDir(): Id[String] =
    Files.createTempDirectory("pravda-cli").toAbsolutePath.toString

  def readFromStdin(): Id[ByteString] = {
    val buf = ByteBuffer.allocate(65536)
    val channel = Channels.newChannel(stdin)
    while (channel.read(buf) >= 0) ()
    buf.flip
    ByteString.copyFrom(buf)
  }

  def readFromFile(pathString: String): Id[Option[ByteString]] = {
    val path = Paths.get(pathString)
    if (Files.exists(path) && !Files.isDirectory(path)) {
      Some(ByteString.copyFrom(Files.readAllBytes(path)))
    } else {
      None
    }
  }

  def writeToStdout(data: ByteString): Id[Unit] =
    sys.process.stdout.write(data.toByteArray)

  def writeStringToStdout(data: String): Id[Unit] =
    sys.process.stdout.print(data)

  def writeStringToStderrAndExit(data: String, code: Int): Id[Unit] = {
    sys.process.stderr.print(data)
    sys.exit(code)
  }

  def writeToFile(pathString: String, data: ByteString): Id[Unit] = {
    val path = Paths.get(pathString)
    if (!Files.isDirectory(path)) {
      Files.write(path, data.toByteArray)
    }
  }

  def exit(code: Int): Id[Unit] = {
    sys.exit(code)
  }
}
