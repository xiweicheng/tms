var markdownpdf = require("markdown-pdf");

// process.argv.forEach(function (val, index, array) {
//     console.log(index + ': ' + val);
// });

console.log('Convert md to pdf start...');

console.log(process.argv[2]);
console.log(process.argv[3]);

// node . test.md test.pdf
markdownpdf().from(process.argv[2]).to(process.argv[3], function() {
    console.log(`Convert md(${process.argv[2]}) to pdf(${process.argv[3]}) Done!`);
});
