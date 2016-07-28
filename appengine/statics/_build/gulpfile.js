var gulp = require("gulp");
var sass = require("gulp-sass");
var autoprefixer = require("gulp-autoprefixer");
var browserSync = require("browser-sync");
var watch = require("gulp-watch");
var plumber = require("gulp-plumber");
var browserify = require("browserify");
var source = require('vinyl-source-stream');
var tsify = require("tsify");

var distpath = {
    html: "../",
    js: "../js",
    css: "../css",
    font:"../font",
    img: "../img"
};

// _buil以下のパス
var srcpath  = {
    html: "./html/index.html",
    ts: "./src/ts/main.ts",
    scss: "./src/sass/main.scss"
};

gulp.task("browser-sync", function() {
    return browserSync.init(null, {
        server: "../html"
    });
});

gulp.task("html", function() {
    gulp.src(srcpath.html)
        .pipe(gulp.dest(distpath.html));
});

gulp.task("bootstrap", function() {
    gulp.src(srcpath.scss)
        .pipe(plumber(function(error) {
            return this.emit("end");
        }))
        .pipe(sass({
                includePaths: ["./bower_components/bootstrap-sass/assets/stylesheets"]
        }).on("error", sass.logError))
        .pipe(autoprefixer())
        // .pipe(gulp.dest("./html/css"))
        .pipe(gulp.dest(distpath.css))
        .pipe(browserSync.reload({
            stream: true
        }));
});

gulp.task("fonts", function() {
  gulp.src("./bower_components/bootstrap-sass/assets/fonts/bootstrap/*.{eot,svg,ttf,woff,woff2}")
  .pipe(gulp.dest(distpath.font));
});

gulp.task("typescript", function() {
    return browserify()
        .add(srcpath.ts)
        .plugin("tsify", {
            target: "ES5",
            removeComments: true
        })
        .bundle()
        .on('error', function (err) {
            console.log(err.toString());
            this.emit("end");
        })
        .pipe(source("main.js"))
        .pipe(gulp.dest(distpath.js))
        .pipe(browserSync.reload({
            stream: true
        }));
});

gulp.task("watch", function() {
    watch("./**/*.html").on("change", browserSync.reload);
    watch(["./html/**/*.html"], function() {
        return gulp.start(["html"]);
    });
    watch(["./src/ts/**/*.ts"], function() {
        return gulp.start(["typescript"]);
    });
    watch(["./src/sass/**/*.scss"], function() {
        return gulp.start(["bootstrap"]);
    });
});

gulp.task("default", ["fonts", "bootstrap","typescript" ,"browser-sync", "watch"]);
