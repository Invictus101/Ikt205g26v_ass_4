Assignment 4 

Hvordan kjøre prosjektet 
1. Åpne Android Studio.
2. Finn mappen "assignment_4" og åpne den.
3. Vent til Gradle Sync er ferdig.
4. Start en emulator eller koble til en Android-enhet



Testing
Prosjektet inneholder både vanlige unit tester og instrumenterte UI-tester.

Før testing i PowerShell
Bruk disse kommandoene først for å sette riktig Java-versjon i terminalen:

$env:JAVA_HOME="C:\Users\david\AppData\Local\Programs\Android Studio\jbr"
$env:Path="$env:JAVA_HOME\bin;$env:Path"


Kjør unit tester:
.\gradlew.bat clean testDebugUnitTest


Kjør instrumenterte UI-tester:
.\gradlew.bat connectedDebugAndroidTest


Se testresultater
Get-ChildItem .\app\build\reports\androidTests\ -Recurse
