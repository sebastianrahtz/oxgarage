<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"  %>
<%@ page import="java.io.*"  %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="content-type" content="text/html; charset=UTF-8"/>

<link rel="icon" href="images/favicon.ico" type="image/x-icon" />
<link rel="shortcut icon" href="images/favicon.ico" type="image/x-icon" />
<meta name="author" content="University of Oxford" />

<title>OxGarage</title>

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
<script type="text/javascript" src="js/jquery.geturlparam.js"></script>
<script type="text/javascript" src="js/ui/ui.core.js"></script>
<script type="text/javascript" src="js/thickbox/thickbox.js"></script>
<!-- thanks to
	http://www.webgeekly.com/tutorials/jquery/how-to-make-your-site-multilingual-using-xml-and-jquery/ -->

<script type="text/javascript" language="javascript">
 $(function() {
    $('a.helpLink').each(function()
          { 
      	  this.href = this.href.replace("helpFiles", 
          "helpFiles-" + language);
     });
    $.ajax({
        url: 'i18n.xml',
        success: function(xml) {
            $(xml).find('translation').each(function(){
                var id = $(this).attr('id');
                var text = $(this).find(language).text();
                $("#" + id).html(text);
            });
        }
    });
});
</script>
</head>
<body>
<script type="text/javascript" language="javascript">
    var language = $(document).getUrlParam("lang");
    if (language == null) { language='en'; }
</script>
<div id="banner" style="background-color:#2277BB;">
                  <img src="images/banner.jpg" 
                    alt="Text Encoding Initiative logo and banner"/>
</div>
<h1 id='lang_title'>OxGarage Document Conversion</h1>
<div id="content">
	<noscript>Please turn the JavaScript on in your browser and refresh the page in order to proceed.</noscript>
	<div id="errorBox" class="errorBox">
		<p id="errorMessage"></p>
	</div>
	<div id="convValidationResult" class="validationResult">
	</div>
	<div id="message">
		<p id="lang_working">working, please wait</p>
	</div>
	<div id="conversionFormDiv" style="display:none;">
		
		<div id="pathOptionsDiv"></div>
		<form id="conversionForm" method="post" name="conversionForm" enctype="multipart/form-data">
			<div id="fileInput">
				<span><strong id="lang_selectfile">Select file to convert</strong>:<a href="helpFiles/convert.html?height=300&amp;with=450" title="Help" class="thickbox helpLink">?</a> <br /><br /><br/>
				<input type="file" id="fileToConvert" name="fileToConvert" /><br /><br/>
				<a href="#" id="showMore">+ <span id="lang_advanced">Show advanced options</span></a><a href="helpFiles/moreOptions.html?height=300&amp;with=450" title="Help" class="thickbox helpLink">?</a></span>
			</div>
			<div id="imagesInput">
				<strong id="lang_uploadimages">Upload images</strong>:<a href="helpFiles/images.html?height=300&amp;with=450" title="Help" class="thickbox helpLink">?</a> <br/>
				<span id="lang_upload2">You can upload image files and .zip files containing images</span> <br/><br/>
				<div id="imageFields">						
					<span id="image0">
						<input type="file" id="imageField_0" name="imageField_0" />
						<a href="#" class="remove" id="removeField_0">- Remove</a><br />
					</span>
				</div>
				<a href="#" id="addImages">+ <span id="lang_moreimages">Add more images</span></a>
			</div>
			<div id="moreOptions"></div>
			<div id="send">
				<input type="button" id="sendFile" value="Convert" onclick="javascript:convertFile();" />
				<input type="reset" id="reset" value="Reset" onclick="javascript:resetForm();" />
			</div>
		</form>
	</div>
	<div class="divContent" id="conversionsListContainer">
		<p><strong><span  id="lang_convertfrom">Convert
		from</span>:<a href="helpFiles/convertFrom.html?height=300&amp;with=450" title="Help" class="thickbox helpLink">?</a></strong></p>
		<ul id="conversionsList">
		</ul>
	</div>
	<div class="divContent" id="conversionsPathsListContainer" style="display:none;">
		<p><strong><span  id="lang_convertto">Convert
		to</span>:<a href="helpFiles/convertTo.html?height=300&amp;with=450" title="Help" class="thickbox helpLink">?</a></strong></p>
		<ul id="conversionsPathsList">
		</ul>
	</div>
</div>
<div id="footer" >
<a href="http://www.oucs.ox.ac.uk/oxgarage/">About OxGarage</a> | <a href="mailto:oxgarage@oucs.ox.ac.uk">Feedback</a>
 </div> 
</body>
</html>
