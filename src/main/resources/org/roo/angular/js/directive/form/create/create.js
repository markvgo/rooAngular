define([
    'angular',
    'ui-bootstrap',
    'ui-bootstrap-tpls',
    'angular-resource',
    'restangular',
    'ui.tinymce',
    'text!./create.html',
    'css!./create'
], function(angular,uibootstrap,uibootstraptpls,resource,restangular,tinymce, createTemplate) {
    'use strict';

    angular.module('create', ['ui.bootstrap','ui.bootstrap.tpls','ngResource','restangular','ui.tinymce'])
        .directive('create', [create]);

    function create() {
        return {
            restrict: 'E',
            transclude: true,
            replace: true,
            template: createTemplate,
            controller: ['$scope', '$element', '$attrs', '$resource','Restangular', function($scope, $element, $attrs, $resource, Restangular ) {

                var requestPath = Restangular.all($attrs.mapping);
                console.log("in Controller");
                console.log($attrs.entity);
                $scope[$attrs.entity] = {};

                $scope.this.submitForm = function(formData) {
                     var saving;
                     $scope.vet = requestPath.post($scope[$attrs.entity]).then(function(response) {
                        console.log("All ok "+response.status);
                    }, function(response) {
                        console.log("Error with status code", response.status);
                    });
                };
            }]
        };
    }
});