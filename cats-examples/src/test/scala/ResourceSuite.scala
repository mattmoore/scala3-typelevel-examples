import cats.effect.{ExitCode, IO, Resource}
import java.io.*
import cats.effect.unsafe.implicits.global

class ResourceSuite extends munit.FunSuite {
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

    def inputOutputStreams(in: File, out: File): Resource[IO, (InputStream, OutputStream)] =
      for {
        inStream  <- inputStream(in)
        outStream <- outputStream(out)
      } yield (inStream, outStream)

    def copy(source: File, destination: File): IO[Long] =
      inputOutputStreams(source, destination).use { case (in, out) =>
        transfer(in, out, new Array[Byte](1024 * 10), 0L)
      }

    def program(args: List[String]): IO[ExitCode] =
      for {
        _ <- IO.raiseWhen(args.length < 2)(new IllegalArgumentException("Need source and destination files"))
        _ <- IO.println(File(".").getCanonicalFile())
        source = new File(getClass().getClassLoader().getResource(args.head).getPath())
        dest   = new File(args.tail.head)
        count <- copy(source, dest)
        _     <- IO.println(s"$count bytes copied from ${source.getPath} to ${dest.getPath}")
      } yield ExitCode.Success

    val actual = program(List("file1.txt", "data/concatenated.txt")).unsafeRunSync()
    assertEquals(actual, ExitCode.Success)
  }
}
