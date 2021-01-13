package peterlavalle

import javax.swing.JTree
import javax.swing.event.{TreeExpansionEvent, TreeExpansionListener}
import javax.swing.tree.{TreeNode, TreePath}

trait PiJTreeT {


	implicit class PiJTree2(tree: JTree) {

		def expand(path: Iterable[TreeNode]): Unit =
			tree.expandPath(
				new TreePath(path.toArray[Object])
			)

		def onExpand(act: List[TreeNode] => Unit): AutoCloseable = {
			expansion(
				new ExpansionListener {
					override def expand(path: List[TreeNode]): Unit = act(path)

					override def collapse(path: List[TreeNode]): Unit = {}
				}
			)
		}

		def onCollapse(act: List[TreeNode] => Unit): AutoCloseable = {
			expansion(
				new ExpansionListener {
					override def expand(path: List[TreeNode]): Unit = {}

					override def collapse(path: List[TreeNode]): Unit = act(path)
				}
			)
		}

		private def expansion(listener: ExpansionListener): AutoCloseable = {
			val expansionListener: TreeExpansionListener =
				new TreeExpansionListener {


					override def treeExpanded(treeExpansionEvent: TreeExpansionEvent): Unit = {
						listener.expand(
							treeExpansionEvent.getPath.getPath
								.toList.map(_.asInstanceOf[TreeNode])
						)
					}

					override def treeCollapsed(treeExpansionEvent: TreeExpansionEvent): Unit =
						listener.collapse(
							treeExpansionEvent.getPath.getPath
								.toList.map(_.asInstanceOf[TreeNode])
						)
				}

			tree.addTreeExpansionListener(
				expansionListener
			)

			() =>
				tree.removeTreeExpansionListener(expansionListener)
		}

		private trait ExpansionListener {

			def expand(path: List[TreeNode]): Unit

			def collapse(path: List[TreeNode]): Unit

		}
	}


}
