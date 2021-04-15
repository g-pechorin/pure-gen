package peterlavalle

import java.awt.event.ActionEvent

import javax.accessibility.Accessible
import javax.swing._

import scala.reflect.ClassTag

object PiSwingT extends PiSwingT

trait PiSwingT {

	implicit class PiJMenuItem[I <: JMenuItem](item: I) {
		def onAction(act: ActionEvent => Unit): I = {
			item.addActionListener(act(_: ActionEvent))
			item
		}
	}

	def SwingVal[T: ClassTag](thing: => T): T = {
		lazy val solved: T = thing

		if (SwingUtilities.isEventDispatchThread)
			solved
		else {
			SwingUtilities.invokeAndWait(
				() => solved
			)
			solved
		}
	}

	implicit class PrimpJPopupMenu(val menu: JPopupMenu) extends PrimpMenuLike[JPopupMenu] {
		override protected val add: (JPopupMenu, JMenuItem) => Unit = (_: JPopupMenu) add (_: JMenuItem)
	}


	implicit class PrimpJMenu(val menu: JMenu) extends PrimpMenuLike[JMenu] {
		override protected val add: (JMenu, JMenuItem) => Unit = (_: JMenu) add (_: JMenuItem)
	}

	implicit class ExtJMenuBar[J <: JMenuBar](val menu: J) extends PrimpMenuLike[J] {
		override protected val add: (JMenuBar, JMenuItem) => Unit = (_: JMenuBar) add (_: JMenuItem)
	}

	implicit class PiJMenuItemString(text: String) {
		def menuAction(act: => Unit): JMenuItem = {
			val menuItem = new JMenuItem(text)
			menuItem.addActionListener((_: ActionEvent) => act)
			menuItem
		}
	}

	def SwingNot(thing: => Unit): Unit =
		if (!SwingUtilities.isEventDispatchThread)
			thing
		else
			new Thread {
				override def run(): Unit = {
					thing
				}
			}.start()

	def SwingNow(thing: => Unit): Unit =
		if (SwingUtilities.isEventDispatchThread)
			thing
		else
			SwingUtilities.invokeAndWait(
				() =>
					thing
			)

	sealed trait PrimpMenuLike[M <: JComponent with Accessible with MenuElement] {
		protected val menu: M

		protected val add: (M, JMenuItem) => Unit

		def item(text: String)(action: => Unit): M = {
			val item = new JMenuItem(text)
			item.addActionListener((_: ActionEvent) => action)
			add(menu, item)
			menu.asInstanceOf[M]
		}

		def menu(text: String)(action: JMenu => Unit): M = {
			val item = new JMenu(text)
			add(menu, item)
			action(item)
			menu.asInstanceOf[M]
		}

		def edit(action: M => Unit): M = {
			action(menu)
			menu.asInstanceOf[M]
		}

	}

}
