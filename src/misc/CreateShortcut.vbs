set shell = CreateObject("WScript.Shell")
set fso = CreateObject("Scripting.FileSystemObject")

dim project_path
project_path = fso.GetParentFolderName(WScript.ScriptFullName) 'aka the current directory
project_path = Mid(project_path, 1, Len(project_path) - 5)

desktop_path = Shell.SpecialFolders("Desktop") 'get desktop directory
set link = Shell.CreateShortcut(desktop_path & "\RBLXInfoViewer.lnk")

dim jarName

for each objFile in fso.getFolder(project_path).Files
    if Lcase(fso.getExtensionName(objFile.Name)) = "jar" then 'find file with the .jar extension
        jarName = objFile.Name
    end if
next

link.TargetPath = "javaw"
link.Arguments = "-jar -Dfile.encoding=UTF-8 " & project_path & "\" & jarName 'make sure to set encoding to UTF-8
link.WorkingDirectory = project_path
link.Save

shell.Popup "Successfully created desktop shortcut!", 3.6
