var gulp = require('gulp'); // http://gulpjs.com/
var replace = require('gulp-replace'); // https://github.com/lazd/gulp-replace
var fs = require('fs');

function getBundleFileName() {
    
    var path = 'scripts/';
    var files = fs.readdirSync(path);

    var startWith = 'vendor-bundle';
    var fileName = '';

    files.forEach(function(file) {

        var stat = fs.statSync(path + file);
        if (stat.isFile()) {
            if(file.indexOf(startWith) === 0) {
                if(file.length > fileName.length) {
                    fileName = file;
                }
            }
        }

    });

    return fileName;
}

export default function replace() {
    gulp.src('index.html')
        .pipe(replace(/(scripts\/)vendor\-bundle[^\.]*\.js/g, '$1' + getBundleFileName()))
        .pipe(gulp.dest('./'));
}
