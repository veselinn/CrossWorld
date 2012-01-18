var CW = CW || {};
CW.Templates = CW.Templates || {};

CW.Templates.crossword = {
	cell:	'<div class="crosswordCell">' +
			'	{{#cellNumber}}' +
			'	<div class=cellNumber>{{cellNumber}}</div>' +
			'	{{/cellNumber}}' +
			'	{{#blank}}' +
			'	<div class="blankCell"></div>' +
			'	{{/blank}}' +
			'	{{^blank}}' +
			'	<div class="fullCell">{{letter}}</div>' +
			'	{{/blank}}' +
			'</div>',
	clue:	'<div class="clue">' +
			'{{clue}}' +
			'</div>',
	clearfix: '<div style="clear: both;">'
}