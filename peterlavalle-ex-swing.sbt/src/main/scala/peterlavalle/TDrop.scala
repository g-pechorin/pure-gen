package peterlavalle

import java.awt.datatransfer.DataFlavor

import javax.swing.tree.TreePath
import javax.swing.{JTree, TransferHandler}

trait TDrop {

	def canDrop(support: TransferHandler.TransferSupport): Option[() => Boolean]

	def unpackTransfer(support: TransferHandler.TransferSupport)(mimeType: String => Boolean): Option[() => AnyRef] = {

		// find a data flavour
		val found: Option[DataFlavor] =
			support.getDataFlavors
				.toList
				.find {
					data: DataFlavor =>
						mimeType(data.getMimeType)
				}

		found.map {
			flavour: DataFlavor =>
				() => {
					support.getTransferable.getTransferData(flavour)
				}
		}
	}
}

object TDrop {
	def tree(tree: JTree): Unit = {
		tree.setTransferHandler(
			new TransferHandler() {

				private def getDrop(support: TransferHandler.TransferSupport): Option[() => Boolean] = {
					val path: TreePath = tree
						.getPathForLocation(
							support.getDropLocation.getDropPoint.x,
							support.getDropLocation.getDropPoint.y
						)
					if (null == path)
						None
					else
						path
							.getLastPathComponent match {
							case drop: TDrop =>
								drop.canDrop(support)
							case w =>
								println(w)
								None
						}
				}

				override def importData(support: TransferHandler.TransferSupport): Boolean =
					getDrop(support)
						.exists((_: () => Boolean).apply())


				override def canImport(support: TransferHandler.TransferSupport): Boolean =
					getDrop(support).nonEmpty
			}
		)
	}
}
