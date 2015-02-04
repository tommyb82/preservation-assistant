/* Preservation Assistant JS */
;
/* Globals */
var basepath = $("#basepath").text();

/*
 * Module: Preserve data set
 */

/**
 * Configures, but does not open, the set reg creds dialog
 */
function configSetRegCredsDialog() {
	$("#setRegCreds").dialog({
		title : "Set Registry Credentials",
		modal : true,
		autoOpen : false,
		width : 320,
		height : 230
	});
}

/**
 * Configures the set reg creds form submit event handler, so an ajax post is
 * performed instead of a normal form post.
 */
function configSetRegCredsForm() {
	$("#setRegCredsForm")
			.submit(
					function(event) {
						event.preventDefault();

						var $form = $(this), regUID = $form.find(
								"input[name='regUID']").val(), regPrincipal = $form
								.find("input[name='regPrincipal']").val(), regCred = $form
								.find("input[name='regCred']").val(), url = $form
								.attr("action");

						var posting = $.post(url, {
							regUID : regUID,
							regPrincipal : regPrincipal,
							regCred : regCred
						});

						posting.done(function(setRegCredsResult) {
							$("#setRegCreds").dialog("close");
							checkRegAccess(regUID);
						});
					});
}

/**
 * Clear the set reg creds form, re-populates it with the given regUID and opens
 * the dialog
 */
function addCreds(regUID) {
	document.setRegCredsForm.reset();
	document.setRegCredsForm.regUID.value = regUID;
	$("#setRegCreds").dialog("open");
}

/**
 * Call a server side resource to check if write access is currently enabled for
 * a given registry.
 * 
 * @param regUID
 */
function checkRegAccess(regUID) {
	$("#regAuthStatus").html("Checking authentication...");
	$.getJSON(basepath + "registries/" + regUID + "/checkaccess",
			function(regAuthStatus) {
				console.log("complete");
			}).done(
			function(regAuthStatus) {
				/* Success */
				var writeAuthenticated = regAuthStatus.writeAuthenticated;
				if (writeAuthenticated) {
					$("#regAuthStatus").html("Authentication successful");
				} else {
					$("#regAuthStatus").html(
							"Authentication required <br />- <a href=\"javascript:addCreds('"
									+ regUID + "');\">add now</a>");
				}
			}).fail(function(errorData) {
		/* Error */
		$("#regAuthStatus").html("Error checking authentication");
	});
}

/*
 * Module: Data set Preservation
 */
var jobDetailsIntervalID;

function readyPreserveDS() {
	configSetRegCredsDialog();
	configSetRegCredsForm();
	configJobDetailsDialog();

	$("#registry").change(function() {
		checkRegAccess($(this).val());
	});

	// Perform check once page loaded on default value:
	checkRegAccess(document.regSelectForm.registry.value);
}

function configJobDetailsDialog() {
	$("#jobDetailsDialog").dialog({
		title : "Preservation job details",
		modal : true,
		autoOpen : false,
		width : 900,
		height : 600,
		close : function(event, ui) {
			$('#jobDetails').DataTable().destroy();
			// Cancel the job details refresh job:
			clearInterval(jobDetailsIntervalID);
		}
	});
}

function viewJobDetails(datasetName, registryUID) {

	$('#jobDetailsJobName').text(datasetName);
	$('#jobDetailsRegName').text(registryUID);

	$('#jobDetails')
			.dataTable(
					{
						autoWidth : true,
						paging : true,
						searching : false,
						processing : true,
						ajax : basepath
								+ "preservation/jobdetails?datasetName="
								+ datasetName,
						columns : [
								{
									"data" : "preservable",
									"render" : function(preservable, type, row,
											meta) {
										if (preservable.preservableTypeID == 'MF') {
											return preservable.name + ' ('
													+ preservable.displayName
													+ '): '
													+ preservable.itemFileName;
										} else {
											return 'RepInfo Group: '
													+ preservable.rilName;
										}
									}
								},
								{
									"data" : "preservable",
									"render" : function(preservable, type, row,
											meta) {
										if (preservable.preservableTypeID == 'MF') {
											return preservable.type;
										} else {
											return 'RIL Group';
										}
									}
								},
								{
									"data" : null,
									"render" : function(data, type, row, meta) {
										if (data.thrown !== null) {
											return "Error during preservation";
										} else if (data.succeeded
												&& (data.preservedObject !== null)) {
											return "Complete";
										} else if (!data.succeeded
												&& (data.preservedObject === null)) {
											return "In progress <img src='"
													+ basepath
													+ "/images/preload.gif' />";
										}
										return "Unknown";
									}
								},
								{
									"data" : "preservedObject",
									"render" : function(preservedObject, type,
											row, meta) {
										if (preservedObject !== null) {

											var result = "CPID: "
													+ preservedObject.cpid.uid;
											if (preservedObject.registryObjectType == 'MANIFEST') {
												result += "<br />URL: <a href='"
														+ preservedObject.location
														+ "'>"
														+ preservedObject.location
														+ "</a>";
											}
											return result;

										} else {
											return "Please wait..";
										}
									}
								} ]
					});

	jobDetailsIntervalID = setInterval(function() {
		$('#jobDetails').DataTable().ajax.reload();
	}, 5000);

	$("#jobDetailsDialog").dialog("open");
}

