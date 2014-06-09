define([
    'angular',
    'text!./menu.html',
    'css!./menu'
], function(angular, menuTemplate) {
    'use strict';

    angular.module('menu', [])
        .directive('menu', [menu]);

    function menu() {
        return {
            restrict: 'E',
            transclude: true,
            replace: true,
            template: menuTemplate
        };
    }
});