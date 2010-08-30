/**
 * On DOM ready state : prepare possible input types for conversion.
 */

/**
 * Flag value is checking if defaultUrl is used instead of wrong 
 * configured homePageUrl. defaultUrl value can also give connection
 * error, then utterly error message will be shown.
 */
var usesDefaultUrl = false;


/**
 * Flag value representing the state of the moreOptions div
 */
var infoHidden = true;

/**
 * Array containing list of already selected images, so that images with same filename can be detected.
 */
var uploadedImages = Array();

$().ready(
		function() {
			performAjaxRequest(homePageUrl + "Conversions/", "GET", conversionsListDataHandler, standardErrorHandler);
			$("#moreOptions").hide();
			infoHidden=true;
			$('a#showMore').click(function() {
				$("#sendFile").attr("disabled", "");
				infoHidden = !infoHidden;				
				if (infoHidden) {
					$('a#showMore').empty();
					$('a#showMore').append('+ Show advanced options');
					$("#moreOptions").hide('fast');
				}
				else {
					$('a#showMore').empty();
					$('a#showMore').append('- Hide advanced options');
					$("#moreOptions").show('fast');
				}
			});
			var i=1;
			$('#addImages').click(function() {
				 $('#imageFields').append('<span id="image' + i + '"><input type="file" id="imageField_' + i + '" name="imageField_' + i + '" /><a href="#" class="remove" id="removeField_' + i + '">- Remove</a><br/></span>');
				i = i+1;
			});
			$('.remove').live("click", function() {
				var ids = $(this).attr('id').split('_');
				$('#image' + ids[1]).remove();
				uploadedImages[ids[1]]='';
			});
			$('#imageFields input[type=file]').live("change", function(){
				var inputValue = $(this).val();
				var thisField = $(this).attr("id").split('_')[1];				
				for(var j = 0; j<uploadedImages.length; j++) {  				
					if(j!=thisField && uploadedImages[j].length!==0 && inputValue==uploadedImages[j]) {
						alert("Image with same filename has already been selected. If you proceed, these images might not be converted as expected. Please change the image filenames and their references in document or upload images in a .zip archive. If you upload images in .zip archive, we will be able to deal with images with same filenames properly.");
					}
				}
				uploadedImages[thisField] = inputValue;		
			});
			$('#fileToConvert').change(function() {
				$("#sendFile").attr("disabled", "");
			});
			$('.aDocFamily').live("click", function(){
				resetForm();
				var ids = $(this).attr('id').split('_');
				$('#' + ids[1]).show("fast");
			});
		});

/**
 * Perform parameterised ajax request.
 * 
 * @param url -
 *            address of request
 * @param method -
 *            GET or POST
 * @param dataHandler -
 *            function for handling response data
 * @param errorHandler -
 *            function for handling error response.
 * @return
 */
function performAjaxRequest(url, method, dataHandler, errorHandler) {
	$.ajax( {
		type : method,
		url : url,
		dataType : "xml",
		success : dataHandler,
		error : errorHandler
	});
}

/**
 * Standard error handler of AJAX request.
 * 
 * @param xmlHttpRequest
 * @param txtStatus
 * @param errorThrowed
 * @return
 */
function standardErrorHandler(xmlHttpRequest, txtStatus, errorThrowed) {
	 if((xmlHttpRequest.status == "404" || xmlHttpRequest.status == "0")  && !usesDefaultUrl){
		 homePageUrl = defaultUrl;
		 usesDefaultUrl = true;
		 performAjaxRequest(homePageUrl + "/Conversions/",
					"GET", conversionsListDataHandler, standardErrorHandler);
	 }
	 else{
		 if(xmlHttpRequest.status == "404" || xmlHttpRequest.status == "0"){
			 errorThrowed = "Connection to web service refused (could be wrong address).";
		 }
		 showError(txtStatus + " error: " + errorThrowed + " status:"
			+ xmlHttpRequest.status);
	 }
}

