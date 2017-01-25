import gulp from 'gulp';
import clean from './clean';
import hash from './hash';
import replace from './replace';
import build from './build';

export default gulp.series(
    clean,
    build,
    hash,
    replace
);
