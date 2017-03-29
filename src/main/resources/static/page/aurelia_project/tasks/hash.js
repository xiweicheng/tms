var gulp = require('gulp'); // http://gulpjs.com/
var hash = require('gulp-hash-filename'); // https://www.npmjs.com/package/gulp-hash-filename

export default function hash() {
    return gulp.src('scripts/vendor-bundle*.js')
        .pipe(hash({ "format": "{name}.{hash:8}.{size}{ext}" }))
        .pipe(gulp.dest('scripts/'));
}
