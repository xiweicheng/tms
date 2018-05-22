var csv2md = require('csv2md/src/csv2md').csv2md;
var fs = require('fs');

//1. load string from csv file
var data = fs.readFileSync(process.argv[2], 'utf-8').toString();

//console.log(data);

//2. csv2md convert
var markdown = csv2md(data, {
    pretty: true
});

//3. output md table
console.log(markdown);
