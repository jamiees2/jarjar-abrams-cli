import java.io.{BufferedInputStream, InputStream}
import collection.mutable.ArrayBuffer

object Streamable {

  /** Traits which can be viewed as a sequence of bytes.  Source types
    *  which know their length should override def length: Long for more
    *  efficient method implementations.
    */
  trait Bytes {
    def inputStream(): InputStream
    def length: Long = -1

    def bufferedInput() = new BufferedInputStream(inputStream())
    def bytes(): Iterator[Byte] = bytesAsInts().map(_.toByte)
    def bytesAsInts(): Iterator[Int] = {
      val in = bufferedInput()
      Iterator.continually(in.read()).takeWhile(_ != -1)
    }

    /** This method aspires to be the fastest way to read
      *  a stream of known length into memory.
      */
    def toByteArray(): Array[Byte] = {
      // if we don't know the length, fall back on relative inefficiency
      if (length == -1L)
        return (new ArrayBuffer[Byte]() ++= bytes()).toArray

      val arr = new Array[Byte](length.toInt)
      val len = arr.length
      lazy val in = bufferedInput()
      var offset = 0

      def loop(): Unit = {
        if (offset < len) {
          val read = in.read(arr, offset, len - offset)
          if (read >= 0) {
            offset += read
            loop()
          }
        }
      }
      try loop()
      finally in.close()

      if (offset == arr.length) arr
      else throw new RuntimeException("Could not read entire source (%d of %d bytes)".format(offset, len))
    }
  }
  def bytes(is: => InputStream): Array[Byte] =
    new Bytes { def inputStream() = is }.toByteArray
}
