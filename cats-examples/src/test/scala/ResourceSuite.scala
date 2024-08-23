import cats.effect.IO
import cats.effect.kernel.Resource
import cats.effect.unsafe.implicits.global

import java.io.Closeable
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.nio.charset.StandardCharsets

class ResourceSuite extends munit.FunSuite {
  test("Resource is used for acquiring, using and releasing a resource") {
    def openRead(name: String): IO[FileInputStream] = IO.blocking {
      val file = new File(getClass().getClassLoader().getResource(name).getFile())
      new FileInputStream(file)
    }

    def openWrite(name: String): IO[FileOutputStream] = IO.blocking {
      val file = new File(getClass().getClassLoader().getResource(name).getFile())
      new FileOutputStream(file)
    }

    def close(closeable: Closeable): IO[Unit] = IO {
      closeable.close()
    }

    def fileReadR(name: String): Resource[IO, FileInputStream] =
      Resource.make[IO, FileInputStream](openRead(name))(file => close(file))

    def fileWriteR(name: String): Resource[IO, FileOutputStream] =
      Resource.make[IO, FileOutputStream](openWrite(name))(file => close(file))

    def read(stream: FileInputStream): IO[Array[Byte]] = IO {
      stream.readAllBytes()
    }

    def write(stream: FileOutputStream, content: Array[Byte]): IO[Unit] = IO {
      stream.write(content)
      stream.flush()
      stream.close()
    }

    val program: IO[Unit] =
      (
        for {
          in1 <- fileReadR("file1.txt")
          in2 <- fileReadR("file2.txt")
          out <- fileWriteR("file3.txt")
          in3 <- fileReadR("file3.txt")
        } yield (in1, in2, out, in3)
      ).use { case (file1, file2, file3, file4) =>
        for {
          bytes1 <- read(file1)
          _      <- IO.println(s"bytes1: ${String(bytes1, StandardCharsets.UTF_8)}")
          bytes2 <- read(file2)
          _      <- IO.println(s"bytes2: ${String(bytes2, StandardCharsets.UTF_8)}")
          _      <- write(file3, bytes1 ++ bytes2)
          bytes3 <- read(file4)
          _      <- IO.println(s"Third file is now:\n${String(bytes3, StandardCharsets.UTF_8)}")
        } yield ()
      }

    val actual = program.unsafeRunSync()
    assertEquals(actual, ())
  }
}
