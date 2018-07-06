package pravda.dotnet.translation.opcode
import pravda.dotnet.data.TablesData.{MemberRefData, TypeRefData}
import pravda.dotnet.parsers.CIL
import pravda.dotnet.parsers.CIL._
import pravda.dotnet.translation.data.{MethodTranslationCtx, TranslationError, UnknownOpcode}
import pravda.vm.{Data, Opcodes}
import pravda.vm.asm.Operation

case object StringTranslation extends OpcodeTranslator {
  override def deltaOffset(op: CIL.Op, ctx: MethodTranslationCtx): Either[TranslationError, Int] = op match {
    case LdStr(s)                                                                    => Right(1)
    case Call(MemberRefData(TypeRefData(_, "String", "System"), "Concat", _))        => Right(-1)
    case CallVirt(MemberRefData(TypeRefData(_, "String", "System"), "get_Chars", _)) => Right(-1)
    case CallVirt(MemberRefData(TypeRefData(_, "String", "System"), "Substring", _)) => Right(-2)
    case _                                                                           => Left(UnknownOpcode)
  }

  override def translate(op: CIL.Op,
                         stackOffsetO: Option[Int],
                         ctx: MethodTranslationCtx): Either[TranslationError, List[Operation]] = op match {
    case LdStr(s) =>
      Right(List(Operation.Push(Data.Primitive.Utf8(s))))
    case Call(MemberRefData(TypeRefData(_, "String", "System"), "Concat", _)) =>
      Right(List(Operation(Opcodes.CONCAT)))
    case CallVirt(MemberRefData(TypeRefData(_, "String", "System"), "get_Chars", _)) =>
      Right(List(Operation(Opcodes.ARRAY_GET)))
    case CallVirt(MemberRefData(TypeRefData(_, "String", "System"), "Substring", signatureIdx)) => // FIXME more accurate method detection
      Right(
        List(Operation.Push(Data.Primitive.Int32(2)),
             Operation(Opcodes.DUPN),
             Operation(Opcodes.ADD),
             Operation(Opcodes.SLICE)))
    case _ => Left(UnknownOpcode)
  }
}