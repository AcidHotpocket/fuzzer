<b>Fuzzer</b>

The fabulous fuzzer project for software security: http://www.se.rit.edu/~swen-331/projects/fuzzer/

<b>Project Team:</b>
<ul>
	<li>Zackery Martin - <i>zlm2353@rit.edu<i> <br></li>
	<li>Richard Jester - <i>rej7869@rit.edu<i> <br></li>
	<li>Dennis Liang - <i>dxl1795@rit.edu<i></li>
</ul><br>

<b>Installation</b><br>
<ol>
	<li>Copy this repository to your workspace</li>
	<li>Run program from the command line</li>
</ol>

<b>Project Setup</b><br>
<ul>
	<li>Testing, input discovery, resources is all in the package.</li>
</ul>

<b>Dependencies</b><br>
<ul>
	<li>Java 1.6</li>
	<li>Jsoup</li>
</ul>

<b>Example usage</b><br>

"replace this with an example from the command line"<br>

  java fuzz test http://localhost:8080 --custom-auth=dvwa --common-words=words.txt --vectors=vectors.txt --sensitive=creditcards.txt --random=false<br>
  java fuzz discover http://localhost:8080 --common-words=mywords.txt<br>
  java fuzz discover http://localhost:8080 --common-words=mywords.txt<br>


  

