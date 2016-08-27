var bookingInfo = {};
var sessionId = '';

function authenticate(username, password) {
    $('#login-error').addClass("hidden-element");
    $.ajax({
        url: '/login/authenticate',
        headers: {
            Authorization: 'Basic ' + btoa(username + ':' + password)
        }
    })
    .done(function(sessionJson) {
        var sessionInfo = JSON.parse(sessionJson);
        sessionId = sessionInfo.SessionId;
        bookingInfo = sessionInfo.Booking;

        $('#login').addClass('inactive-page')
        $('#home').removeClass('inactive-page')
    })
    .fail(function(jqXHR, textStatus) {
        $('#login-error').removeClass("hidden-element");
    });
}


