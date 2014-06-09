define([
    'angular',
    'text!./item.html',
    'css!./item'
], function(angular, itemTemplate) {
    'use strict';

    angular.module('item', [])
        .directive('item', [item]);

    function item() {
        return {
            scope : {
                url : '@'
            },
            restrict: 'E',
            transclude: true,
            replace: true,
            template: itemTemplate
        };
    }
});