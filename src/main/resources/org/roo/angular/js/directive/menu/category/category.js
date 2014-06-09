define([
    'angular',
    'text!./category.html',
    'css!./category'
], function(angular, categoryTemplate) {
    'use strict';

    angular.module('category', [])
        .directive('category', [category]);

    function category() {
        return {
            restrict: 'E',
            transclude: true,
            replace: true,
            template: categoryTemplate
        };
    }
});