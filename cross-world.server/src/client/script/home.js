var CW = CW || {};

$(document).ready(function () {
	$.get("/crossword/").done(function (result) {
		showCrossword(result);
	})
});

function showCrossword(crossword) {
	var i;
	
	$(".crosswordContainer").css("width", (crossword.cols * 22) + "px");
	for (i = 0; i < crossword.grid.length; i++) {
		$(".crosswordContainer").append(Mustache.to_html(CW.Templates.crossword.cell, {
			blank: crossword.grid[i] === '.',
			letter: crossword.grid[i]
		}))
	}
	
	$(".crosswordContainer").append('<div style="clear: both;">');
}