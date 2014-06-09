require.config({

    baseUrl: "js",
    
    path: "http://localhost:8080/bim-0.1.0.BUILD-SNAPSHOT",
    
    // alias libraries paths.  Must set 'angular'
    paths: {
        'angular': '../bower_components/angular/angular',
        'angular-route': '../bower_components/angular-route/angular-route',
        'angular-messages': '../bower_components/angular-messages/angular-messages',
        'angular-resource': '../bower_components/angular-resource/angular-resource',
        'angularAMD': '../bower_components/angularAMD/angularAMD',
        'ui-bootstrap': '../bower_components/angular-bootstrap/ui-bootstrap',
        'ui-bootstrap-tpls': '../bower_components/angular-bootstrap/ui-bootstrap-tpls',
        'ng-table': '../bower_components/ng-table/ng-table',
        'tinymce': '../bower_components/tinymce/tinymce.min',
        'ui.tinymce': '../bower_components/angular-ui-tinymce/src/tinymce',
        css: '../bower_components/require-css/css',
        normalize: '../bower_components/require-css/normalize',
        'css-builder': '../bower_components/require-css/css-builder',
        'is': '../bower_components/require-is/is',
        'is-api': '../bower_components/require-is/is-api',
        'is-builder': '../bower_components/require-is/is-builder',
        'lodash': '../bower_components/lodash/dist/lodash',
        'angular-lodash': '../bower_components/angular-lodash/angular-lodash',
        'restangular': '../bower_components/restangular/dist/restangular',
        text: '../bower_components/requirejs-text/text'
    },

    // Add angular modules that does not support AMD out of the box, put it in a shim
    shim: {
        'angularAMD': ['angular'],
        'angular-route': ['angular'],
        'angular-resource': ['angular'],
        'angular-messages': ['angular']
    },

    // kick start application
    deps: ['app']
});


define('angular', angular);

