'use strict';
/* global require */

// Define dependencies
var gulp = require('gulp'),
    connect = require('gulp-connect');

// Define file locations
var htmlFiles = [ '**/*.html' ],
    cssFiles = [ 'css/**/*.css' ],
    jsFiles = [ 'scripts/**/*.js' ];


gulp.task('connect', function() {
    connect.server({
        root: 'app',
        livereload: true
    });
});

gulp.task('html', function () {
    gulp.src('./app/*.html')
        .pipe(gulp.dest('./app'))
        .pipe(connect.reload());
});

gulp.task('watch', function () {
    gulp.watch(['./app/*.html'], ['html']);
});

gulp.task('copy', function() {
    gulp.src('index.html')
        .pipe(gulp.dest('assets'))
});

gulp.task('default', ['connect']);

