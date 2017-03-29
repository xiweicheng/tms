import gulp from 'gulp';
var del = require('del'); // https://www.npmjs.com/package/del

export default function clean() {

    return del([
        'scripts/**/*'
    ]);
}