function showError(msg) {
	var errMsg = $("#errorMessage");
	errMsg.append("<img style=\"margin-right:5px;\" src=\"style/img/err.gif\"/><b>Error occured. Please check the filetype and try again.<a href=\"helpFiles/errorHelp.html?height=300&with=450\" title=\"Help\" class=\"thickbox helpLink\">?</a></b><p> "
					+ msg + "</p>");
	 $('.thickbox').each(function(i) {$(this).unbind('click');});
	tb_init('a.thickbox');		
	$("#errorBox").css( {
		'visibility' : 'visible',
		'padding-top' : '6px',
		'padding-bottom' : '6px',
		'height' : '150px'
	});
	$("#message").html("<p>Error occured. Please try again.</p>");
}

function hideError() {
	$("#errorMessage").empty();
	$("#errorBox").css( {
		'visibility' : 'hidden',
		'padding-top' : '0px',
		'padding-bottom' : '0px',
		'height' : '0px'
	});
}
			
var uploadedImages = Array();
var pathsDestinations;
var lastInputSelection;
var lastPathSelection;

/**
 * Handle response of a list of input data types of possible EGE conversions.
 * Response is a XML data.
 * 
 * @param data -
 *            response data.
 * @return
 */	
function conversionsListDataHandler(data) {
	var inputDataTypes = data.getElementsByTagName("input-data-types")[0];
	inputDataTypes = inputDataTypes.getElementsByTagName("input-data-type");
	var sizeOfData = inputDataTypes.length;
	var container = $("#conversionsList");
	if (sizeOfData == 0) {
		$("#conversionListContainer").append("<p> No content </p>");
		return;
	}
	pathsDestination = new Array(sizeOfData);
	var i;
	for (i = 0; i < sizeOfData; i++) {
		var current = inputDataTypes[i];
		var id = current.getAttribute("id");
		pathsDestination[i] = current.getAttribute("xlink:href");
		if (pathsDestination[i] == null) {
			pathsDestination[i] = current.getAttribute("href");
		}
		var descs = id.split(":");
		// divide into document families
		var documentFamily = descs[0].split(" ")[0];
		if(container.children().children("#" + documentFamily).length==0) {
			if(i!=0) container.append("</ul></li>");
			container.append("<li class=\"documentFamilyItem\"><a href=\"#\" class=\"aDocFamily\" id=\"aDocFamily_" + documentFamily + "\"><img src=\"images/" + documentFamily.toLowerCase() + ".png\" /><strong>" + descs[0] + "</strong></a><ul id=\"" + documentFamily + "\" class=\"documentFamily\">");
			$("#" + documentFamily).hide();
		}
		// create link from input type
		container.children().children("#" + documentFamily).append("<li><input type=\"radio\" id=\"pSel_" + i + "\" name=\"inputTypeSel\" /><a href=\"#\" id=\"pSela_" + i + "\" title=\"" + descs[2] + "\">" + descs[1]
				+ "</a></li>");
		// handle click event of link
		$("#pSel_" + i).live(
				"click",
				function() {
					_hideResults();
					hideError();
					resetPathSelection();
					$("#sendFile").attr("disabled", "");
					var id = $(this).attr("id");
					var li = parseInt(id.replace(/pSel_/, ""));
					$("#message").html("<p>...Working, please wait...</p>");
					$('#conversionFormDiv').animate({
						opacity : 0.0
						}, 150, function() {
							$("#conversionFormDiv").css( {
								'display' : 'none'
							});
							var type = $("#pSela_" + li).attr("title").split(',');		
							if(type[1]!="text/xml"){
								$('#imageFields').html("<strong>You do not need to upload any images, as images are included in the document file.");
								$('#addImages').remove();
							}
							else {
								$('#imageFields').html("<span id=\"image0\"><input type=\"file\" id=\"imageField_0\" name=\"imageField_0\" /><a href=\"#\" class=\"remove\" id=\"removeField_0\">- Remove</a><br /></span>");
								if($('#addImages').length==0) {
									$('#imagesInput').append("<a href=\"#\" id=\"addImages\">+ Add more images</a>");
								}
							}
					});
					$("#conversionsPathsListContainer").css({
						'display' : '',
						'opacity' : '0'
					});
					performAjaxRequest(pathsDestination[li], "GET",
							conversionsPathsDataHandler, standardErrorHandler);
				}
		);
		$("#pSela_" + i).live(
				"click",
				function() {
					$("#sendFile").attr("disabled", "");
					_hideResults();
					hideError();
					resetPathSelection();
					var id = $(this).attr("id");
					var li = parseInt(id.replace(/pSela_/, ""));
					$("#message").html("<p>...Working, please wait...</p>");
					$('#conversionFormDiv').animate({
						opacity : 0.0
						}, 150, function() {
							$("#conversionFormDiv").css( {
								'display' : 'none'
							});
							var type = $('#pSela_' + li).attr("title").split(',');
							if(type[1]!="text/xml"){
								$('#imageFields').html("<strong>You do not need to upload any images, as images are included in the document file.");
								$('#addImages').remove();
							}
							else {
								$('#imageFields').html("<span id=\"image0\"><input type=\"file\" id=\"imageField_0\" name=\"imageField_0\" /><a href=\"#\" class=\"remove\" id=\"removeField_0\">- Remove</a><br /></span>");
								if($('#addImages').length==0) {
									$('#imagesInput').append("<a href=\"#\" id=\"addImages\">+ Add more images</a>");
								}
							}
					});
					$("#conversionsPathsListContainer").css({
						'display' : '',
						'opacity' : '0'
					});
					document.getElementById("pSel_"+li).checked = true;
					performAjaxRequest(pathsDestination[li], "GET",
							conversionsPathsDataHandler, standardErrorHandler);
				}
		);
	}
	if(i!=0) container.append("</ul></li>");	
	$("#message").animate({
		opacity : 0.0
		}, 250, function() {
			$("#message").html("<p>Please select the type of the document you want to convert.</p>");
		$("#message").animate({
			opacity : 1.0
			}, 250, function() {});					
		});
}

 

