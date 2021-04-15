package peterlavalle

import java.util

object Error {
	private val toDone = new util.HashSet[String]()
}

trait Error {

	def TODO(message: String): Unit = {

		val frame: String =
			Thread.currentThread()
				.getStackTrace
				.toList
				.tail
				.filterNot((_: StackTraceElement).getMethodName.matches("TODO\\$?")).head.toString()

		val text: String =
			if (null == message)
				frame
			else
				frame + "\n\t" + message

		Error.toDone.synchronized {
			if (Error.toDone.add(text))
				System.err.println(text)
		}
	}

	def ??? : Nothing = {
		val error = new NotImplementedError()
		error.setStackTrace {
			error.getStackTrace
				.dropWhile((_: StackTraceElement).getMethodName.matches("\\$qmark\\$qmark\\$qmark\\$?"))
		}
		throw error
	}

	def require(requirement: Boolean, message: Any = "failed"): Unit = if (!requirement) {
		val error = new RuntimeException(message.toString)

		error.setStackTrace(
			error.getStackTrace
				.dropWhile {
					frame: StackTraceElement =>
						"require" == frame.getMethodName || "require$" == frame.getMethodName
				}
				.toList
				.filterNext {
					(l: StackTraceElement, r: StackTraceElement) =>
						l.getFileName != r.getFileName || l.getLineNumber != r.getLineNumber
				}.toArray
		)

		throw error
	}

	def raise(throwable: Throwable): Nothing = {
		System.out.flush()
		System.err.flush()
		throwable.printStackTrace(System.err)
		System.err.flush()
		throw throwable
	}

	def expect(requirement: Boolean, message: Any = "unexpected"): Boolean = {
		if (!requirement) {
			val error = new RuntimeException(message.toString)

			error.setStackTrace(
				error.getStackTrace
					.dropWhile {
						frame: StackTraceElement =>
							"expect" == frame.getMethodName || "expect$" == frame.getMethodName
					}
					.toList
					.filterNext {
						(l: StackTraceElement, r: StackTraceElement) =>
							l.getFileName != r.getFileName || l.getLineNumber != r.getLineNumber
					}
					.toArray
					.take(8)
			)

			System.out.flush()
			System.err.flush()
			error.printStackTrace(System.err)
			System.err.flush()
		}
		requirement
	}

	def error(message: Any): Nothing = {
		val error = new RuntimeException(message.toString)

		error.setStackTrace(
			error.getStackTrace
				.dropWhile {
					frame: StackTraceElement =>
						"error" == frame.getMethodName || "error$" == frame.getMethodName
				}
				.toList
				.filterNext {
					(l: StackTraceElement, r: StackTraceElement) =>
						l.getFileName != r.getFileName || l.getLineNumber != r.getLineNumber
				}.toArray
		)
		error.printStackTrace(System.err)

		throw error
	}
}
