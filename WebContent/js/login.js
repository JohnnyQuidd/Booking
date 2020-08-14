$(document).ready(() => {

    $('#login').click(() => {
        let username = $('#username').val();
        let password = $('#password').val();

        if(validFields(username, password)) {
            let payload = JSON.stringify({username, password});

            $.post({
                url: '../rest/user/login',
                data: payload,
                contentType: 'application/json',
                dataType: 'text',
                success: response => {
                    localStorage.setItem('username', username);
					localStorage.setItem('role', response);
					window.location.href = '../index.html';
                },
                error: err => {
                    alert(err.responseText);
                }
            });

        } else {
            showWarnings(username, password);
        }
    });

    function validFields(username, password) {
        return username !== '' && password !== '';
    }

    function showWarnings(username, password) {
        username === '' ? $('#username-label').show() : $('#username-label').hide();
        password === '' ? $('#password-label').show() : $('#password-label').hide();  
    }
});