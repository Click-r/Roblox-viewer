Set shell = CreateObject("WScript.Shell")
Set fso = CreateObject("Scripting.FileSystemObject")

Dim project_path
project_path = fso.GetParentFolderName(WScript.ScriptFullName) 'aka the current directory
project_path = Mid(project_path, 1, Len(project_path) - 5)

desktop_path = Shell.SpecialFolders("Desktop") 'get desktop directory
Set link = Shell.CreateShortcut(desktop_path & "\RBLXInfoViewer.lnk")

link.TargetPath = "%JAVA_HOME%\bin\javaw.exe"
link.Arguments = "-jar -Dfile.encoding=UTF-8 " & project_path & "\RBLXInfoViewer.jar" 'make sure to set encoding to UTF-8
link.WorkingDirectory = project_path
link.Save

shell.Popup "Successfully created desktop shortcut!", 3.6
