'use strict';

describe("Register Controller", function () {

    beforeEach(module('smlBootstrap.services', 'smlBootstrap.controllers'));

    afterEach(inject(function (_$httpBackend_) {
        _$httpBackend_.verifyNoOutstandingExpectation();
        _$httpBackend_.verifyNoOutstandingRequest();
    }));

    var scope, $httpBackend, ctrl;

    beforeEach(inject(function (_$httpBackend_, $rootScope, $routeParams, $controller) {
        $httpBackend = _$httpBackend_;

        scope = $rootScope.$new();
        ctrl = $controller('RegisterController', {$scope: scope});

        scope.user = {
            password: '',
            repeatPassword: ''
        };

        scope.registerForm = {
            login: {
                $dirty: false
            },
            email: {
                $dirty: false
            },
            password: {
                $dirty: false
            },
            repeatPassword: {
                $dirty: false,
                $error: {
                    repeat: false
                }
            },
            $invalid: false,
            $valid: true
        };
    }));

    it('Should call register rest service when form is valid', function () {
        // Given
        $httpBackend.expectPOST('rest/users/register').respond('anything');

        // When
        scope.register();
        $httpBackend.flush();
    });

    it('Should not call register rest service when form is invalid', function () {
        // Given
        scope.registerForm.$valid = false;

        // When
        scope.register();

        // Then
        // verifyNoOutstandingRequest(); is checked after each test
    });

    it("should not call register rest service when passwords don't match", function() {
        //Given
        scope.registerForm.repeatPassword.$error.repeat = true;

        //When
        scope.register();

        //Then
        //no outstanding requests (checked after test)
    });

});