/*
 * Module: Edit Data Set Item
 */
function readyEditDSItem() {

	if ($("#rilCPID").val() != null) {
		fetchRILMemberItemsByCPID($("#rilCPID").val());
	}

	$("input[type=radio][name=dataHolderType]").change(function() {
		if (this.value == "BYTESTREAM") {
			// Hide and disabled the URL field, enable and show the file control
			// and enable flex fields
			$("#dataHolderURL").addClass("hidden");
			$("#dataHolderURL").attr("disabled", "disabled");
			$("#dataHolderFile").removeAttr("disabled");
			$("#dataHolderFileBlock").removeClass("hidden");
			$("input[class=dynamicField]").removeAttr("disabled");
		} else if (this.value == "URI") {
			// Hide and disabled the File control, enable and show the URL field
			// and disable flex fields
			$("#dataHolderFileBlock").addClass("hidden");
			$("#dataHolderFile").attr("disabled", "disabled");
			$("#dataHolderURL").removeAttr("disabled");
			$("#dataHolderURL").removeClass("hidden");
			$("input[class=dynamicField]").attr("disabled", "disabled");
		}
	});

	$("#rilCPID").change(function() {
		fetchRILMemberItemsByCPID($(this).val());
	});

	$(".message").delay(2000).slideUp("fast");
}

function fetchRILMemberItemsByCPID(rilCPID) {

	var datasetName = $("#datasetName").text();

	$("#rilMemberItems").html(
			"<li>Reloading... <img src='" + basepath
					+ "/images/preload.gif' /></li>");

	$.getJSON(
			basepath + "datasets/" + datasetName
					+ "/items/groups/rilmemberitems?rilCPID=" + rilCPID,
			function() {
				console.log("ril member items request succeded");
			}).done(
			function(rilMemberItems) {

				$("#rilMemberItems").html('');

				if (rilMemberItems != null && rilMemberItems.length > 0) {

					var nodesToInsert = '';

					$.each(rilMemberItems, function(entryIdx, entry) {
						nodesToInsert += "<li><strong>" + entry.key
								+ "</strong><ul>";

						$.each(entry.value, function(itemIndex, item) {
							nodesToInsert += "<li>" + item.itemFileName
									+ "</li>";
						});
						nodesToInsert += "</ul></li>";
					});

					$("#rilMemberItems").append(nodesToInsert);

				} else {
					$("#rilMemberItems").append(
							"<li>This group currently contains no items</li>");
				}

			}).fail(function(errorData) {
		$("#regAuthStatus").html("Error retrieving the other group items");
	});
}

/*
 * Module: User management
 */
function readyUserManagement() {
	$("input[type=checkbox][name=editactive]").change(function() {

		var msgSpan = $(this).next();

		$.post(basepath + '/admin/users/editactive', {
			editactive : $(this).is(':checked'),
			editactiveusername : $(this).prev().val()
		}, function(data) {
			if (data == 'true') {
				msgSpan.html('saved');
			} else {
				msgSpan.html('failed');
			}
			msgSpan.show();
			msgSpan.delay(2000).fadeOut();
		}, 'text');
	});
};

/*
 * Module: Data set home
 */
function readyDatasetHome() {

}