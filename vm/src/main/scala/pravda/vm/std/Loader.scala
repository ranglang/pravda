package pravda.vm

package std

import pravda.common.domain.Address
import state.Environment

object Loader extends Loader {

  val libraries: Seq[NativeLibrary] = Array(
    libs.Math,
    libs.Typed
  )

  private lazy val libsTable = libraries.map(l => l.address -> l).toMap

  override def lib(address: Address, worldState: Environment): Option[NativeLibrary] =
    libsTable.get(address.toStringUtf8)

}
