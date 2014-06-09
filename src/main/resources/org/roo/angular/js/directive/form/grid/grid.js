define([
    'angular',
    'restangular',
    'ng-table',
    'text!./grid.html',
    'css!./grid'
], function(angular,restangular,ngTable,listTemplate) {
    'use strict';

    angular.module('grid', ['restangular','ngTable'])
        .directive('grid', [grid]);

    function grid() {
        return {
            restrict: 'E',
            transclude: true,
            replace: true,
            require: '?ngModel',
            template: listTemplate,
            scope : {
                entity : '=',
            },
            link: function($scope, element, attrs, ctrl, tranclude) {
                $scope.entity = attrs.entity;
            },
            controller: ['$scope','$element', '$attrs','Restangular','ngTableParams', function($scope,$element,$attrs,Restangular, ngTableParams ) {
                 Restangular.all( $attrs.entity ).getList().then(function(response) {
                    $scope.gridData = response.data;
                });
                $scope.columnTitles = $attrs.titles.split(',');
                $scope.columnFields = $attrs.fields.split(',');
            }]
        };
    }
});