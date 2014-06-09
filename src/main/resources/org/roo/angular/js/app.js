define(['angularAMD', 'angular-route','angular-resource','lodash','restangular','ng-table','ui.tinymce',
    'directive/menu/menu/menu',
    'directive/menu/category/category',
    'directive/menu/item/item',
    'directive/form/create/create',
    'directive/form/fields/raInput/raInput',
    'directive/form/grid/grid',
    'directive/form/fields/editor/editor'
], function (angularAMD) {
    var app = angular.module("webapp", ['ngRoute','ngResource','lodash','restangular','ngTable','ui.tinymce','menu','category','item','create','raInput','grid','editor']);

    app.config(function ($routeProvider) {
        $routeProvider
            .when("/home", angularAMD.route({
                templateUrl: 'views/home.html', controller: 'HomeCtrl', controllerUrl: 'controller/home'
            }))
            .when("/:entity/create", angularAMD.route({
                templateUrl: function(params){ return 'views/' + params.entity +'/create.html'  }, controller: 'HomeCtrl', controllerUrl: 'controller/home'
            }))
            .when("/:entity/edit", angularAMD.route({
                templateUrl: function(params){ return 'views/' + params.entity +'/edit.html'  }, controller: 'HomeCtrl', controllerUrl: 'controller/home'
            }))
            .otherwise({redirectTo: "/home"});
    });


    var lodash = angular.module('lodash', []);
    lodash.factory('_', function() {
        return window._; // assumes underscore has already been loaded on the page
    });

    var path = 'http://localhost:8080/bim-0.1.0.BUILD-SNAPSHOT';
    
    app.config(function(RestangularProvider) {
        RestangularProvider.setBaseUrl(path);
        RestangularProvider.setDefaultHeaders({
            "Content-Type": "application/json",
            "X-Requested-With": "XMLHttpRequest"
        });
        RestangularProvider.setFullResponse(true);
    });
    app.config(function(RestangularProvider) {
        RestangularProvider.setBaseUrl(path );
        RestangularProvider.setDefaultHeaders({
            "Content-Type": "application/json",
            "X-Requested-With": "XMLHttpRequest"
        });
        RestangularProvider.setFullResponse(true);
    });

    app.config(function($httpProvider) {
        //Enable cross domain calls
        $httpProvider.defaults.useXDomain = true;

        //Remove the header used to identify ajax call  that would prevent CORS from working
        delete $httpProvider.defaults.headers.common['X-Requested-With'];
    });


    app.provider('Post', function() {
            this.$get = ['$resource', function($resource) {
                var Post = $resource('http://localhost:3000/api/post/:_id', {}, {
                    update: {
                        method: 'PUT'
                    }
                })

                return Post;
            }];
        });

    angularAMD.bootstrap(app);

    return app;
});

