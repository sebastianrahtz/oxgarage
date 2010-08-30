
var vUsesDefaultUrl = false;

$().ready(
		function() {
			performAjaxRequest(homePageUrl + "Validation/", "GET",
					validationListDataHandler, validationErrorHandler);
		});

function validationErrorHandler(xmlHttpRequest, txtStatus, errorThrowed) {
	if ((xmlHttpRequest.status == "404" || xmlHttpRequest.status == "0")  && !vUsesDefaultUrl) {
		vUsesDefaultUrl = true;
		homePageUrl = defaultUrl;
		performAjaxRequest(homePageUrl + "/Validation/", "GET",
				validationListDataHandler, validationErrorHandler);
	}
	else{
		if(xmlHttpRequest.status == "404" || xmlHttpRequest.status == "0"){
			 errorThrowed = "Connection to web service refused (could be wrong address)!";
		 }
		_showError(txtStatus + " error: " + errorThrowed + " status:"
			+ xmlHttpRequest.status);
	}
}

var validationsDest;
var selectedValidationURL;
var lastDTSelection;

function validationListDataHandler(data) {
	var inputDataTypes = data.getElementsByTagName("validations")[0];
	inputDataTypes = inputDataTypes.getElementsByTagName("input-data-type");
	var size = inputDataTypes.length;
	var container = $("#validationsList");
	if (size == 0) {
		$("#validationListContainer").append("<p> No content </p>");
		return;
	}
	var validationsDest = new Array(size);
	for ( var i = 0; i < size; i++) {
		var current = inputDataTypes[i];
		var id = current.getAttribute("id");
		validationsDest[i] = current.getAttribute("xlink:href");
		if (validationsDest[i] == null) {
			validationsDest[i] = current.getAttribute("href");
		}
		// create link from input type
		container
				.append("<li><input type=\"radio\" name=\"valInputs\" id=\"vSel_"
						+ i
						+ "\" /><a href=\"#\" id=\"vSela_"
						+ i
						+ "\">"
						+ id
						+ "</a>");
		// handle click event of link
		$("#vSel_" + i).live("click", function() {
			_hideError();
			lastDTSelection = $(this);
			var id = $(this).attr("id");
			var li = parseInt(id.replace(/vSel_/, ""));
			selectedValidationURL = validationsDest[li];
		});
		$("#vSela_" + i).live("click", function() {
			_hideError();
			lastDTSelection = $(this);
			var id = $(this).attr("id");
			var li = parseInt(id.replace(/vSela_/, ""));
			selectedValidationURL = validationsDest[li];
			document.getElementById("vSel_" + li).checked = true;
		});
	}

}

function _showError(msg) {
	var errMsg = $("#errorMessageV");
	 $('.thickbox').each(function(i) {$(this).unbind('click');});
	tb_init('a.thickbox');	
	errMsg.append("<img style=\"float:left;margin-right:5px;\" src=\"style/img/err.gif\"/><b>Error occured: <a href=\"helpFiles/errorHelp.html?height=300&with=450\" title=\"Help\" class=\"thickbox helpLink\">?</a></b> "
					+ msg);
	$("#errorBoxV").css( {
		'visibility' : 'visible'
	});
	$("#message").html("<p>Error occured. Please try again.</p>");
}

function _hideError() {
	$("#errorMessageV").empty();
	$("#errorBoxV").css( {
		'visibility' : 'hidden'
	});
}

function hideResults() {
	$("#validationResult").empty();
	$("#validationResult").css( {
		'visibility' : 'hidden',
		'padding' : '0px'
	});
}

function printValidationResult(responseData, cont) {
	var result = responseData.getElementsByTagName("validation-result")[0];
	cont.css( {
		'visibility' : 'visible',
		'padding' : '15px'
	});
	var status = result.getElementsByTagName("status")[0].childNodes[0].nodeValue;
	//cont.append("<p style=\"background:#fff;padding:2px;\"><b style=\"font-size:9pt;\">Validation result</b></p>");
	var status_style;
	if (status == "ERROR" || status == "FATAL") {
		if (status == "FATAL") {
			status = "Selected file type doesn't match the document type<a href=\"helpFiles/validationErrorHelp.html?height=300&with=450\" title=\"Help\" class=\"thickbox helpLink\">?</a>";
		} else if (status == "ERROR") {
			status = "Selected file type doesn't match the document type<a href=\"helpFiles/validationErrorHelp.html?height=300&with=450\" title=\"Help\" class=\"thickbox helpLink\">?</a>";
		}
	} else if (status == "SUCCESS") {
		status_style = "background:#fff;color:#000;padding:2px;";
		status = "Success";
	}
	cont.append("<p><b>"
			+ status + "</b></p>");
	 $('.thickbox').each(function(i) {$(this).unbind('click');});
	tb_init('a.thickbox');	
	var messages = result.getElementsByTagName("messages")[0];
	messages = messages.getElementsByTagName("message");
	if (messages.length > 0) {
		cont.append("<p><b>Please check the file type and try again</p>");
		var msgCont = "<div id=\"messagesCont\" style=\"overflow:auto;height:150px;padding-left:15px;width:752px;font-size:8pt;text-align:left;margin-top:30px;\"><b>Errors detected during validation of the document:</b>";
		var msg;
		for ( var i = 0; i < messages.length; i++) {
			msg = messages[i].childNodes[0].nodeValue;
			if (msg != "...") {
				msgCont += "<p>" + (i + 1) + ") " + msg + "</p>";
			} else {
				msgCont += "<p>" + msg + "</p>";
			}
		}
		msgCont += "</div>";
		cont.append(msgCont);
		$("#message").html("<p>Error occured. Please try again.</p>");
	}
}

function validateFile() {
	if (typeof selectedValidationURL == 'undefined'
			|| selectedValidationURL == "") {
		alert("No input data type selected. Select one and try again. ");
	} else {
		if (document.validationForm.fileToValidate.value != '') {
			hideResults();
			_hideError();
			$("#validationForm").ajaxSubmit(
					{
						dataType : 'xml',
						url : selectedValidationURL,
						success : function(data, status) {
							var err = data.getElementsByTagName("pre");
							if (typeof err != 'undefined' && err != null) {
								err = err[0];
								if (typeof err != 'undefined' && err != null) {
									_showError(err.childNodes[0].nodeValue);
								} else {
									_resolveErrorResponse(data,
											$("#validationResult"));
								}
							} else {
								_resolveErrorResponse(data,
										$("#validationResult"));
							}
						}
					});
		} else {
			alert("No file specified! Please choose file you want to validate and try again.");
		}
	}
}

function _resolveErrorResponse(data, validationContainer) {
	var result = data.getElementsByTagName("validation-result")[0];
	if (typeof result != 'undefined' && result != null) {
		printValidationResult(data, validationContainer);
	} else {
		result = data.getElementsByTagName("error")[0];
		if (typeof result != 'undefined' && result != null) {
			var msg = result.getAttribute("msg");
			var exClass = result.getAttribute("exclass");
			_showError("<p>Error type : " + exClass + "</p><p>Error message : "
					+ msg + "</p>");
		}
	}

}
