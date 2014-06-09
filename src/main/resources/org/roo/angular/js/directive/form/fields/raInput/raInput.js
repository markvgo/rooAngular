define([
    'angular',
    'angular-messages',
    'text!./raInput.html',
    'css!./raInput'
], function(angular, angularMessages, inputTemplate) {
    'use strict';

    angular.module('raInput', ['ngMessages'])
        .directive('raInput', [raInput]);

    function raInput() {
        return {
            restrict: 'E',
            transclude:   true,
            replace: true,
            require: '?ngModel',
            template: inputTemplate,
            scope : {
                    id : "=id",
                    name : '=name',
                    type : "=type",
                    label : '@',
                    mandatory : '=mandatory'
                },
            link: function(scope, element, attrs, ngModel, tranclude) {
                scope.id = attrs.id;
             //   scope.name = attrs.name;
                scope.type = attrs.type;
                scope.label = attrs.label;

                scope.test = false;
                if (scope.mandatory){
                    scope.test = true;
                }

                if (scope.isRequired) {
                    scope.isRequired = true;
                }
               // scope.label = attrs.label;
                element.addClass(attrs.side);

            }//,

        };
    }}
);