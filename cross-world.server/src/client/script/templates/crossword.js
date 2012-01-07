var CW = CW || {};
CW.Templates = CW.Templates || {};

CW.Templates.crossword = {
	cell:	'<div class="crosswordCell">' +
			'	{{#blank}}' +
			'	<div class="blankCell"></div>' +
			'	{{/blank}}' +
			'	{{^blank}}' +
			'	<div class="fullCell">{{letter}}</div>' +
			'	{{/blank}}' +
			'</div>'
}