import cats.effect.{ExitCode, IO, Resource}
import cats.syntax.all.*
import java.io.*
import cats.effect.unsafe.implicits.global

class ResourceFileConcatenationParallel extends munit.FunSuite {
  test("Resource is used for acquiring, using and releasing a resource") {
    def transfer(source: InputStream, destination: OutputStream, buffer: Array[Byte], acc: Long): IO[Long] =
      for {
        amount <- IO.blocking(source.read(buffer, 0, buffer.length))
        count <-
          if (amount > -1) IO.blocking(destination.write(buffer, 0, amount)) >> transfer(source, destination, buffer, acc + amount)
          else IO.pure(acc) // End of read stream reached (by java.io.InputStream contract), nothing to write
      } yield count         // Returns the actual amount of bytes transferred

    def inputStream(f: File): Resource[IO, FileInputStream] =
      Resource.fromAutoCloseable(IO.blocking(new FileInputStream(f)))

    def outputStream(f: File): Resource[IO, FileOutputStream] =
      Resource.fromAutoCloseable(IO.blocking(new FileOutputStream(f)))

    def inputOutputStreams(ins: List[File], out: File): Resource[IO, (List[InputStream], OutputStream)] =
      for {
        inStreams <- ins.traverse(inputStream)
        outStream <- outputStream(out)
      } yield (inStreams, outStream)

    def copy(sources: List[File], destination: File): IO[Long] =
      inputOutputStreams(sources, destination)
        .use { case (ins, out) =>
          ins.traverse { in =>
            for {
              count <- transfer(in, out, new Array[Byte](1024 * 10), 0L)
            } yield count
          }
        }
        .map(_.sum)

    def program(args: List[String]): IO[ExitCode] =
      for {
        _ <- IO.raiseWhen(args.length < 2)(new IllegalArgumentException("Need source and destination files"))
        sources = args.init.map { arg =>
          File(getClass().getClassLoader().getResource(arg).getPath())
        }
        dest = new File(args.last)
        count <- copy(sources, dest)
        _     <- IO.println(s"$count bytes copied from ${sources.map(_.getPath())} to ${dest.getPath}")
      } yield ExitCode.Success

    val actual = program(List("file1.txt", "file2.txt", "file3.txt", "data/concatenated.txt")).unsafeRunSync()
    assertEquals(actual, ExitCode.Success)
  }
}
