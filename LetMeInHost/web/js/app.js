function authenticate(username, password) {
    $.ajax({
        url: '/login/authenticate',
        headers: {
            Authorization: 'Basic ' + btoa(username + ':' + password)
        }
    })
    .done(function(session) {
        alert('Session is ' + session);
    })
    .fail(function(jqXHR, textStatus) {
        alert('failed with ' + textStatus);
    });
}
