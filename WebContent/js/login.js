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
                    console.log('Success');
                },
                error: err => {
                    console.log('An Error occurred: ' + err.responseText);
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