var conversionsPathsURLs;

var selectedPathURL;
var selectedConvPath;
function resetPathSelection() {
	$("#selectedPath").empty();
	selectedPathURL = "";
	selectedConvPath;
}

/**
 * Conversion path JS class
 */
function ConversionPath(url, desc) {
	/*
	 * this conversion path url
	 */
	this.url = url;
	/*
	 * conversion path string name
	 */
	this.desc = desc;
	/*
	 * path vertices - of ConversionAction type
	 */
	this.vertices = new Array();
	
	/*
	 * Translates all vertices properties into UI forms.
	 */
	this.renderProperties = function(containerId){
		$("#"+containerId).empty();
		for(var i = 0 ; i < this.vertices.length; i++){
			var vertex = this.vertices[i];
			var descs = vertex.id.split(":");
			if(vertex.properties.length > 0){
				$("#"+containerId).append("<div id=\"vertOpt"+i+"\" class=\"vertOptions\"> <b>Conversion: " + descs[2] + " -> " + descs[5] + "</b><br /><br /></div>");
				for(j = 0 ; j < vertex.properties.length; j++){
					var prop = vertex.properties[j];
					prop.render("vertOpt"+i);
				}
			}
			else {
				$("#"+containerId).append("<div id=\"vertOpt"+i+"\" class=\"vertOptions\"> <b>Conversion: " + descs[2] + " -> " + descs[5] + "</b><br/><br />There are no more options to specify in this conversion.</div>");
				
			}
		}
		if(infoHidden) $("#"+containerId).hide();
	}
	
	/*
	 * Translates selected path configuration of options
	 * into xml for conversion request.
	 */
	this.readOptionsAsXml = function(){
		var xml = "<conversions>";
		for(var i = 0 ; i < this.vertices.length; i++){
			xml+=("<conversion index=\""+i+"\">");
			for(var j = 0; j < this.vertices[i].properties.length ; j++){
				xml+=("<property id=\""+ this.vertices[i].properties[j].id + "\">");
				xml+=this.vertices[i].properties[j].value;	
				xml+="</property>";
			}
			xml+="</conversion>";
		}
		xml+="</conversions>";
		return xml;
	}
}

