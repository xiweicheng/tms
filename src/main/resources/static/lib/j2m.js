
(function() {

	/**
	 * Takes Jira markup and converts it to Markdown.
	 *
	 * @param {string} input - Jira markup text
	 * @returns {string} - Markdown formatted text
	 */
	function toM(input) {
		input = input.replace(/^h([0-6])\.(.*)$/gm, function (match,level,content) {
			return Array(parseInt(level) + 1).join('#') + content;
		});

		input = input.replace(/([*_])(.*)\1/g, function (match,wrapper,content) {
			var to = (wrapper === '*') ? '**' : '*';
			return to + content + to;
		});

		input = input.replace(/\{\{([^}]+)\}\}/g, '`$1`');
		input = input.replace(/\?\?((?:.[^?]|[^?].)+)\?\?/g, '<cite>$1</cite>');
		input = input.replace(/\+([^+]*)\+/g, '<ins>$1</ins>');
		input = input.replace(/\^([^^]*)\^/g, '<sup>$1</sup>');
		input = input.replace(/~([^~]*)~/g, '<sub>$1</sub>');
		input = input.replace(/-([^-]*)-/g, '-$1-');

		input = input.replace(/\{code(:([a-z]+))?\}([^]*)\{code\}/gm, '```$2$3```');

		input = input.replace(/\[(.+?)\|(.+)\]/g, '[$1]($2)');
		input = input.replace(/\[(.+?)\]([^\(]*)/g, '<$1>$2');

		input = input.replace(/{noformat}/g, '```');

		return input;
	};

	/**
	 * Takes Markdown and converts it to Jira formatted text
	 *
	 * @param {string} input
	 * @returns {string}
	 */
	function toJ(input) {

		// ![5242c580-bba2-41cf-831f-b33b503eb1b9.png](http://translation.sh1.newtouch.com/upload/img/0/5242c580-bba2-41cf-831f-b33b503eb1b9.png) -> !http://translation.sh1.newtouch.com/upload/img/0/5242c580-bba2-41cf-831f-b33b503eb1b9.png!
		input = input.replace(/!\[.*\]\((.*)\)/g, '\n!$1!\n');

		// > some text -> {quote}some text{quote}
		input = input.replace(/^(\s*>\s*)(.*)/gm, '{quote}$2{quote}');

		input = input.replace(/^(.*?)\n([=-])+$/gm, function (match,content,level) {
			return 'h' + (level[0] === '=' ? 1 : 2) + '. ' + content;
		});

		input = input.replace(/^([#]+)(.*?)$/gm, function (match,level,content) {
			return 'h' + level.length + '.' + content;
		});

		input = input.replace(/([*_]+)(.*?)\1/g, function (match,wrapper,content) {
			var to = (wrapper.length === 1) ? '_' : '*';
			return to + content + to;
		});
		// Make multi-level bulleted lists work
  		input = input.replace(/^(\s*)- (.*)$/gm, function (match,level,content) {
    			var len = 2;
    			if(level.length > 0) {
        			len = parseInt(level.length/4.0) + 2;
    			}
    			return Array(len).join("-") + ' ' + content;
  		});

		var map = {
			cite: '??',
			del: '-',
			ins: '+',
			sup: '^',
			sub: '~'
		};

		input = input.replace(new RegExp('<(' + Object.keys(map).join('|') + ')>(.*?)<\/\\1>', 'g'), function (match,from,content) {
			//console.log(from);
			var to = map[from];
			return to + content + to;
		});

		input = input.replace(/~~(.*?)~~/g, '-$1-');

		input = input.replace(/`{3,}(\w+)?((?:\n|.)+?)`{3,}/g, function(match, synt, content) {
			var code = '{code';

			if (synt) {
				code += ':' + synt;
			}

			code += '}' + content + '{code}';

			return code;
		});

		input = input.replace(/`([^`]+)`/g, '{{$1}}');

		input = input.replace(/\[([^\]]+)\]\(([^)]+)\)/g, '[$1|$2]');
		input = input.replace(/<([^>]+)>/g, '[$1]');

		return input;
	};


	/**
	 * Exports object
	 * @type {{toM: toM, toJ: toJ}}
	 */
	var J2M = {
		toM: toM,
		toJ: toJ
	};

	// exporting that can be used in a browser and in node
	try {
		window.J2M = J2M;
	} catch (e) {
		// not a browser, we assume it is node
		module.exports = J2M;
	}
})();
