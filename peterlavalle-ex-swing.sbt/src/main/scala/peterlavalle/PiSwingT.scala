package peterlavalle

import java.awt.event.ActionEvent

import javax.swing.{JMenuItem, SwingUtilities}

import scala.reflect.ClassTag

object PiSwingT extends PiSwingT

trait PiSwingT {

	implicit class PiJMenuItem[I <: JMenuItem](item: I) {
		def onAction(act: ActionEvent => Unit): I = {
			item.addActionListener(act(_: ActionEvent))
			item
		}
	}

	implicit class PiJMenuItemString(text: String) {
		def menuAction(act: => Unit): JMenuItem = {
			val menuItem = new JMenuItem(text)
			menuItem.addActionListener((_: ActionEvent) => act)
			menuItem
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
}