/**
 * Conversion action JS class
 * 
 * @param id
 * @return
 */
function ConversionAction(index, id){
	
	this.id = id;
	/*
	* index of conversion action
	*/
	this.index = index; 
	/*
	 * this conversion action (vertex) available properties.
	 * Properties are of ConversionActionProperty type.
	 */
	this.properties = new Array();
	/*
	 * 
	 */
	this.getPropertyById = function(id){
		for(var i = 0; i < this.properties.length; i++){
			if(this.properties[i].id == id){
				return this.properties[i];
			}
		}
		return null;
	}
}

/**
 * Conversion action property JS class 
 * - each describes one option of path vertex.  
 * 
 * @param id - option id 
 * @param definition - definition (returned through xml data)
 * @param type - data type of property
 * @param localName - local label of option.
 * @return
 */
function ConversionActionProperty(id, definition, type, localName, caNr){
	this.id = id;
	this.definition = definition;
	this.type = type;
	this.localName = localName;
	this.value = "";
	this.index = caNr;
	this.render = function(containerId){
		if(this.type == "array"){
			var avail = this.definition.split(",");
			// id with '.' is not accepted 
			id = this.id.replace(/\./g,"_");
			var propsSelect = "<select id=\""+id+"\" name=\"ca_"+this.index+"\">";
			propsSelect = propsSelect.concat("<option value=\""+avail[0]+"\" selected=\"selected\">"+avail[0]+"</option>");
			this.value = avail[0];
			for(var i = 1; i < avail.length ; i++){
				propsSelect = propsSelect.concat("<option value=\""+avail[i]+"\">"+avail[i]+"</option>");
			}
			propsSelect = propsSelect.concat("</select><br />");
			var container = $("#"+containerId);
			container.append(this.localName);
			container.append(propsSelect);
			//handle change of option
			$("select[name='ca_"+this.index + "'][id=\'"+id+"\']").bind("change" , function(){
				$("#sendFile").attr("disabled", "");
				var ind = $(this).attr("name");
				ind = ind.replace(/ca_/,"");
				ind = parseInt(ind);
				// reversing '.' changes
				var prop = selectedConvPath.vertices[ind].getPropertyById($(this).attr("id").replace(/_/g,"."));
				if(prop != null){
					prop.value = this.options[this.selectedIndex].value;
				}
				else{
					showError("Wrong property to ui component assigment!");
				}
				showUrlPath();
			});
		}
		// boolean, which is true or false for all conversions in the whole path
		else if(this.type == "pathBoolean"){
			id = this.id.replace(/\./g,"_");
			var container = $("#"+containerId);
			if(id == "pl_psnc_dl_ege_tei_getOnlineImages" || id == "pl_psnc_dl_ege_tei_getImages") this.value="true";
			else this.value = "false";
			container.append("<input type=\"hidden\" id=\""+id+"\" name=\"ca_"+this.index+"\" value=\"" + this.value + "\" />");
			//handle change of option
			var pathOption = $("input[name='pathOption'][type='checkbox'][id=\'"+id+"\']");
			if(pathOption.length==0) {
				var boxIsChecked="";				
				if (this.value=="true") boxIsChecked = "checked=\"checked\"";				
				container = container.parent();
				container.prepend("<label for=\""+id+"\">" + this.localName + "</label><br />");
				container.prepend("<input type=\"checkbox\" id=\""+id+"\" name=\"pathOption\" " + boxIsChecked + "/>");
				pathOption = $("input[name='pathOption'][type='checkbox'][id=\'"+id+"\']");				
				pathOption.bind("change" , function(){
					$("#sendFile").attr("disabled", "");
					if(id=="pl_psnc_dl_ege_tei_textOnly" && $(this).attr("checked")==true) {
						if ($("#pl_psnc_dl_ege_tei_getOnlineImages").attr("checked")==true) {
							$("#pl_psnc_dl_ege_tei_getOnlineImages").attr("checked", "")	
							$("#pl_psnc_dl_ege_tei_getOnlineImages").trigger('change');
						}
						if ($("#pl_psnc_dl_ege_tei_getImages").attr("checked")==true) {
							$("#pl_psnc_dl_ege_tei_getImages").attr("checked", "")	
							$("#pl_psnc_dl_ege_tei_getImages").trigger('change');
						}
					}
					else if((id=="pl_psnc_dl_ege_tei_getImages" ||
							id=="pl_psnc_dl_ege_tei_getOnlineImages") && $(this).attr("checked")==true) {
						if ($("#pl_psnc_dl_ege_tei_textOnly").attr("checked")==true) {
							$("#pl_psnc_dl_ege_tei_textOnly").attr("checked", "")	
							$("#pl_psnc_dl_ege_tei_textOnly").trigger('change');
						}
					}
					$.each($("input[type='hidden'][id=\'"+id+"\']"), function() {
						var ind = $(this).attr("name");
						ind = ind.replace(/ca_/,"");
						ind = parseInt(ind);
						// reversing '.' changes
						var prop = selectedConvPath.vertices[ind].getPropertyById($(this).attr("id").replace(/_/g,"."));
						if(prop != null){
							if(prop.value=="true") prop.value = "false";
							else prop.value="true";
							$(this).attr("value", prop.value);
						}
						else{
							showError("Wrong property to ui component assigment!");
						}
					});
					showUrlPath();
				});
			}
		}
	};
}

