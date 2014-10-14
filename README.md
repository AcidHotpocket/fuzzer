<b>Fuzzer</b>

The fuzzer project for software security: http://www.se.rit.edu/~swen-331/projects/fuzzer/

<b>Project Team:</b>
<ul>
	<li>Zackery Martin - <i>zlm2353@rit.edu<i> <br></li>
	<li>Richard Jester - <i>rej7869@rit.edu<i> <br></li>
	<li>Dennis Liang - <i>dxl1795@rit.edu<i></li>
</ul><br>

<b>Installation</b><br>
<ol>
	<li>Download fuzz.jar from dropbox</li>
	<li>Run program from the terminal, see example usage on how to run it.</li>
</ol>

<b>Project Setup</b><br>
<ul>
	<li>Testing, input discovery, resources is all in the package/jar/zip.</li>
</ul>

<b>Dependencies</b><br>
<ul>
	<li>Java 1.6</li>
	<li>HTMLUnit</li>
</ul>

<b>Example usage</b><br>
You will have to provide your own directory. <br>

java jar fuzz.jar discover http://127.0.0.1/dvwa/login.php --common-words=Z:\Fuzzer\common-words.txt --vectors=Z:\Fuzzer\vectors.txt --sensitive=Z:\Fuzzer\sensitive-data.txt --custom-auth=dvwa<br>


<b>Notes</b><br>
As long as you are on a RIT school computer and follow the manual page that is found in the course website under the fuzzer section, you do not need to download any dependencies.


