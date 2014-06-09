define([
        'angular',
        'angular-messages',
        'tinymce',
        'text!./editor.html',
        'css!./editor'
    ], function(angular, angularMessages, tinymce, inputTemplate) {
        'use strict';

        angular.module('editor', ['ngMessages','ui.tinymce'])
            .directive('editor', [editor]);

        function editor() {
            return {
                restrict: 'E',
                require: '?ngModel',
                template: inputTemplate,
                controller: ['$scope', '$element', '$attrs', function($scope, $element, $attrs ) {

                    $scope.tinymceOptions = {
                        theme: "modern",
                        plugins: ["link image"
//                            "advlist autolink lists link image charmap print preview hr anchor pagebreak",
//                            "searchreplace wordcount visualblocks visualchars code fullscreen",
//                            "insertdatetime media nonbreaking save table contextmenu directionality",
//                            "emoticons template paste textcolor"
                        ],
                        toolbar1: "link image",
//                        toolbar1: "insertfile undo redo | styleselect | bold italic | alignleft aligncenter alignright alignjustify | bullist numlist outdent indent | link image",
//                        toolbar2: "print preview media | forecolor backcolor emoticons",
                        image_advtab: true,
                        height: "200px",
                        width: "650px"
                    };
                }]
            };
        }}
);

