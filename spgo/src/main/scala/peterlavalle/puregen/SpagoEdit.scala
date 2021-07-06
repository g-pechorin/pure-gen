package peterlavalle.puregen

import java.io.{File, FileWriter, Writer}

import peterlavalle._

class SpagoEdit(
								 projectName: String,
								 workspaceRoot: File,

								 spagoRoot: File
							 ) extends TemplateResource {
	def this(projectName: String,
					 workspaceRoot: File
					) = this(
		projectName,
		workspaceRoot,
		workspaceRoot / projectName / "target/spago"
	)

	def genVsCode(): Unit = {
		bind("tasks") {
			case "name" =>
				projectName
			case "spago" =>
				assume((workspaceRoot / projectName / ".vscode/tasks.json").AbsolutePath == spagoRoot.AbsolutePath, "fix pathing")
				s"$projectName/.vscode/tasks.json"
		}.foldLeft(new FileWriter(workspaceRoot / projectName / ".vscode/tasks.json"): Writer)(_ append _ append "\n")
			.close()
	}
}
