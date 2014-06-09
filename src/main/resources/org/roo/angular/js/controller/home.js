define(['app'], function (app) {
    app.register.controller('HomeCtrl', function ($scope) {
        $scope.message = "Message from HomeCtrl";
    });
});

var myapp = angular.module("myapp", [])
    .controller("HelloController", function($scope) {
        $scope.helloTo = {};
        $scope.this.submitForm = function(formData) {


            alert("test");
        };

            $scope.helloTo.title = "World, AngularJS";
    } ) .controller("MyController", function($scope) {
        $scope.myData = {};
        $scope.myData.textf = function() { return "A text from a function"; };
    });