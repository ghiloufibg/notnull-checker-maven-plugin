def log = new File(basedir, "build.log").text

assert log.contains("Local null assignment without @NotNull: s in App.java at (line 5,col 16)-(line 5,col 23)")
assert log.contains("Parameter without @NotNull: args in (line 4,col 29)-(line 4,col 41) at App.java")

println "✔ Verification OK : le build a echoue pour la bonne raison."
