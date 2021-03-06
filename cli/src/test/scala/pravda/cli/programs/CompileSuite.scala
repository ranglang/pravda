package pravda.cli.programs

import cats.Id
import com.google.protobuf.ByteString
import pravda.cli.PravdaConfig
import pravda.cli.PravdaConfig.CompileMode
import pravda.node.client.{CompilersLanguage, IoLanguageStub, IpfsLanguageStub, MetadataLanguageStub}
import pravda.vm.Meta
import pravda.vm.asm.Operation
import utest._

object CompileSuite extends TestSuite {

  import CompileMode._

  final val UnexpectedBinaryOutput = ByteString.EMPTY
  final val UnexpectedStringOutput = ""

  final val StringSource = "source"
  final val BinarySource = ByteString.copyFromUtf8(StringSource)
  final val ExpectedStringOutput = "expected output"
  final val ExpectedBinaryOutput = ByteString.copyFromUtf8(ExpectedStringOutput)

  val tests: Tests = Tests {
    "asm" - {
      val io = new IoLanguageStub(Some(BinarySource))
      val compilers = new CompilersLanguage[Id] {
        def asm(source: String): Id[Either[String, ByteString]] =
          if (source == StringSource) Right(ExpectedBinaryOutput)
          else Right(UnexpectedBinaryOutput)
        def asm(fileName: String, source: String): Id[Either[String, ByteString]] =
          Left("nope")
        def disasm(source: ByteString): Id[String] =
          UnexpectedStringOutput
        def dotnet(sources: Seq[(ByteString, Option[ByteString])],
                   mainClass: Option[String]): Id[Either[String, ByteString]] = Right(UnexpectedBinaryOutput)
        def disasmToOps(source: ByteString): Id[Seq[(Int, Operation)]] = Nil

        def evm(source: ByteString, abi: ByteString): Id[Either[String, ByteString]] = ???
        def disasm(source: ByteString, metas: Map[Int, Seq[Meta]]): Id[String] = ???
        def disasmToOps(source: ByteString, metas: Map[Int, Seq[Meta]]): Id[Seq[(Int, Operation)]] = ???
      }
      val metadata = new MetadataLanguageStub[Id]()
      val ipfs = new IpfsLanguageStub[Id]()
      val compile = new Compile[Id](io, compilers, ipfs, metadata)
      compile(PravdaConfig.Compile(Asm))
      assert(io.stdout.headOption.contains(ExpectedBinaryOutput))
    }
    "disasm" - {
      val io = new IoLanguageStub(Some(BinarySource))
      val compilers = new CompilersLanguage[Id] {
        def asm(fileName: String, source: String): Id[Either[String, ByteString]] =
          Right(UnexpectedBinaryOutput)
        def asm(source: String): Id[Either[String, ByteString]] =
          Right(UnexpectedBinaryOutput)
        def disasm(source: ByteString): Id[String] =
          if (source == BinarySource) ExpectedStringOutput
          else UnexpectedStringOutput
        def dotnet(sources: Seq[(ByteString, Option[ByteString])],
                   mainClass: Option[String]): Id[Either[String, ByteString]] = Right(UnexpectedBinaryOutput)
        def disasmToOps(source: ByteString): Id[Seq[(Int, Operation)]] = Nil

        def evm(source: ByteString, abi: ByteString): Id[Either[String, ByteString]] = Right(UnexpectedBinaryOutput)
        def disasm(source: ByteString, metas: Map[Int, Seq[Meta]]): Id[String] =
          if (source == BinarySource) ExpectedStringOutput
          else UnexpectedStringOutput
        def disasmToOps(source: ByteString, metas: Map[Int, Seq[Meta]]): Id[Seq[(Int, Operation)]] = Nil
      }
      val metadata = new MetadataLanguageStub[Id]()
      val ipfs = new IpfsLanguageStub[Id]()
      val compile = new Compile[Id](io, compilers, ipfs, metadata)
      compile(PravdaConfig.Compile(Disasm))
      assert(io.stdout.headOption.contains(ExpectedBinaryOutput))
    }

    "dotnet" - {
      val io = new IoLanguageStub(Some(BinarySource))
      val compilers = new CompilersLanguage[Id] {
        def asm(fileName: String, source: String): Id[Either[String, ByteString]] =
          Right(UnexpectedBinaryOutput)
        def asm(source: String): Id[Either[String, ByteString]] =
          Right(UnexpectedBinaryOutput)
        def disasm(source: ByteString): Id[String] =
          UnexpectedStringOutput
        def dotnet(sources: Seq[(ByteString, Option[ByteString])],
                   mainClass: Option[String]): Id[Either[String, ByteString]] = {
          if (sources.headOption.map(_._1).contains(BinarySource)) Right(ExpectedBinaryOutput)
          else Right(UnexpectedBinaryOutput)
        }
        def disasmToOps(source: ByteString): Id[Seq[(Int, Operation)]] = Nil
        def evm(source: ByteString, abi: ByteString): Id[Either[String, ByteString]] = Right(UnexpectedBinaryOutput)
        def disasm(source: ByteString, metas: Map[Int, Seq[Meta]]): Id[String] = ???
        def disasmToOps(source: ByteString, metas: Map[Int, Seq[Meta]]): Id[Seq[(Int, Operation)]] = ???
      }
      val metadata = new MetadataLanguageStub[Id]()
      val ipfs = new IpfsLanguageStub[Id]()
      val compile = new Compile[Id](io, compilers, ipfs, metadata)
      compile(PravdaConfig.Compile(DotNet))
      assert(io.stdout.headOption.contains(ExpectedBinaryOutput))
    }
  }
}