var convPaths;

function getPathByURL(url){
	for(var i = 0; i < convPaths.length; i++){
		if(convPaths[i].url == url){
			return convPaths[i];
		}
	}
	return null;
}

/**
 * Handle response of a list of possible conversions paths. Response is a XML
 * data.
 * 
 * @param data
 * @return
 */
function conversionsPathsDataHandler(data) {
	var conversionsPaths = data.getElementsByTagName("conversions-paths")[0];
	conversionsPaths = conversionsPaths
			.getElementsByTagName("conversions-path");
	var sizeOfData = conversionsPaths.length;
	var container = $("#conversionsPathsList");
	container.empty();
	if (sizeOfData == 0) {
		$("#conversionsPathsListContainer").append("<p> No content </p>");
		return;
	}
	convPaths = new Array(sizeOfData);   
	conversionsPathsURLs = new Array(sizeOfData);
	listOfPaths = new Array(sizeOfData);
	for ( var i = 0; i < sizeOfData; i++) {
		var current = conversionsPaths[i];
		conversionsPathsURLs[i] = current.getAttribute("xlink:href");
		if (conversionsPathsURLs[i] == null) {
			conversionsPathsURLs[i] = current.getAttribute("href");
		}
		var desc = current.getElementsByTagName("path-name")[0].childNodes[0].nodeValue;
		var descs = desc.split(":");
		convPath = new ConversionPath(conversionsPathsURLs[i], desc);
		listOfPaths[i] = "<input type=\"radio\" id=\"idToReplace\" alt=\""
						+ conversionsPathsURLs[i] + "\" name=\"conversionPathSel\" value=\"" + desc + "\" /><a href=\"#\" id=\"id_ToReplace\" title=\"" + descs[descs.length-1] + "\">" + descs[descs.length-2] + "</a>";
		var vertices = current.getElementsByTagName("conversion");
		var conversionActions = new Array(vertices.length);
		for(var j = 0; j < vertices.length; j++){
			var ca = new ConversionAction(parseInt(vertices[j].getAttribute("index")), vertices[j].getAttribute("id"));
			var props = vertices[j].getElementsByTagName("property");
			var properties = new Array(props.length);
			for(var z = 0; z < props.length ; z++){
				properties[z] = new ConversionActionProperty(props[z].getAttribute("id"),
						props[z].getElementsByTagName("value")[0].childNodes[0].nodeValue
					   ,props[z].getElementsByTagName("type")[0].childNodes[0].nodeValue
					   ,props[z].getElementsByTagName("property-name")[0].childNodes[0].nodeValue, j);
			}
			ca.properties = properties;
			conversionActions[ca.index] = ca;
		}
		convPath.vertices = conversionActions;
		convPaths[i] = convPath;
	}
	//listOfPaths.sort();
	for ( var i = 0; i < sizeOfData; i++) {
		listOfPaths[i] = listOfPaths[i].replace(/idToReplace/, "convSel_" + i);
		listOfPaths[i] = listOfPaths[i].replace(/id_ToReplace/,"convSela_" + i);
		container.append("<li>" + listOfPaths[i] + "</li>");
		// handle click event of a link
		$("#convSel_" + i).live("click", function() {
			$("#sendFile").attr("disabled", "");
			if ($("#conversionFormDiv").css('display')!="none") {
				$("#moreOptions").css({
						'display' : '',
						'opacity' : '0'
					});
					$('#moreOptions').animate({
						opacity : 1.0
						}, 150, function() {
						    // Animation complete.
					});
			}
			$("#message").animate({
				opacity : 0.0
				}, 250, function() {
					$("#message").html("<p>Choose the file, upload images and press convert.</p>");	
					$("#message").animate({
					opacity : 1.0
					}, 250, function() {
					});					
				});
			pathSelectionEvent($(this));
		});
		$("#convSela_" + i).live("click", function() {
			$("#sendFile").attr("disabled", "");
			var li = $(this).attr("id");
			li = li.replace(/convSela_/,"");
			document.getElementById("convSel_" + li).checked = true;
			if ($("#conversionFormDiv").css('display')!="none") {
				$("#moreOptions").css({
						'display' : '',
						'opacity' : '0'
					});
					$('#moreOptions').animate({
						opacity : 1.0
						}, 150, function() {
						    // Animation complete.
					});
			}
			$("#message").animate({
				opacity : 0.0
				}, 250, function() {
					$("#message").html("<p>Choose the file, upload images and press convert.</p>");	
					$("#message").animate({
					opacity : 1.0
					}, 250, function() {
					});					
				});
			pathSelectionEvent($("#convSel_"+li));
		});
			
	}
	$("#message").animate({
		opacity : 0.0
		}, 250, function() {
			$("#message").html("<p>Select the format into which you want to convert your document.</p>");
			$("#message").animate({
			opacity : 1.0
			}, 250, function() {
			});					
		});
	$('#conversionsPathsListContainer').animate({
		opacity : 1.0
		}, 500, function() {
		    // Animation complete.
	});

}
 
