<%@ page language="java" contentType="text/html; charset=UTF-8"
pageEncoding="UTF-8" import="java.io.*;"  %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="content-type" content="text/html; charset=UTF-8"/>

<link rel="icon" href="images/favicon.ico" type="image/x-icon" />
<link rel="shortcut icon" href="images/favicon.ico" type="image/x-icon" />
<meta name="author" content="University of Oxford" />

<title>OxGarage Document Conversion</title>

<link rel="stylesheet" href="js/thickbox/thickbox.css" type="text/css" media="screen" />

<link type="text/css" href="js/themes/custom-theme/ui.all.css" rel="stylesheet" />

<link rel="stylesheet" type="text/css" media="screen" href="style/oucs.css" />

<link rel="stylesheet" type="text/css" media="print" href="style/cssp_print.css" />

<link rel="stylesheet" type="text/css" media="screen" href="style/main.css" />

<meta name="viewport" content="width=device-width, initial-scale=1" />

<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<meta http-equiv="Content-Language" content="en-gb" />
<meta http-equiv="Content-Script-Type" content="text/javascript" />

<script type="text/javascript">   
	var homePageUrl = <%
		String homePage = (String)application.getInitParameter("webservice.url");
		out.print("'"+homePage+"';");
	%>
	var defaultUrl = <% 
		String defaultPage;
		String port;
		if(request.isSecure()){
			defaultPage = "https://";
		}else{
			defaultPage = "http://";
		}
		port = String.valueOf(request.getServerPort());
		defaultPage = defaultPage + request.getServerName() + ":" + port + "/ege-webservice/";
		out.print("'"+defaultPage+"';");
	%>
</script>
<script type="text/javascript" src="js/jquery.js"></script>
<script type="text/javascript" src="js/jquery.form.js"></script>
<script type="text/javascript" src="js/ege-js-client.js"></script>
<script type="text/javascript" src="js/ege-js-validation.js"></script>
<script type="text/javascript" src="js/ui/ui.core.js"></script>
<script type="text/javascript" src="js/thickbox/thickbox.js"></script>
</head>
<body>

<div id="hdr"><div id="headerOuter"><div id="upperBarOuter"><div id="globalNavOuter"><a href="http://www.oucs.ox.ac.uk/">OUCS</a> | <a href="http://www.oucs.ox.ac.uk/about/contact.xml">Contact</a> | <a href="http://www.oucs.ox.ac.uk/atoz/">A to Z</a> | <a href="http://www.oucs.ox.ac.uk/help/">Help</a> | <a href="http://status.ox.ac.uk/">Status</a> | <a href="http://www.ict.ox.ac.uk/oxford/rules/">Rules</a> | <a href="http://www.ox.ac.uk">Oxford University</a></div><div id="searchOuter"><div id="searchInner"><div class="searchbox"><a name="search"></a><form method="get" id="searchform" action="http://googlesearch.oucs.ox.ac.uk/search"><fieldset><legend>Search</legend><input type="hidden" value="OUCS" name="Unit"/><input type="hidden" value="http://www.oucs.ox.ac.uk/googlesearch/radcliffe.jpg" name="UnitPicture" /><input type="hidden" value="oucs" name="client" /><input type="hidden" value="oucs" name="proxystylesheet" /><input type="hidden" value="xml_no_dtd" name="output" /><input type="hidden" value="1" name="filter" /><span class="input"><input type="text" value="search OUCS" title="search box" size="15" name="q" maxlength="2048" id="input-search" class="cleardefault" />&nbsp; <input type="submit" value="Go!" name="Go" class="gobutton" /><br /></span><input type="hidden" value="oucs" name="site" id="oucs" /></fieldset></form></div></div></div><h1>Oxford University Computing Services</h1></div><div id="lowerBarOuter"><div id="lowerBarInner"><script type="text/javascript">
var d=new Date()
var weekday=new Array("Sunday","Monday","Tuesday","Wednesday","Thursday","Friday","Saturday")
var monthname=new Array("Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec")
document.write(weekday[d.getDay()] + " ")
document.write(d.getDate() + ". ")
document.write(monthname[d.getMonth()] + " ")
document.write(d.getFullYear())
</script></div></div><div id="logoOuter"></div></div></div>
<h1>OxGarage Document Conversion</h1>
<div id="content">
	<noscript>Please turn the JavaScript on in your browser and refresh the page in order to proceed.</noscript>
	<div id="errorBox" class="errorBox">
		<p id="errorMessage"></p>
	</div>
	<div id="convValidationResult" class="validationResult">
	</div>
	<div id="message">
		<p>...Working, please wait...</p>
	</div>
	<div id="conversionFormDiv" style="display:none;">
		
		<div id="pathOptionsDiv"></div>
		<form id="conversionForm" method="post" name="conversionForm" enctype="multipart/form-data">
			<div id="fileInput">
				<span><strong>Select file to convert:</strong><a href="helpFiles/convert.html?height=300&amp;with=450" title="Help" class="thickbox helpLink">?</a> <br /><br /><br/>
				<input type="file" id="fileToConvert" name="fileToConvert" /><br /><br/>
				<a href="#" id="showMore">+ Show advanced options</a><a href="helpFiles/moreOptions.html?height=300&amp;with=450" title="Help" class="thickbox helpLink">?</a></span>
			</div>
			<div id="imagesInput">
				<strong>Upload images:</strong><a href="helpFiles/images.html?height=300&amp;with=450" title="Help" class="thickbox helpLink">?</a> <br/>
				You can upload image files and .zip files containing images. <br/><br/>
				<div id="imageFields">						
					<span id="image0">
						<input type="file" id="imageField_0" name="imageField_0" />
						<a href="#" class="remove" id="removeField_0">- Remove</a><br />
					</span>
				</div>
				<a href="#" id="addImages">+ Add more images</a>
			</div>
			<div id="moreOptions"></div>
			<div id="send">
				<input type="button" id="sendFile" value="Convert" onclick="javascript:convertFile();" />
				<input type="reset" id="reset" value="Reset" onclick="javascript:resetForm();" />
			</div>
		</form>
	</div>
	<div class="divContent" id="conversionsListContainer">
		<p><strong>Convert from:<a href="helpFiles/convertFrom.html?height=300&amp;with=450" title="Help" class="thickbox helpLink">?</a></strong></p>
		<ul id="conversionsList">
		</ul>
	</div>
	<div class="divContent" id="conversionsPathsListContainer" style="display:none;">
		<p><strong>Convert to:<a href="helpFiles/convertTo.html?height=300&amp;with=450" title="Help" class="thickbox helpLink">?</a></strong></p>
		<ul id="conversionsPathsList">
		</ul>
	</div>
</div>
<div id="footer" >
<a href="http://www.oucs.ox.ac.uk/oxgarage/">About OxGarage</a> | <a href="mailto:oxgarage@oucs.ox.ac.uk">Feedback</a>
 </div> 
</body>
</html>
