var CW = CW || {};

$(document).ready(function () {
	var $crosswordTabs = $("#crosswordTabs");
	$crosswordTabs.tabs({
		disabled: [0, 1, 2]
	});
	$.each(CW.CrosswordTemplates, function (index, crosswordTemplate) {
		var $crosswordWrapper = $("<div>").addClass("crosswordContainer")
				.appendTo($("#crosswordTemplatesTab .crosswordsContainer"))
				.hover(function (e) {
						$(this).addClass("hovered");
					}, function (e) {
						$(this).removeClass("hovered");
					})
				.on("click", function (e) {
					retrieveCrossword(crosswordTemplate)						
						.done(function (crossword) {
							showCrosswordTab(crossword);
						})
						.fail(function (message) {
							$("#generatingCrosswordTab p").text("An error ocurred.");
							$("#generatingCrosswordTab .loader").hide();
						});
					showWaitTab();
				});
		
		showCrossword(crosswordTemplate, $crosswordWrapper);
	});
});

function showCrosswordTemplatesTab() {
	$("#crosswordTabs").tabs("option", "disabled", [1, 2]);
	$("#crosswordTabs").tabs("option", "selected", 0);
	$("#crosswordTabs").tabs("option", "disabled", [0, 1, 2]);
}

function showWaitTab() {
	$("#crosswordTabs").tabs("option", "disabled", [0, 2]);
	$("#crosswordTabs").tabs("option", "selected", 1);
	$("#crosswordTabs").tabs("option", "disabled", [0, 1, 2]);
}

function showCrosswordTab(crossword) {
	var $crosswordContainer = $("#generatedCrosswordTab .crosswordContainer").empty();
	$("#crosswordTabs").tabs("option", "disabled", [0, 1]);
	$("#crosswordTabs").tabs("option", "selected", 2);
	$("#crosswordTabs").tabs("option", "disabled", [0, 1, 2]);
	showCrossword(crossword, $crosswordContainer);
	$("#generatedCrosswordTab #backButton").on("click", function (e) {
		showCrosswordTemplatesTab();
	});
}

function retrieveCrossword(crosswordTemplate) {
	var blankCells = [],
		deferred = new $.Deferred(),
		i;
	for (i = 0; i < crosswordTemplate.grid.length; i++) {
		if (crosswordTemplate.grid[i] === '.') {
			blankCells.push(i);
		}
	}
	
	$.ajax({
		type: "GET",
		cache: "false",
		url: "/crossword/",
		contentType: "application/json",
		dataType: "json",
		data: {
			rows: crosswordTemplate.rows,
			cols: crosswordTemplate.cols,
			blankCells: blankCells
		},
		success: function (crossword) {
			console.log(crossword);
			deferred.resolve(crossword);
		},
		error: function (jqXHR, textStatus) {
			deferred.reject(textStatus);
		}
	});

	
	return deferred.promise();
}

function showCrossword(crossword, $element) {
	var i;
	
	for (i = 0; i < crossword.grid.length; i++) {
		$(Mustache.to_html(CW.Templates.crossword.cell, {
			blank: crossword.grid[i] === '.',
			letter: crossword.grid[i],
			cellNumber: crossword.gridNums && crossword.gridNums[i]
		}))
		.css("width", crossword.gridNums ? 35 : 20)
		.css("height", crossword.gridNums ? 35 : 20)
		.appendTo($element);
	}
	$element.css("width", (crossword.cols * $element.find("div").outerWidth()) + "px");
	
	//Fix element height.
	$element.append(CW.Templates.crossword.clearfix);
}