/**
 * Event method of selection of conversion path.
 * 
 * @param selector
 * @return
 */ 
function pathSelectionEvent(selector){
	_hideResults();
	hideError();
	selectedPathURL = selector.attr("alt");
	var selPath = getPathByURL(selectedPathURL);
	if(selPath == null){
		showError("Error reading conversions paths!");
	}
	else{
		selPath.renderProperties("moreOptions");
		selectedConvPath = selPath;
	}
	showUrlPath();
	if ($("#conversionFormDiv").css('display')=="none") {
		$("#conversionFormDiv").css( {
			'display' : '',
			'opacity' : '0'
		});
		$('#conversionFormDiv').animate({
			opacity : 1.0
			}, 500, function() {
			    // Animation complete.
		});
	}
	$("#selectedPath").empty();
	$("#selectedPath").append(selector.attr('value'));
}
 
function _hideResults(){
	$("#convValidationResult").empty();
	$("#convValidationResult").css({'visibility':'hidden', 'padding' : '0px'});
}

/**
 * Handles submit event of form with data file 
 * to convert.  
 * 
 * @return
 */
function convertFile() {
	if (typeof selectedPathURL == 'undefined' || selectedPathURL == "") {
		alert("No conversions path was selected! Please select one and try again.");
	} else {
		if (document.conversionForm.fileToConvert.value != '') {
			$("#sendFile").attr("disabled", "disabled");
			_hideResults();
			hideError();
			$("#message").animate({
				opacity : 0.0
				}, 250, function() {
					$("#message").html("<p>File submitted for conversion. Please wait until the download dialog appears...</p>");
				$("#message").animate({
					opacity : 1.0
					}, 250, function() {
					});					
			});
			$("#conversionForm").ajaxSubmit( {
				dataType : 'xml',
				url : selectedPathURL+"conversion?properties="+selectedConvPath.readOptionsAsXml(),
				success : function(data, status) {
					hideError();
					var err = data.getElementsByTagName("pre");
					if (typeof err != 'undefined' && err != null) {
						err = err[0];
						if (typeof err != 'undefined' && err != null) {
							showError(err.childNodes[0].nodeValue);
							$("#sendFile").attr("disabled", "");
						}else{
							resolveErrorResponse(data, $("#convValidationResult"));
						}
					}else{
						resolveErrorResponse(data, $("#convValidationResult"));
					}
				}
			});
		} else {
			alert("No file specified! Please choose file you want to convert and try again.");
		}
	}
}

function resolveErrorResponse(data, validationContainer){
	var result = data.getElementsByTagName("validation-result")[0];
	if(typeof result != 'undefined' && result != null){
		printValidationResult(data,validationContainer);
		$("#sendFile").attr("disabled", "");
	}
	else{
		result = data.getElementsByTagName("error")[0];
		if(typeof result != 'undefined' && result != null){
			var msg = result.getAttribute("msg");
			var exClass = result.getAttribute("exclass");
			showError("<p>Error type : " + exClass + "</p><p>Error message : " + msg + "</p>");
			$("#sendFile").attr("disabled", "");
		}
	}
	
}

function resetForm(){
	_hideResults();
	hideError();
	$("#sendFile").attr("disabled", "");
	$("#message").animate({
		opacity : 0.0
		}, 250, function() {
			$("#message").html("<p>Please select the type of the document you want to convert.</p>");
		$("#message").animate({
			opacity : 1.0
			}, 250, function() {
			});					
		});
	$('#conversionFormDiv').animate({
		opacity : 0.0
		}, 150, function() {
			$("#conversionFormDiv").css( {
				'display' : 'none'
		});
		$('#imageFields').html("<span id=\"image0\"><input type=\"file\" id=\"imageField_0\" name=\"imageField_0\" /><a href=\"#\" class=\"remove\" id=\"removeField_0\">- Remove</a><br /></span>");
		if($('#addImages').length==0) {
			$('#imagesInput').append("<a href=\"#\" id=\"addImages\">+ Add more images</a>");
		}
	});
	$('#conversionsPathsListContainer').animate({
		opacity : 0.0
		}, 500, function() {
		    // Animation complete.
	});
	$("#conversionsPathsListContainer").css({
		'display' : 'none'
	});
	$("#moreOptions").hide('fast');
	infoHidden = true;
	$('a#showMore').empty();
	$('a#showMore').append('+ Show advanced options');
	var $inputs = $('#conversionsList li input[type=radio]');
	$inputs.each(function() {
        	$(this).attr("checked", false);
	});
	uploadedImages = Array();
	$('.documentFamily').hide("fast");				
}

function showUrlPath() {
	var props = new String(selectedConvPath.readOptionsAsXml());
	if($("#urlPath").length==0) $("#moreOptions").append("<div id=\"urlPath\" class=\"vertOptions\"><p><strong>URL for conversion with current properties:</strong></p><div id=\"showPathWithProperties\"></div><p><strong>URL for conversion with default properties:</strong></p><div id=\"showPathWithoutProperties\"></div></div>");

	else $("#urlPath").html("<p><strong>URL for conversion with current properties:</strong></p><div id=\"showPathWithProperties\"></div><p><strong>URL for conversion with default properties:</strong></p><div id=\"showPathWithoutProperties\"></div></div>");

	$("#showPathWithProperties").text(selectedPathURL+"conversion?properties=" + props).html();
	$("#showPathWithoutProperties").text(selectedPathURL).html();
}
